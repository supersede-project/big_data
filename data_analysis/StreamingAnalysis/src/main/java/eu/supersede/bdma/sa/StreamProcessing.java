package eu.supersede.bdma.sa;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import eu.supersede.bdma.sa.statisticalAnalysis.FeedbackAnalysis;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
//import eu.supersede.integration.api.dm.proxies.DecisionMakingSystemProxy;
import eu.supersede.integration.api.dm.types.Alert;
import eu.supersede.integration.api.mdm.types.Release;
import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.json.JSONObject;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.runtime.StatelessKnowledgeSession;
import org.kie.internal.utils.KieService;
import scala.Tuple1;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by snadal on 11/01/17.
 */
public class StreamProcessing {

    public class MyClass {
        public MyClass(int t) { this.t = t ;}
        public int t;
    }

    private static KnowledgePackage compilePkgDescr( PackageDescr pkg ) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newDescrResource( pkg ),
                ResourceType.DESCR );
        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        return kpkgs.iterator().next();
    }

    //final Logger logger;

    private Map<String, Object> kafkaParams;
    private Map<String, Tuple2<Boolean,String>> releases;

    //private DecisionMakingSystemProxy proxy;

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

        //logger = LogManager.getLogger(StreamProcessing.class);

        //proxy = new DecisionMakingSystemProxy();
    }

    public void process(JavaSparkContext ctx, JavaStreamingContext streamCtx) throws Exception {
        // Broadcast variable to workers
        Broadcast<Map<String, Tuple2<Boolean,String>>> broadcastReleases = ctx.broadcast(releases);

        JavaInputDStream<ConsumerRecord<String, String>> kafkaStream =
                Utils.getKafkaStream(streamCtx, broadcastReleases.value().keySet(), this.kafkaParams);

        /**
         * 1: Send to Dispatcher if needed
         */
        /*
        kafkaStream.foreachRDD(records -> {
            final OffsetRange[] offsetRanges = ((HasOffsetRanges) records.rdd()).offsetRanges();
            records.foreachPartition(consumerRecords -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                consumerRecords.forEachRemaining(record -> {
                    if (broadcastReleases.value().get(o.topic())._1()) { //isDispatch?
                        // TODO Warning, using local FS methods. Must change for HDFS
                        try {
                            Files.append(record.value()+"\n", new File(broadcastReleases.value().get(o.topic())._2()), Charset.defaultCharset());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
        });
        */

        /**
         * 2: Send the raw data to the Live Data Feed
         */
        /*kafkaStream.foreachRDD(records -> {
            final OffsetRange[] offsetRanges = ((HasOffsetRanges) records.rdd()).offsetRanges();
            records.foreachPartition(consumerRecords -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                consumerRecords.forEachRemaining(record -> {
                    JSONObject out = new JSONObject();
                    out.put("topic",o.topic());
                    out.put("message",record.value());
                    try {
                        // TODO use IF to send this message
                        Sockets.sendSocketAlert(out.toString(),"raw_data");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        });
        */

        /**
         * 3: Evaluate rules
         */

        kafkaStream.foreachRDD(records -> {
            records.foreach(record -> {

                System.out.println("Received "+(record.value().length()>125 ? record.value().substring(0,125) : record.value()));
                System.out.println(FeedbackAnalysis.getFeedbackAnalysisSummary(record.value()).getSummary());

                //if (new JSONObject(record.value()).has("TV")) {

                    PackageDescr pkg =
                            DescrFactory.newPackage()
                                    .name("testPkg")
                                    .newRule().name("testRule")
                                    .lhs()
                                    .pattern("eu.supersede.bdma.sa.SergiClass").constraint("x > 10").end()
                                    .end()
                                    //.rhs("System.out.println(\"Condition satisfied, sending SOFTWARE_EVOLUTION alert to WP3\");")
                                    .rhs("eu.supersede.bdma.sa.rules.Action.sendAlert();")
                                    .end()
                                    .getDescr();

        /*KieServices kieServices = KieServices.Factory.get();
        KieResources kieResources = kieServices.getResources();
        KieRepository kieRepository = kieServices.getRepository();

        Resource resource = kieResources. newDescrResource(pkg);
        kieRepository.addKieModule(resource);

        KieContainer kContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        StatelessKieSession ksession = kContainer.newStatelessKieSession();*/

                    KnowledgePackage kpkg = compilePkgDescr(pkg);
                    KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
                    kbase.addKnowledgePackages(Collections.singleton(kpkg));
                    StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
                    ksession.getGlobals().set("param","value");
                    //eu.supersede.bdma.sa.SergiClass x = new eu.supersede.bdma.sa.SergiClass((new JSONObject(record.value()).getInt("All")));
                    eu.supersede.bdma.sa.SergiClass x = new eu.supersede.bdma.sa.SergiClass(Integer.parseInt(record.value()));

                    ksession.execute(x);
                //}
            });
        });

        //KieContainer kContainer = KieRepository

        /*kafkaStream.foreachRDD(records -> {
            records.foreach(record -> {
                int x = Integer.parseInt(record.value());
                if (x > 5) {
                    Alert alert = new Alert();
                    //alert.
                }
            });
        });*/
    }
}
