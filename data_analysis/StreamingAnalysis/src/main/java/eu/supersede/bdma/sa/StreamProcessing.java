package eu.supersede.bdma.sa;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.eca_rules.DynamicAdaptationAlert;
import eu.supersede.bdma.sa.eca_rules.SoftwareEvolutionAlert;
import eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition;
import eu.supersede.bdma.sa.eca_rules.conditions.TextCondition;
import eu.supersede.bdma.sa.proxies.MDMProxy;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.integration.api.mdm.types.ActionTypes;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import net.minidev.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;
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
import org.kie.internal.runtime.StatelessKnowledgeSession;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by snadal on 11/01/17.
 */
public class StreamProcessing {

    private static void sendMessageToSocket(String topic, String message) {
        JSONObject out = new JSONObject();
        out.put("topic",topic);
        out.put("message",message);
        try {
            // TODO use IF to send this message
            Sockets.sendSocketAlert(out.toString(),"raw_data");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static KnowledgePackage compilePkgDescr( PackageDescr pkg ) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newDescrResource( pkg ),
                ResourceType.DESCR );
        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        return kpkgs.iterator().next();
    }

    private static int evaluateNumericRule(String operator, String ruleValue, String[] values) {
        System.out.println("evaluateNumericRule("+operator+","+ruleValue+","+Arrays.toString(values));
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
            sendMessageToSocket("analysis","Extracted value ["+strNum+"]");
            ksession.insert(new DoubleCondition(Double.parseDouble(strNum)));
        }

        int nRules = ksession.fireAllRules();
        sendMessageToSocket("analysis",nRules + " satisfy the condition");

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
            sendMessageToSocket("analysis","Extracted value: "+str);
            //socketOut += "Extracted value ["+str+"]"+"\n";
            System.out.println("Classified as ["+label+"]");
            sendMessageToSocket("analysis","Classified as: "+label);
            //socketOut += "Classified as ["+label+"]"+"\n";
            ksession.insert(new TextCondition(label));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        //socketOut += nRules + " satisfy the condition"+"\n";

