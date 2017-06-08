package eu.supersede.bdma.sa.stream_processes;

import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import net.minidev.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;

/**
 * Created by snadal on 28/05/17.
 */
public class RawDataToLiveFeed {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
        kafkaStream.foreachRDD(records -> {
            final OffsetRange[] offsetRanges = ((HasOffsetRanges) records.rdd()).offsetRanges();
            records.foreachPartition(consumerRecords -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                consumerRecords.forEachRemaining(record -> {
                    System.out.println(record.value());
                    if (!record.value().trim().isEmpty()) {
                        Sockets.sendMessageToSocket(o.topic(), record.value());
                    }
                    //Sockets.sendMessageToSocket(o.topic(),Utils.extractFeatures(record.value(), "http://www.BDIOntology.com/global/Feature/textFeedbacks/text").get(0));
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

    }

}