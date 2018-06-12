package eu.supersede.bdma.sa.stream_processes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.adaptation.types.Tenant;
import eu.supersede.integration.api.mdm.types.Event;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import scala.Tuple2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SiemensAPIViewer {

    private static String SIEMENS_MONITOR_TOPIC = "0d740ce6-b364-45f9-9d35-35b870c71a98";

    private static Map<String,JSONObject> toProcess = Maps.newHashMap();
    //private static Map<String, Tuple2<Integer, Integer>> breakdownPerAPI = Maps.newHashMap(); // _1 (succ), _2 (unsucc)

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
        kafkaStream.filter(t -> t.topic().equals(SIEMENS_MONITOR_TOPIC))
                .map(t -> t.value())
                .map(t -> ((JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) JSONValue.parse(t)).get("JSONFiles")).get("DataItems")).get(0))/*.getAsString("message")*/)
                .mapToPair(t -> new Tuple2<>(t.getAsString("message").split("\\|")[2].trim(),t/*t.split("\\|")[4].trim()*/))
                .foreachRDD(rdd -> {
                    rdd.foreach(t -> {
                        if (toProcess.containsKey(t._1)) {
                            JSONObject oldT = toProcess.get(t._1);
                            JSONObject newT = t._2;
                            toProcess.remove(t._1);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss,SSS");
                            Date requestDate = dateFormat.parse(oldT.getAsString("Date"));
                            Date responseDate = dateFormat.parse(newT.getAsString("Date"));

                            String API = "others";
                            if (oldT.toString().contains("buildings")) API = "getBuildings";
                            else if (oldT.toString().contains("minmax")) API = "getMinMaxDates";
                            else if (oldT.toString().contains("types")) API = "getTypes";

                            //String API = oldT.getAsString("class_name");//.split("\\|")[7].trim();
                            long time = responseDate.getTime()-requestDate.getTime();

                            JSONObject obj = new JSONObject();
                            obj.put("attribute",API);
                            JSONArray arr = new JSONArray();
                            arr.add(time);
//                            values.forEach(v -> arr.add(v));
                            obj.put("values",arr);

                            Sockets.sendSocketAlert(obj.toString(), "breakdown_per_api");
                            //String result = newT.split("\\|")[9].trim();

                            /*if (!breakdownPerAPI.containsKey(API)) {
                                breakdownPerAPI.put(API, new Tuple2<Integer, Integer>(0, 0));
                            }

                            if (newT.toString().contains("SUCCESS")) {
                                breakdownPerAPI.put(API, new Tuple2<Integer,Integer>(breakdownPerAPI.get(API)._1+1,breakdownPerAPI.get(API)._2));
                            }
                            else {
                                breakdownPerAPI.put(API, new Tuple2<Integer,Integer>(breakdownPerAPI.get(API)._1,breakdownPerAPI.get(API)._2+1));
                            }*/
                            /*
                            JSONObject objBreakdown = new JSONObject();
                            for (String key : breakdownPerAPI.keySet()) {
                                JSONObject results = new JSONObject();
                                results.put("succesful",breakdownPerAPI.get(key)._1);
                                results.put("unsuccesful",breakdownPerAPI.get(key)._2);
                                objBreakdown.put(key, results);
                            }
                            System.out.println("Sending "+objBreakdown.toString());
                            */
                            //Sockets.sendSocketAlert(objBreakdown.toString(), "breakdown_per_api");
                        } else {
                            toProcess.put(t._1, t._2);
                        }
                    });
                });

    }

}