        return nRules;
    }


    private Map<String, Object> kafkaParams;

    // TODO Make Release serializable so it can be broadcast
    private Map<String, Tuple2<Boolean,String>> releases;
    // TODO Make ECA_Rule serializable so it can be broadcast
    private static List<ECA_Rule> rules;

    private static String dispatcher_path;

    public StreamProcessing() throws Exception {
        releases = MDMProxy.getReleasesIndexedPerKafkaTopic2();
        System.out.println(releases);

        kafkaParams = Maps.newHashMap();
        kafkaParams.put("bootstrap.servers", Main.properties.getProperty("BOOTSTRAP_SERVERS_CONFIG"));
        kafkaParams.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        kafkaParams.put("value.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        kafkaParams.put("group.id", Main.properties.getProperty("GROUP_ID"));
        kafkaParams.put("auto.offset.reset", Main.properties.getProperty("AUTO_OFFSET_RESET"));
        kafkaParams.put("enable.auto.commit", false);

        rules = MDMProxy.getRules();
        System.out.println("Have "+rules.size());

        dispatcher_path = Main.properties.getProperty("DISPATCHER_PATH");
        System.out.println("Dispatcher path "+dispatcher_path);
    }

    public void process(JavaSparkContext ctx, JavaStreamingContext streamCtx) throws Exception {
        // Broadcast variable to workers
        Broadcast<Map<String, Tuple2<Boolean,String>>> broadcastReleases = ctx.broadcast(releases);

        JavaInputDStream<ConsumerRecord<String, String>> kafkaStream =
                Utils.getKafkaStream(streamCtx, broadcastReleases.value().keySet(), this.kafkaParams);

        /**
         * 1: Send to Dispatcher if needed
         */
        kafkaStream.foreachRDD(records -> {
            final OffsetRange[] offsetRanges = ((HasOffsetRanges) records.rdd()).offsetRanges();
            records.foreachPartition(consumerRecords -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                consumerRecords.forEachRemaining(record -> {
                    // Dispatch always (For validation)
                    //if (broadcastReleases.value().get(o.topic())._1()) { //isDispatch?
                        // TODO Warning, using local FS methods. Must change for HDFS
                        try {
                            Files.append(record.value()+"\n", new File(dispatcher_path+o.topic()+".txt"), Charset.defaultCharset());
                            //Files.append(record.value()+"\n", new File(broadcastReleases.value().get(o.topic())._2()), Charset.defaultCharset());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    //}
                });
            });
        });

        /**
         * 2: Send the raw data to the Live Data Feed
         */
        kafkaStream.foreachRDD(records -> {
            final OffsetRange[] offsetRanges = ((HasOffsetRanges) records.rdd()).offsetRanges();
            records.foreachPartition(consumerRecords -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                consumerRecords.forEachRemaining(record -> {
                    System.out.println(record.value());
                    sendMessageToSocket(o.topic(),record.value());
                    /*
                    JSONObject out = new JSONObject();
                    out.put("topic",o.topic());
                    out.put("message",record.value());
                    try {
                        // TODO use IF to send this message
                        Sockets.sendSocketAlert(out.toString(),"raw_data");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    */
                });
            });
        });


        /**
         * 3: Evaluate rules
         */
        JavaPairDStream<String,String> window = kafkaStream.flatMapToPair(record -> {
            List<Tuple2<String,String>> out = Lists.newArrayList();
            rules.forEach(rule -> {
                String tuple = record.value().toString();
                Utils.extractFeatures(tuple,rule.getFeature()).forEach(extractedElement -> {
                    out.add(new Tuple2<String,String>(rule.getEca_ruleID(),extractedElement));
                });
            });
            return out.iterator();
        })
        .window(new Duration(300000),new Duration(15000));

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
                                    sendMessageToSocket("analysis",valids + "/"+ rule.getWindowSize()+" satisfy the condition");
                                    if (valids >= rule.getWindowSize()) {
                                        if (rule.getAction().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION)) {
                                            sendMessageToSocket("analysis","Sending alert for DYNAMIC_ADAPTATION");
                                            DynamicAdaptationAlert.sendAlert();
                                            Files.append("New alert!"+new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())+"\n", new File(dispatcher_path+"alerts"+".txt"), Charset.defaultCharset());

                                        } else {
                                            sendMessageToSocket("analysis","Sending alert for SOFTWARE_EVOLUTION");
                                            SoftwareEvolutionAlert.sendAlert(Iterables.toArray(set._2(), String.class));
                                            Files.append("New alert!"+new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())+"\n", new File(dispatcher_path+"alerts"+".txt"), Charset.defaultCharset());

                                        }
                                    }

                                    break;
                                }
                                case FEEDBACK_CLASSIFIER_LABEL: {
                                    int valids = evaluateFeedbackRule(rule.getPredicate().val(), rule.getValue().toString(), Iterables.toArray(set._2(), String.class));
                                    sendMessageToSocket("analysis",valids + "/"+ rule.getWindowSize()+" satisfy the condition");
                                    if (valids >= rule.getWindowSize()) {
                                        if (rule.getAction().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION)) {
                                            sendMessageToSocket("analysis","Sending alert for DYNAMIC_ADAPTATION");
                                            DynamicAdaptationAlert.sendAlert();
                                            Files.append("New alert!"+new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())+"\n", new File(dispatcher_path+"alerts"+".txt"), Charset.defaultCharset());

                                        } else {
                                            sendMessageToSocket("analysis","Sending alert for SOFTWARE_EVOLUTION");
                                            SoftwareEvolutionAlert.sendAlert(Iterables.toArray(set._2(), String.class));
                                            Files.append("New alert!"+new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())+"\n", new File(dispatcher_path+"alerts"+".txt"), Charset.defaultCharset());

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
