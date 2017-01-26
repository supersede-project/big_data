package eu.supersede.bdma.sa.usecase_specific;

import java.util.Map;

import eu.supersede.bdma.sa.StreamingAnalysis;
//import eu.supersede.integration.api.dm.proxies.DecisionMakingSystemProxy;
//import eu.supersede.integration.api.dm.types.Alert;
//import eu.supersede.integration.api.dm.types.AlertLevel;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
//import org.json.JSONObject;

import com.google.common.collect.Maps;

import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.utils.Sockets;
import scala.Tuple2;

/**
 * Created by snadal on 26/09/16.
 */
public class Atos_SA extends StreamingAnalysis {

/*    final static Logger logger = LogManager.getLogger(Atos_SA.class);

    private static int MAX_USERS = 100;
    private static long CURRENT_USERS;
    private static long CURRENT_TIMESTAMP;
    private static DecisionMakingSystemProxy proxy;

    public static void init() throws Exception {
        topicName = Main.properties.getProperty("atos_topic");
        consumerGroup = Main.properties.getProperty("atos_consumer_group");
        CURRENT_USERS = 0;
        proxy = new DecisionMakingSystemProxy();
    }

    public static void process(JavaStreamingContext streamContext) throws Exception {

        Map<String, Integer> topicMap = Maps.newHashMap();
        topicMap.put(topicName, 1);

        JavaPairReceiverInputDStream<String, String> kafkaStream =
                KafkaUtils.createStream(streamContext,Main.properties.getProperty("zk_quorum"),consumerGroup,topicMap);

        //0) Send the whole message
        kafkaStream.foreachRDD(arg0 -> {
            for (Tuple2<String,String> t : arg0.collect()) {
                JSONObject obj = new JSONObject(t._2);
                JSONObject newObj = new JSONObject();

                newObj.put("deviceID", obj.getString("deviceID"));
                newObj.put("timestamp", obj.getString("timestamp"));
                newObj.put("playbackErrorVideo", obj.has("playbackErrorVideo") ? obj.getString("playbackErrorVideo") : "-");
                newObj.put("rebufferingVideo", obj.has("rebufferingVideo") ? obj.getLong("rebufferingVideo") : "0");
                newObj.put("syncAttemps", obj.has("syncAttemps") ? obj.getLong("syncAttemps") : "0");

                Sockets.sendSocketAlert(newObj.toString(), "atos/fullMessage");

                //Test alert raising

                //Alert alert = new Alert(AlertLevel.Info, "1", newObj.toString(), 10.0);
                //proxy.notifyAlert(alert);
            }
        });


        //1) DeviceID Detectar una audiencia superior al 85% de un 1000000.

        JavaPairDStream<String, Integer> deviceIds = kafkaStream.mapToPair(arg0 -> {
            JSONObject event = new JSONObject(arg0._2);
            String key = event.getString("deviceID");
            return new Tuple2<String,Integer>(key,1);
        });
        JavaPairDStream<String, Integer> counts = deviceIds.reduceByKeyAndWindow(
                new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer i1, Integer i2) {
                        return i1 + i2;
                    }
                }, new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer i1, Integer i2) {
                        return i1 - i2;
                    }
                }, new Duration(60 * 5 * 1000), new Duration(1 * 1000))
                .filter(new Function<Tuple2<String,Integer>, Boolean>() {
                    @Override
                    public Boolean call(Tuple2<String, Integer> arg0) throws Exception {
                        return arg0._2 > 0;
                    }
                });
        counts.count().foreachRDD(arg0 -> {
            CURRENT_USERS = arg0.collect().get(0);
            Sockets.sendSocketAlert(String.valueOf(CURRENT_USERS), "atos/distinctDevices");
        });

        //2) Detectar que un 20% de los usuarios (DeviceID distintos en un periodo de tiempo) tienen algun problema de PlaybackErrorVideo.
        JavaPairDStream<String, Integer> deviceIdsWithPlaybackErrorVideo = kafkaStream.filter(arg0 -> {
            return new JSONObject(arg0._2).has("playbackErrorVideo");
        }).mapToPair(arg0 -> {
            JSONObject event = new JSONObject(arg0._2);
            String key = event.getString("deviceID");
            return new Tuple2<String,Integer>(key,1);
        });
        JavaPairDStream<String, Integer> playbackErrorVideoCounts = deviceIdsWithPlaybackErrorVideo.reduceByKeyAndWindow(
                new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer i1, Integer i2) {
                        return i1 + i2;
                    }
                }, new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer i1, Integer i2) {
                        return i1 - i2;
                    }
                }, new Duration(60 * 5 * 1000), new Duration(1 * 1000)).
                filter(new Function<Tuple2<String,Integer>, Boolean>() {
                    @Override
                    public Boolean call(Tuple2<String, Integer> arg0) throws Exception {
                        return arg0._2 > 0;
                    }
                });
        playbackErrorVideoCounts.count().foreachRDD(arg0 -> {
            long countDeviceIdsWithPlaybackErrorVideo = arg0.collect().get(0);
            Sockets.sendSocketAlert(String.valueOf(countDeviceIdsWithPlaybackErrorVideo), "atos/playbackErrorVideo");
        });

        //3) Detectar que un 20% de los usuarios (DeviceID distintos en un periodo de tiempo) tienen algun problema de Rebuffering con valor > 2.
        JavaPairDStream<String, Integer> deviceIdsWithRebuffering = kafkaStream.filter(arg0 -> {
            JSONObject event = new JSONObject(arg0._2);
            return event.has("rebufferingVideo");
        }).mapToPair(arg0 -> {
            JSONObject event = new JSONObject(arg0._2);
            String key = event.getString("deviceID");
            return new Tuple2<String,Integer>(key,1);
        });
        JavaPairDStream<String, Integer> rebufferingVideoCounts = deviceIdsWithRebuffering.reduceByKeyAndWindow(
                new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer i1, Integer i2) {
                        return i1 + i2;
                    }
                }, new Function2<Integer, Integer, Integer>() {
                    public Integer call(Integer i1, Integer i2) {
                        return i1 - i2;
                    }
                }, new Duration(60 * 5 * 1000), new Duration(1 * 1000)).
                filter(new Function<Tuple2<String,Integer>, Boolean>() {
                    @Override
                    public Boolean call(Tuple2<String, Integer> arg0) throws Exception {
                        return arg0._2 > 0;
                    }
                });
        deviceIdsWithRebuffering.count().foreachRDD(arg0 -> {
            long countDeviceIdsWithRebuffering = arg0.collect().get(0);
            Sockets.sendSocketAlert(String.valueOf(countDeviceIdsWithRebuffering), "atos/rebuffering");
        });

        //4) SyncAttemp - Detectar outliers en cada periodo (Espacios de 5m.)

        JavaDStream<Vector> syncAttemptWindow = kafkaStream.map(arg0 -> {
            JSONObject obj = new JSONObject(arg0._2);
            return obj.has("syncAttemps") ? Vectors.dense(Double.valueOf(obj.getLong("syncAttemps")+"")) : Vectors.dense(0);
        }).window(new Duration(60 * 5 * 1000), new Duration(1000));//.print();

        syncAttemptWindow.foreachRDD(arg0 -> {
            MultivariateStatisticalSummary summary = Statistics.colStats(arg0.rdd());
            Sockets.sendSocketAlert(String.valueOf(summary.mean().toArray()[0]), "atos/meanSyncAttempt");
            Sockets.sendSocketAlert(String.valueOf(Math.sqrt(summary.mean().toArray()[0])), "atos/stDevSyncAttempt");
        });

    }
*/
}
