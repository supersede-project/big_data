package eu.supersede.bdma.sa.stream_processes;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Duration;
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

    private static Map<String, Long> firedRulesXTimestamp = Maps.newConcurrentMap();

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


    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream, Broadcast<Map<String, Tuple2<Boolean,String>>> broadcastReleases,
                               List<SerializableECA_Rule> rules, String evo_adapt) {
        for (SerializableECA_Rule r : rules) {
            firedRulesXTimestamp.put(r.getEca_ruleID(), Long.valueOf(0));
        }

        JavaPairDStream<String, Tuple2<String,Long>> theStream = kafkaStream.
                filter(record -> !record.value().isEmpty()).
                flatMapToPair(record -> {
            List<Tuple2<String, Tuple2<String,Long>>> out = Lists.newArrayList();
            rules.forEach(rule -> {
                if (rule.getKafkaTopic().equals(record.topic())) {
                    // Filter for feedback demo, not to interfere with other users


                    String tuple = record.value().toString();
                    if (Utils.extractFeatures(tuple, rule.getFeature()) != null) {
                        out.add(new Tuple2<String, Tuple2<String, Long>>(rule.getEca_ruleID(), new Tuple2<String, Long>(tuple, System.currentTimeMillis())));
                    }
                }
            });
            System.out.println("Extracted "+out.toString());
            return out.iterator();
        }).window(new Duration(evo_adapt.equals("evolution") ? 7200000 : 300000/*5 min*/), new Duration(5000));

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
                                                SoftwareEvolutionAlert.sendAlert(Iterables.toArray(feedbacks,String.class));
                                            }
                                        }
                                        break;
                                    }
                                    /*case FEEDBACK_CLASSIFIER_LABEL: {
                                        int valids = evaluateFeedbackRule(rule.getPredicate().val(), rule.getValue().toString(), Iterables.toArray(set._2(), String.class));
                                        Sockets.sendMessageToSocket("analysis", valids + "/" + rule.getWindowSize() + " satisfy the condition");
                                        if (valids >= rule.getWindowSize()) {
                                            if (rule.getAction().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION)) {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for DYNAMIC_ADAPTATION");
                                                DynamicAdaptationAlert.sendAlert(rule);
                                            } else {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for SOFTWARE_EVOLUTION");
                                                SoftwareEvolutionAlert.sendAlert(Iterables.toArray(set._2(), String.class));
                                            }
                                        }

                                        break;
                                    }*/
                                }
                                //}
                            }
                        }
                    });
                System.out.println("###############################################");

            });

    }
}
