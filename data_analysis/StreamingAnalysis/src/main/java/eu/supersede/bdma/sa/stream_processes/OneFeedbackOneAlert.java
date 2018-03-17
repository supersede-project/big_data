package eu.supersede.bdma.sa.stream_processes;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.eca_rules.DynamicAdaptationAlert;
import eu.supersede.bdma.sa.eca_rules.FeedbackReconfigurationAlert;
import eu.supersede.bdma.sa.eca_rules.MonitorReconfigurationAlert;
import eu.supersede.bdma.sa.eca_rules.SoftwareEvolutionAlert;
import eu.supersede.bdma.sa.eca_rules.conditions.ConditionEvaluator;
import eu.supersede.bdma.sa.utils.MonitorReconfigurationJSON;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.mdm.types.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

/**
 * Created by snadal on 28/05/17.
 */
public class OneFeedbackOneAlert {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream,
                               Broadcast<List<Event>> events) {
        kafkaStream.foreachRDD(rdd -> {
            rdd.foreach(record -> {
                String JSON = record.value();
                Event evt = events.value().stream().filter(e -> e.getKafkaTopic().equals(record.topic())).findFirst().get();
                if (evt.getType().equals("feedback")) {
                    SoftwareEvolutionAlert.sendAlert(evt,Iterables.toArray(
                            Utils.extractFeatures(JSON,"Attributes/textFeedbacks/text"),String.class));
                }
            });
        });
    }
}
