package eu.supersede.bdma.sa.usecase_specific;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.hadoop.conf.Configuration;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
//import org.apache.spark.streaming.kafka.KafkaUtils;
//import org.json.JSONObject;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.utils.Sockets;
import scala.Tuple2;


/**
 * Created by snadal on 26/09/16.
 */
public class Siemens_SA {
/*
    final static Logger logger = LogManager.getLogger(Siemens_SA.class);

    private static Map<String,String> toProcess;

    private static long TOTAL_API_CALLS;
    private static long TOTAL_SUCCESSFUL_API_CALLS;
    private static long TOTAL_UNSUCCESSFUL_API_CALLS;
    private static HashMap<String, Tuple2<Integer, Integer>> breakdownPerAPI; // _1 (succ), _2 (unsucc)

    public static void init() throws Exception {
        topicName = Main.properties.getProperty("siemens_topic");
        consumerGroup = Main.properties.getProperty("siemens_consumer_group");
        toProcess = Maps.newHashMap();

        TOTAL_API_CALLS = 0;
        TOTAL_SUCCESSFUL_API_CALLS = 0;
        TOTAL_UNSUCCESSFUL_API_CALLS = 0;
        breakdownPerAPI = Maps.newHashMap();
    }

    public static void process(JavaStreamingContext streamContext) throws Exception {
        Map<String, Integer> topicMap = Maps.newHashMap();
        topicMap.put(topicName, 1);

        JavaPairReceiverInputDStream<String, String> kafkaStream =
                KafkaUtils.createStream(streamContext, Main.properties.getProperty("zk_quorum"), consumerGroup, topicMap);

        //0) Send the whole message

        kafkaStream.foreachRDD(arg0 -> {
            for (Tuple2<String, String> t : arg0.collect()) {
                Sockets.sendSocketAlert(t._2, "siemens/full_message");
            }
        });
        JavaPairDStream<String, String> pairsBySessionId = kafkaStream.mapPartitionsToPair(arg0 -> {
            List<Tuple2<String, String>> res = Lists.newArrayList();
            while (arg0.hasNext()) {
                String line = arg0.next()._2;
                res.add(new Tuple2<String, String>(line.split("\\|")[4].trim(), line));
            }
            return res.iterator();
        });

        pairsBySessionId.foreachRDD(arg0 -> {
            for (Tuple2<String, String> t : arg0.collect()) {
                for (String name : toProcess.keySet()) {
                    String key = name.toString();
                    String value = toProcess.get(name).toString();
                }
                if (toProcess.containsKey(t._1)) {
                    String oldT = toProcess.get(t._1);
                    String newT = t._2;
                    toProcess.remove(t._1);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss,SSS");
                    Date requestDate = dateFormat.parse(oldT.split("\\|")[0].trim());
                    Date responseDate = dateFormat.parse(newT.split("\\|")[0].trim());

                    String API = oldT.split("\\|")[7].trim();
                    long time = responseDate.getTime() - requestDate.getTime();
                    String result = newT.split("\\|")[9].trim();

                    ++TOTAL_API_CALLS;

                    if (!breakdownPerAPI.containsKey(API)) {
                        breakdownPerAPI.put(API, new Tuple2<Integer, Integer>(0, 0));
                    }

                    if (newT.contains("SUCCESS")) {
                        ++TOTAL_SUCCESSFUL_API_CALLS;
                        breakdownPerAPI.put(API, new Tuple2<Integer, Integer>(breakdownPerAPI.get(API)._1 + 1, breakdownPerAPI.get(API)._2));
                    } else {
                        ++TOTAL_UNSUCCESSFUL_API_CALLS;
                        breakdownPerAPI.put(API, new Tuple2<Integer, Integer>(breakdownPerAPI.get(API)._1, breakdownPerAPI.get(API)._2 + 1));
                    }

                    Sockets.sendSocketAlert(String.valueOf(TOTAL_API_CALLS), "siemens/total_api_calls");
                    Sockets.sendSocketAlert(String.valueOf(TOTAL_SUCCESSFUL_API_CALLS), "siemens/total_successful_api_calls");
                    Sockets.sendSocketAlert(String.valueOf(TOTAL_UNSUCCESSFUL_API_CALLS), "siemens/total_unsuccessful_api_calls");

                    JSONObject objBreakdown = new JSONObject();
                    for (String key : breakdownPerAPI.keySet()) {
                        JSONObject results = new JSONObject();
                        results.put("succesful", breakdownPerAPI.get(key)._1);
                        results.put("unsuccesful", breakdownPerAPI.get(key)._2);
                        objBreakdown.put(key, results);
                    }

                    Sockets.sendSocketAlert(objBreakdown.toString(), "siemens/breakdown_per_api");

                    JSONObject plotAllAPIs = new JSONObject();
                    plotAllAPIs.put("total_suc", String.valueOf(TOTAL_SUCCESSFUL_API_CALLS));
                    plotAllAPIs.put("total_unsuc", String.valueOf(TOTAL_UNSUCCESSFUL_API_CALLS));
                    Sockets.sendSocketAlert(plotAllAPIs.toString(), "siemens/plot_all_APIs");
                } else {
                    toProcess.put(t._1, t._2);
                }
            }

            if (valuesOutOfThresholds()) {
                resetValues();
            }
        });

    }

    private static boolean valuesOutOfThresholds() {
        return TOTAL_API_CALLS >= 500;
    }

    private static void resetValues() {
        toProcess = Maps.newHashMap();

        TOTAL_API_CALLS = 0;
        TOTAL_SUCCESSFUL_API_CALLS = 0;
        TOTAL_UNSUCCESSFUL_API_CALLS = 0;
        breakdownPerAPI = Maps.newHashMap();
    }
*/
}