package eu.supersede.bdma.sa.stream_processes;

import com.google.common.collect.ImmutableMap;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.mdm.types.Event;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import scala.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by snadal on 4/06/17.
 */
public class FeedbackViewer {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream,
                               Broadcast<List<Event>> events) {
        List<String> feedbackEvents = events.getValue().
                stream().filter(e -> e.getType().equals("feedback")).
                map(e -> e.getKafkaTopic()).collect(Collectors.toList());

        kafkaStream.foreachRDD(rdd -> {
            rdd.foreach(tuple -> {
                if (feedbackEvents.contains(tuple.topic())) {
                    JSONObject obj = new JSONObject();
                    obj.put("kafkaTopic", tuple.topic());
                    obj.put("attribute","Feedback");
                    obj.put("iri","Attributes/textFeedbacks/text");
                    JSONArray arr = new JSONArray();
                    Utils.extractFeatures(tuple.value(),"Attributes/textFeedbacks/text").forEach(value -> {
                        if (!value.isEmpty()) {
                            arr.add(value);
                            obj.put("values",arr);
                        }
                    });
                    Sockets.sendSocketAlert(obj.toString(), "feedback_viewer");
                }
            });
        });

    }

}
