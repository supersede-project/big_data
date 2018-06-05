package eu.supersede.bdma.sa.stream_processes;

import com.google.common.collect.ImmutableMap;
import eu.supersede.bdma.sa.eca_rules.SerializableECA_Rule;
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

/**
 * Created by snadal on 4/06/17.
 */
public class DataSourceStatistics {

    private static Map<String, String> catalogOfStatistics = ImmutableMap.<String, String>builder().
            put("Attributes/HttpMonitoredData/DataItems/responseTime", "Response Time").
            put("Attributes/HttpMonitoredData/DataItems/responseCode", "Response Code").
            build();


    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
        // Send the data
        kafkaStream.mapToPair(record ->new Tuple2<String,String>(record.topic(),record.value()))
                .groupByKey()
                .foreachRDD(rdd -> {
                    //rdd.take(1).
                    rdd.take(1).forEach(t -> {
                        System.out.println("sending to socket "+t);

                        for (String JSON : t._2()) {
                            for (String iri : catalogOfStatistics.keySet()) {
                                List<String> values = Utils.extractFeatures(JSON, iri);
                                if (!values.isEmpty()) {
                                    JSONObject obj = new JSONObject();
                                    obj.put("kafkaTopic", t._1());
                                    obj.put("attribute",catalogOfStatistics.get(iri));
                                    obj.put("iri",iri);
                                    JSONArray arr = new JSONArray();
                                    values.forEach(v -> arr.add(v));
                                    obj.put("values",arr);
                                    try {
                                        Sockets.sendSocketAlert(obj.toString(), "socket_data_source_statistics");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                });
    }

}
