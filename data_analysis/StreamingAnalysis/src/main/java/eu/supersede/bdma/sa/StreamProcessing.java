package eu.supersede.bdma.sa;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.eca_rules.DynamicAdaptationAlert;
import eu.supersede.bdma.sa.eca_rules.SerializableECA_Rule;
import eu.supersede.bdma.sa.eca_rules.SoftwareEvolutionAlert;
import eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition;
import eu.supersede.bdma.sa.eca_rules.conditions.TextCondition;
import eu.supersede.bdma.sa.proxies.MDMProxy;
import eu.supersede.bdma.sa.stream_processes.*;
import eu.supersede.bdma.sa.tests.WindowTests;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.integration.api.mdm.types.ActionTypes;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import eu.supersede.integration.api.mdm.types.Event;
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
import java.util.stream.Collectors;

/**
 * Created by snadal on 11/01/17.
 */
public class StreamProcessing {

    private Map<String, Object> kafkaParams;
    // TODO Make Release serializable so it can be broadcast
    //private Map<String, Tuple2<Boolean,String>> releases;
    private List<Event> events;
    // TODO Make ECA_Rule serializable so it can be broadcast
    private List<ECA_Rule> rules;

    private static String dispatcher_path;

    public StreamProcessing() throws Exception {
        //releases = MDMProxy.getReleasesIndexedPerKafkaTopic2(evo_adapt);
        events = MDMProxy.getAllEvents();

        kafkaParams = Maps.newHashMap();
        kafkaParams.put("bootstrap.servers", Main.properties.getProperty("BOOTSTRAP_SERVERS_CONFIG"));
        kafkaParams.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        kafkaParams.put("value.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        kafkaParams.put("group.id", Main.properties.getProperty("GROUP_ID"));
        kafkaParams.put("auto.offset.reset", Main.properties.getProperty("AUTO_OFFSET_RESET"));
        kafkaParams.put("enable.auto.commit", false);

        rules = MDMProxy.getRules();

        System.out.println("####################################");
        System.out.println("Events");
        System.out.println("####################################");
        events.forEach(e -> System.out.print(e.getEvent() + "(" + e.getKafkaTopic() + ")" + ", "));
        System.out.println();
        System.out.println("####################################");
        System.out.println("Rules");
        System.out.println("####################################");
        rules.forEach(r -> System.out.print(r.getName() + "("+r.getEvent().getKafkaTopic()+"), "));
        System.out.println();
    }

    public void process(JavaSparkContext ctx, JavaStreamingContext streamCtx) throws Exception {
        // Broadcast variable to workers
        Broadcast<List<Event>> broadcastEvents = ctx.broadcast(events);
        Broadcast<List<ECA_Rule>> broadcastRules = ctx.broadcast(rules);

        JavaInputDStream<ConsumerRecord<String, String>> kafkaStream =
                Utils.getKafkaStream(streamCtx, Sets.newHashSet(events.stream().map(e -> e.getKafkaTopic()).collect(Collectors.toList())), this.kafkaParams);

        if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_PRINT_STREAM_TO_STDOUT"))) {
            System.out.println("LAUNCH_PRINT_STREAM_TO_STDOUT");
            PrintStreamToStdout.process(kafkaStream);
        }
        if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_DISPATCHER"))) {
            System.out.println("LAUNCH_DISPATCHER");
            Dispatcher.process(kafkaStream);
        }
        if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_DATA_SOURCE_STATISTICS"))) {
            System.out.println("LAUNCH_DATA_SOURCE_STATISTICS");
            DataSourceStatistics.process(kafkaStream);
        }
        if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_GENERIC_STREAM_STATISTICS"))) {
            System.out.println("LAUNCH_GENERIC_STREAM_STATISTICS");
            GenericStreamStatistics.process(kafkaStream,broadcastEvents);
        }
        if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_RAW_DATA_TO_LIVE_FEED"))) {
            System.out.println("LAUNCH_RAW_DATA_TO_LIVE_FEED");
            RawDataToLiveFeed.process(kafkaStream);
        }
        if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_RULE_EVALUATION"))) {
            System.out.println("LAUNCH_RULE_EVALUATION");
            RuleEvaluation.process(kafkaStream,broadcastEvents,broadcastRules/*,evo_adapt*/);
        }
        if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_FG_RECONFIGURATION"))) {
            System.out.println("LAUNCH_FG_RECONFIGURATION");
            FGReconfiguration.process(ctx);
        }

    }
}
