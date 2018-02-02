package eu.supersede.bdma.sa.stream_processes;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.eca_rules.DynamicAdaptationAlert;
import eu.supersede.bdma.sa.eca_rules.SerializableECA_Rule;
import eu.supersede.bdma.sa.eca_rules.SoftwareEvolutionAlert;
import eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition;
import eu.supersede.bdma.sa.eca_rules.conditions.TextCondition;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.integration.api.mdm.types.ActionTypes;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import eu.supersede.integration.api.mdm.types.Event;
import eu.supersede.integration.api.mdm.types.OperatorTypes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import scala.Tuple2;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by snadal on 28/05/17.
 */
public class RuleEvaluation {

    private static Map<ECA_Rule, Long> firedRulesXTimestamp = Maps.newConcurrentMap();

    private static KnowledgePackage compilePkgDescr(PackageDescr pkg ) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newDescrResource( pkg ),
                ResourceType.DESCR );
        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        return kpkgs.iterator().next();
    }

    private static int evaluateNumericRule(String operator, String ruleValue, String[] values) {
        if (operator.equals("=")) operator = "==";
        System.out.println("evaluateNumericRule("+operator+","+ruleValue+","+ Arrays.toString(values));
        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("numericRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        for (String strNum : values) {
            ksession.insert(new DoubleCondition(Double.parseDouble(strNum)));
        }

        int nRules = ksession.fireAllRules();

        return nRules;
    }

    private static int evaluateFeedbackRule(String operator, String ruleValue, String[] values) throws Exception {
        if (operator.equals("=")) operator = "==";
        System.out.println("evaluateFeedbackRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("numericRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.TextCondition").constraint("x "+operator+" \""+ruleValue+"\"").end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String path = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        for (String str : values) {
            String label = feedbackClassifier.classify(path, new UserFeedback(str)).getLabel();
            System.out.println("Extracted value ["+str+"]");
            Sockets.sendMessageToSocket("analysis","Extracted value: "+str);
            System.out.println("Classified as ["+label+"]");
            Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new TextCondition(label));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    private static void windowBasedRuleEvaluation(JavaPairDStream<String, Tuple2<String,Long>> window,
                                                  ActionTypes windowType, Broadcast<List<Event>> events,
                                                  Broadcast<List<ECA_Rule>> rules) {
        window
            .groupByKey()
            .foreachRDD(rdd -> {
                rdd.foreach(records -> {
                    rules.value().forEach(eca_rule -> {
                        if (eca_rule.getEvent().getKafkaTopic().equals(records._1)) {
                            List<String> data = Lists.newArrayList();
                            /*
                            records._2().forEach(t -> {
                                if (firedRulesXTimestamp.get(eca_rule.getEca_ruleID()) < t._2() &&
                                        firedRulesXTimestamp.get(rule.getEca_ruleID())+(evo_adapt.equals("evolution") ? 7200000 : 300000) < System.currentTimeMillis()) {
                                    data.add(t._1());
                                }
                            });
                            */
                            eca_rule.getConditions().forEach(condition -> {
                                switch (condition.getOperator()) {
                                    case "Value": {
                                        List<String> extractedData = Lists.newArrayList();
                                        for (String json : data) {
                                            Utils.extractFeatures(json,condition.getAttribute()).forEach(element -> extractedData.add(element));
                                        }

                                    }
                                }
                            });


                        }
                    });
                });
            });
        System.out.println(windowType);
        window.print();
    }


    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream,
                               Broadcast<List<Event>> events,
                               Broadcast<List<ECA_Rule>> rules) {
        for (ECA_Rule r : rules.value()) {
            firedRulesXTimestamp.put(r, Long.valueOf(0));
        }

        JavaDStream<ConsumerRecord<String, String>> nonEmptyStream = kafkaStream.filter(record -> !record.value().isEmpty());

        JavaPairDStream<String, Tuple2<String,Long>> evolutionWindow = nonEmptyStream
                .flatMapToPair(record -> {
                    List<Tuple2<String, Tuple2<String,Long>>> recordsPerRule = Lists.newArrayList();
                    rules.value().forEach(rule -> {
                        if (rule.getEvent().getKafkaTopic().equals(record.topic()) &&
                                rule.getAction().val().equals(ActionTypes.ALERT_EVOLUTION.val())) {
                            recordsPerRule.add(new Tuple2<String,Tuple2<String,Long>>(rule.getEca_ruleID(),
                                    new Tuple2<String, Long>(record.value(),System.currentTimeMillis())));
                        }
                    });
                    return recordsPerRule.iterator();
                }).window(new Duration(Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_EVOLUTION_MS"))),new Duration(5000));

        JavaPairDStream<String, Tuple2<String,Long>> adaptationWindow = nonEmptyStream
                .flatMapToPair(record -> {
                    List<Tuple2<String, Tuple2<String,Long>>> recordsPerRule = Lists.newArrayList();
                    rules.value().forEach(rule -> {
                        if (rule.getEvent().getKafkaTopic().equals(record.topic()) &&
                                rule.getAction().val().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION.val())) {
                            recordsPerRule.add(new Tuple2<String,Tuple2<String,Long>>(rule.getEca_ruleID(),
                                    new Tuple2<String, Long>(record.value(),System.currentTimeMillis())));
                        }
                    });
                    return recordsPerRule.iterator();
                }).window(new Duration(Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_DYNAMIC_ADAPTATION_MS"))),new Duration(5000));

        JavaPairDStream<String, Tuple2<String,Long>> monitorReconfigurationWindow = nonEmptyStream
                .flatMapToPair(record -> {
                    List<Tuple2<String, Tuple2<String,Long>>> recordsPerRule = Lists.newArrayList();
                    rules.value().forEach(rule -> {
                        System.out.println(rule.getAction().val() + " -- " +ActionTypes.ALERT_MONITOR_RECONFIGURATION.val());
                        if (rule.getEvent().getKafkaTopic().equals(record.topic()) &&
                                rule.getAction().val().equals(ActionTypes.ALERT_MONITOR_RECONFIGURATION.val())) {
                            recordsPerRule.add(new Tuple2<String,Tuple2<String,Long>>(rule.getEca_ruleID(),
                                    new Tuple2<String, Long>(record.value(),System.currentTimeMillis())));
                        }
                    });
                    return recordsPerRule.iterator();
                }).window(new Duration(Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_MONITOR_RECONF_MS"))),new Duration(5000));

        windowBasedRuleEvaluation(evolutionWindow,ActionTypes.ALERT_EVOLUTION,events,rules);
        windowBasedRuleEvaluation(adaptationWindow,ActionTypes.ALERT_DYNAMIC_ADAPTATION,events,rules);
        windowBasedRuleEvaluation(monitorReconfigurationWindow,ActionTypes.ALERT_MONITOR_RECONFIGURATION,events,rules);


/*
        JavaPairDStream<String, Tuple2<String,Long>> theStream = kafkaStream.
                filter(record -> !record.value().isEmpty()).
                flatMapToPair(record -> {
            List<Tuple2<String, Tuple2<String,Long>>> out = Lists.newArrayList();
            rules.value().forEach(rule -> {
                if (rule.getEvent().getKafkaTopic().equals(record.topic())) {
                    String tuple = record.value().toString();
                    // Filter for feedback demo, not to interfere with other users
                    if (record.topic().equals("5ff7d393-e2a5-49fd-a4de-f4e1f7480bf4") && evo_adapt.equals("evolution")) {
                        System.out.println(rule.getName() + " - " + rule.getKafkaTopic() + " - "+record.topic());
                        if (Utils.extractFeatures(tuple, rule.getFeature()) != null &&
                            Utils.extractFeatures(tuple,"http://www.BDIOntology.com/global/Feature/userIdentification").get(0).equals("243205")) {
                            out.add(new Tuple2<String, Tuple2<String, Long>>(rule.getEca_ruleID(), new Tuple2<String, Long>(tuple, System.currentTimeMillis())));
                        }
                    } else {
                        if (Utils.extractFeatures(tuple, rule.getFeature()) != null) {
                            out.add(new Tuple2<String, Tuple2<String, Long>>(rule.getEca_ruleID(), new Tuple2<String, Long>(tuple, System.currentTimeMillis())));
                        }
                    }
                }
            });
            System.out.println("Extracted "+out.toString());
            return out.iterator();
        }).window(new Duration(evo_adapt.equals("evolution") ? 7200000 : 300000), new Duration(5000));
*/
/**
        theStream
            .groupByKey()
            .foreachRDD(records -> {
                records.foreach(set -> {
                        for (SerializableECA_Rule rule : rules) {
                            // if the data is for that rule
                            if (set._1().equals(rule.getEca_ruleID())) {
                                List<String> data = Lists.newArrayList();
                                set._2().forEach(t -> {
                                    if (firedRulesXTimestamp.get(rule.getEca_ruleID()) < t._2() && firedRulesXTimestamp.get(rule.getEca_ruleID())+(evo_adapt.equals("evolution") ? 7200000 : 300000) < System.currentTimeMillis()) {
                                        data.add(t._1());
                                    }
                                });

                                switch (rule.getOperator()) {
                                    case VALUE: {
                                        List<String> extractedData = Lists.newArrayList();
                                        for (String json : data) {
                                            Utils.extractFeatures(json,rule.getFeature()).forEach(element -> extractedData.add(element));
                                        }

                                        int valids = evaluateNumericRule(rule.getPredicate().val(), rule.getValue().toString(), Iterables.toArray(extractedData, String.class));
                                        Sockets.sendMessageToSocket("analysis", "["+rule.getName()+"] "+valids + "/" + rule.getWindowSize() + " satisfy the condition");
                                        if (valids >= rule.getWindowSize()) {
                                            // Set the timestamp when the last alert has been triggered
                                            firedRulesXTimestamp.put(rule.getEca_ruleID(),System.currentTimeMillis());

                                            if (rule.getAction().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION)) {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for DYNAMIC_ADAPTATION");

                                                //TO DO: remember to replace

                                                DynamicAdaptationAlert.sendAlert(rule);
                                            } else {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for SOFTWARE_EVOLUTION");
                                                List<String> feedbacks = Lists.newArrayList();
                                                for (String json : data) {
                                                    Utils.extractFeatures(json,"http://www.BDIOntology.com/global/Feature/ratingFeedbacks/rating").forEach(element -> {
                                                        if (Double.parseDouble(element)<3) {
                                                            Utils.extractFeatures(json,"http://www.BDIOntology.com/global/Feature/textFeedbacks/text").forEach(e -> feedbacks.add(e));
                                                        }
                                                    });
                                                }
                                                //TO DO: remember to replace
                                                //SoftwareEvolutionAlert.sendAlert(Iterables.toArray(feedbacks,String.class));

                                            }
                                        }
                                        break;
                                    }
                                    //case FEEDBACK_CLASSIFIER_LABEL: {
                                    //    int valids = evaluateFeedbackRule(rule.getPredicate().val(), rule.getValue().toString(), Iterables.toArray(set._2(), String.class));
                                    //    Sockets.sendMessageToSocket("analysis", valids + "/" + rule.getWindowSize() + " satisfy the condition");
                                    //    if (valids >= rule.getWindowSize()) {
                                    //        if (rule.getAction().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION)) {
                                    //            Sockets.sendMessageToSocket("analysis", "Sending alert for DYNAMIC_ADAPTATION");
                                    //            DynamicAdaptationAlert.sendAlert(rule);
                                    //        } else {
                                    //            Sockets.sendMessageToSocket("analysis", "Sending alert for SOFTWARE_EVOLUTION");
                                    //            SoftwareEvolutionAlert.sendAlert(Iterables.toArray(set._2(), String.class));
                                    //        }
                                    //    }
                                    //    break;
                                    //}
                                }
                                //}
                            }
                        }
                    });
                System.out.println("#");
            });
**/
    }
}
