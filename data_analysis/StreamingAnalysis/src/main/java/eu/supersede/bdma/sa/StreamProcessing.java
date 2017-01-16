package eu.supersede.bdma.sa;

import com.google.common.collect.Maps;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
//import eu.supersede.integration.api.dm.proxies.DecisionMakingSystemProxy;
import eu.supersede.integration.api.dm.types.Alert;
import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;
import org.json.JSONObject;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;

import java.util.Collection;
import java.util.Map;

/**
 * Created by snadal on 11/01/17.
 */
public class StreamProcessing {

    //final Logger logger;

    private Map<String, Object> kafkaParams;
    private Collection<String> topics;

    //private DecisionMakingSystemProxy proxy;

    public StreamProcessing() throws Exception {
        topics = MDMProxy.getKafkaTopics();

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

    public void process(JavaStreamingContext streamContext) throws Exception {
        JavaInputDStream<ConsumerRecord<String, String>> kafkaStream = Utils.getKafkaStream(streamContext, this.topics, this.kafkaParams);

        /**
         * 1: Send the raw data to the Live Data Feed
         */
        kafkaStream.foreachRDD(records -> {
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
