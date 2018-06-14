package eu.supersede.bdma.sa.stream_processes;

import com.google.common.collect.Iterables;
import eu.supersede.bdma.sa.eca_rules.SoftwareEvolutionAlert;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.mdm.types.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.api.java.JavaInputDStream;

import java.util.List;

/**
 * Created by snadal on 28/05/17.
 */
public class OneFeedbackOneAlert {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream,
                               Broadcast<List<Event>> events) {
        /*kafkaStream.foreachRDD(rdd -> {
            rdd.foreach(record -> {
                String JSON = record.value();
                Event evt = events.value().stream().filter(e -> e.getKafkaTopic().equals(record.topic())).findFirst().get();
                if (evt.getType().equals("feedback")) {
                    SoftwareEvolutionAlert.sendAlert(evt,Iterables.toArray(
                            Utils.extractFeatures(JSON,"Attributes/textFeedbacks/text"),String.class),
                            Utils.extractFeatures(JSON,"Attributes/applicationId").get(0));
                }
            });
        });*/
    }
}
