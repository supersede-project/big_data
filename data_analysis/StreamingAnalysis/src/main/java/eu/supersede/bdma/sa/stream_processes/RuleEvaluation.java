package eu.supersede.bdma.sa.stream_processes;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import eu.supersede.bdma.sa.eca_rules.DynamicAdaptationAlert;
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

    private static KnowledgePackage compilePkgDescr(PackageDescr pkg ) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newDescrResource( pkg ),
                ResourceType.DESCR );
        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        return kpkgs.iterator().next();
    }

    private static int evaluateNumericRule(String operator, String ruleValue, String[] values) {
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
            Sockets.sendMessageToSocket("analysis","Extracted value ["+strNum+"]");
            ksession.insert(new DoubleCondition(Double.parseDouble(strNum)));
        }

        int nRules = ksession.fireAllRules();
        Sockets.sendMessageToSocket("analysis",nRules + " satisfy the condition");

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

        //String socketOut = "";

        for (String str : values) {
            String label = feedbackClassifier.classify(path, new UserFeedback(str)).getLabel();
            System.out.println("Extracted value ["+str+"]");
            Sockets.sendMessageToSocket("analysis","Extracted value: "+str);
            //socketOut += "Extracted value ["+str+"]"+"\n";
            System.out.println("Classified as ["+label+"]");
            Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            //socketOut += "Classified as ["+label+"]"+"\n";
            ksession.insert(new TextCondition(label));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        //socketOut += nRules + " satisfy the condition"+"\n";

        return nRules;
    }


    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream, List<ECA_Rule> rules) {

        JavaPairDStream<String, String> window = kafkaStream.flatMapToPair(record -> {
            List<Tuple2<String, String>> out = Lists.newArrayList();
            rules.forEach(rule -> {
                String tuple = record.value().toString();
                Utils.extractFeatures(tuple, rule.getFeature()).forEach(extractedElement -> {
                    out.add(new Tuple2<String, String>(rule.getEca_ruleID(), extractedElement));
                });
            });
            return out.iterator();
        })
                .window(new Duration(300000), new Duration(15000));

        window.print();

        window.groupByKey()
            .foreachRDD(records -> {
                    records.foreach(set -> {
                        for (ECA_Rule rule : rules) {
                            // if the data is for that rule
                            if (set._1().equals(rule.getEca_ruleID())) {
                                //for (String tuple : set._2()) {
                                switch (rule.getOperator()) {
                                    case VALUE: {
                                        int valids = evaluateNumericRule(rule.getPredicate().val(), rule.getValue().toString(), Iterables.toArray(set._2(), String.class));
                                        Sockets.sendMessageToSocket("analysis", valids + "/" + rule.getWindowSize() + " satisfy the condition");
                                        if (valids >= rule.getWindowSize()) {
                                            if (rule.getAction().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION)) {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for DYNAMIC_ADAPTATION");
                                                DynamicAdaptationAlert.sendAlert();
                                                //Files.append("New alert!" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + "\n", new File(dispatcher_path + "alerts" + ".txt"), Charset.defaultCharset());

                                            } else {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for SOFTWARE_EVOLUTION");
                                                SoftwareEvolutionAlert.sendAlert(Iterables.toArray(set._2(), String.class));
                                                //Files.append("New alert!" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + "\n", new File(dispatcher_path + "alerts" + ".txt"), Charset.defaultCharset());

                                            }
                                        }

                                        break;
                                    }
                                    case FEEDBACK_CLASSIFIER_LABEL: {
                                        int valids = evaluateFeedbackRule(rule.getPredicate().val(), rule.getValue().toString(), Iterables.toArray(set._2(), String.class));
                                        Sockets.sendMessageToSocket("analysis", valids + "/" + rule.getWindowSize() + " satisfy the condition");
                                        if (valids >= rule.getWindowSize()) {
                                            if (rule.getAction().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION)) {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for DYNAMIC_ADAPTATION");
                                                DynamicAdaptationAlert.sendAlert();
                                                //Files.append("New alert!" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + "\n", new File(dispatcher_path + "alerts" + ".txt"), Charset.defaultCharset());

                                            } else {
                                                Sockets.sendMessageToSocket("analysis", "Sending alert for SOFTWARE_EVOLUTION");
                                                SoftwareEvolutionAlert.sendAlert(Iterables.toArray(set._2(), String.class));
                                                //Files.append("New alert!" + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()) + "\n", new File(dispatcher_path + "alerts" + ".txt"), Charset.defaultCharset());

                                            }
                                        }

                                        break;
                                    }
                                }
                                //}
                            }
                        }
                    });
                });
    }
}
