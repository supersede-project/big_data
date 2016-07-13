package eu.supersede.bdma.validation.streamprocessing.atos;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.dmg.pmml.MultivariateStat;
import org.json.JSONObject;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.supersede.bdma.validation.streamprocessing.Main;
import eu.supersede.bdma.validation.streamprocessing.StreamProcessing;
import eu.supersede.bdma.validation.streamprocessing.util.Sockets;
import scala.Tuple2;
import scala.Tuple4;

public class AtoS_stream_processing extends StreamProcessing {
	
	final static Logger logger = LogManager.getLogger(AtoS_stream_processing.class);
	
	private static int MAX_USERS = 100;
	private static long CURRENT_USERS;
	private static long CURRENT_TIMESTAMP;
	
	public static void init() throws Exception {
		topicName = Main.properties.getProperty("atos_topic");
		consumerGroup = Main.properties.getProperty("atos_consumer_group");
		hbaseTable = Main.properties.getProperty("atos_hbase_table");
		CURRENT_USERS = 0;
	}

	public static void process(JavaStreamingContext streamContext) throws Exception {

		Map<String, Integer> topicMap = Maps.newHashMap();
		topicMap.put(topicName, 1);
		
		JavaPairReceiverInputDStream<String, String> kafkaStream = 
				KafkaUtils.createStream(streamContext,Main.properties.getProperty("zk_quorum"),consumerGroup,topicMap);
		
		/**
		 * 0) Send the whole message
		 */
		kafkaStream.foreachRDD(new Function<JavaPairRDD<String,String>, Void>() {
			@Override
			public Void call(JavaPairRDD<String, String> arg0) throws Exception {
				for (Tuple2<String,String> t : arg0.collect()) {
					JSONObject obj = new JSONObject(t._2);
					JSONObject newObj = new JSONObject();
					
					newObj.put("deviceID", obj.getString("deviceID"));
					newObj.put("timestamp", obj.getString("timestamp"));
					newObj.put("playbackErrorVideo", obj.has("playbackErrorVideo") ? obj.getString("playbackErrorVideo") : "-");
					newObj.put("rebufferingVideo", obj.has("rebufferingVideo") ? obj.getLong("rebufferingVideo") : "0");
					newObj.put("syncAttemps", obj.has("syncAttemps") ? obj.getLong("syncAttemps") : "0");
					
					Sockets.sendSocketAlert(newObj.toString(), "atos/fullMessage");
				}
				return null;
			}
		});
		
		
		/**
		 * 1) DeviceID
				Detectar una audiencia superior al 85% de un 1000000.
		 */		
		JavaPairDStream<String, Integer> deviceIds = kafkaStream.mapToPair(new PairFunction<Tuple2<String,String>, String, Integer>() {
			@Override
			public Tuple2<String, Integer> call(Tuple2<String, String> arg0) throws Exception {
				JSONObject event = new JSONObject(arg0._2);
				String key = event.getString("deviceID");
				return new Tuple2<String,Integer>(key,1);
			}
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
		counts.count().foreachRDD(new Function<JavaRDD<Long>, Void>() {
			@Override
			public Void call(JavaRDD<Long> arg0) throws Exception {
				CURRENT_USERS = arg0.collect().get(0);
				Sockets.sendSocketAlert(String.valueOf(CURRENT_USERS), "atos/distinctDevices");
				return null;
			}
		});
		
		/**
		 * 2) Detectar que un 20% de los usuarios (DeviceID distintos en un periodo de tiempo) tienen algun problema de PlaybackErrorVideo.
		 */	
		JavaPairDStream<String, Integer> deviceIdsWithPlaybackErrorVideo = kafkaStream.filter(new Function<Tuple2<String,String>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, String> arg0) throws Exception {
				JSONObject event = new JSONObject(arg0._2);
				return event.has("playbackErrorVideo");
			}
		}).mapToPair(new PairFunction<Tuple2<String,String>, String, Integer>() {
			@Override
			public Tuple2<String, Integer> call(Tuple2<String, String> arg0) throws Exception {
				JSONObject event = new JSONObject(arg0._2);
				String key = event.getString("deviceID");
				return new Tuple2<String,Integer>(key,1);
			}
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
		playbackErrorVideoCounts.count().foreachRDD(new Function<JavaRDD<Long>, Void>() {
			@Override
			public Void call(JavaRDD<Long> arg0) throws Exception {
				long countDeviceIdsWithPlaybackErrorVideo = arg0.collect().get(0);
				Sockets.sendSocketAlert(String.valueOf(countDeviceIdsWithPlaybackErrorVideo), "atos/playbackErrorVideo");
				return null;
			}
		});
		
		/**
		 * 3) Detectar que un 20% de los usuarios (DeviceID distintos en un periodo de tiempo) tienen algun problema de Rebuffering con valor > 2.
		 */	
		JavaPairDStream<String, Integer> deviceIdsWithRebuffering = kafkaStream.filter(new Function<Tuple2<String,String>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, String> arg0) throws Exception {
				JSONObject event = new JSONObject(arg0._2);
				return event.has("rebufferingVideo");
			}
		}).mapToPair(new PairFunction<Tuple2<String,String>, String, Integer>() {
			@Override
			public Tuple2<String, Integer> call(Tuple2<String, String> arg0) throws Exception {
				JSONObject event = new JSONObject(arg0._2);
				String key = event.getString("deviceID");
				return new Tuple2<String,Integer>(key,1);
			}
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
		playbackErrorVideoCounts.count().foreachRDD(new Function<JavaRDD<Long>, Void>() {
			@Override
			public Void call(JavaRDD<Long> arg0) throws Exception {
				long countDeviceIdsWithRebuffering = arg0.collect().get(0);
				Sockets.sendSocketAlert(String.valueOf(countDeviceIdsWithRebuffering), "atos/rebuffering");
				return null;
			}
		});
		
		/**
		 * 4) SyncAttemp - Detectar outliers en cada periodo (Espacios de 5m.)
		 */	
		JavaDStream<Vector> syncAttemptWindow = kafkaStream.map(new Function<Tuple2<String,String>, Vector>() {
			@Override
			public Vector call(Tuple2<String, String> arg0) throws Exception {
				JSONObject obj = new JSONObject(arg0._2);
				return obj.has("syncAttemps") ? Vectors.dense(Double.valueOf(obj.getLong("syncAttemps")+"")) : Vectors.dense(0);
				//return Vectors.dense(CURRENT_USERS);
			}
		}).window(new Duration(60 * 5 * 1000), new Duration(1000))/*.print()*/;
		
		syncAttemptWindow.foreachRDD(new Function<JavaRDD<Vector>, Void>() {
			@Override
			public Void call(JavaRDD<Vector> arg0) throws Exception {
				MultivariateStatisticalSummary summary = Statistics.colStats(arg0.rdd());
				Sockets.sendSocketAlert(String.valueOf(summary.mean().toArray()[0]), "atos/meanSyncAttempt");
				Sockets.sendSocketAlert(String.valueOf(Math.sqrt(summary.mean().toArray()[0])), "atos/stDevSyncAttempt");

				return null;
			}
			
		});
		
		
		
		
	}
	
}
