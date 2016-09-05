package eu.supersede.bdma.validation.streamprocessing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import eu.supersede.bdma.validation.streamprocessing.atos.AtoS_stream_processing;
import eu.supersede.bdma.validation.streamprocessing.senercon.SEnerCon_stream_processing;
import eu.supersede.bdma.validation.streamprocessing.siemens.SIEMENS_stream_processing;
import eu.supersede.bdma.validation.streamprocessing.util.Properties;
import scala.Tuple2;

/**
 * @author Sergi Nadal
 */
public class Main {
	
	final static Logger logger = LogManager.getLogger(Main.class);
	public static Properties properties;
	static String HADOOP_COMMON_PATH = "C:\\Users\\Sergi Nadal\\Downloads\\winutils";
	
	public static void main(String[] args) throws Exception {
		System.setProperty("hadoop.home.dir", HADOOP_COMMON_PATH);
		
		if (args.length != 2) {
			throw new Exception("Usage: [0]=config.properties path, [1] use case (atos,siemens,senercon)");
		}
		properties = new Properties(args[0]);
		if (!validProperties(properties)) {
			throw new Exception("Invalid properties, stopping execution");
		}
		
		logger.debug("Defining Spark context with master = "+properties.getProperty("spark_master_hostname"));
		SparkConf conf = new SparkConf().setAppName("SparkIngestionToHBase").setMaster(properties.getProperty("spark_master_hostname"));
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaStreamingContext streamContext = new JavaStreamingContext(context, new Duration(Long.parseLong(properties.getProperty("microbatch_period"))));
		streamContext.checkpoint("checkpoint");
		context.setLogLevel("WARN");
		
		if (args[1].equals("atos")) {
			AtoS_stream_processing.init();
			AtoS_stream_processing.process(streamContext);
		}
		else if (args[1].equals("siemens")) {
			SIEMENS_stream_processing.init();
			SIEMENS_stream_processing.process(streamContext);
		}
		else if (args[1].equals("senercon")) {
			SEnerCon_stream_processing.init();
			SEnerCon_stream_processing.process(streamContext);
		}
		else {
			throw new Exception("Wrong name for use case");
		}
		
		streamContext.start();
		streamContext.awaitTermination();
		
	}

	private static boolean validProperties(Properties properties) {
		if (properties.getProperty("kafka_master_hostname") == null) {
			logger.error("Missing property \"kafka_master_hostname\"");
			return false;
		}
		if (properties.getProperty("spark_master_hostname") == null) {
			logger.error("Missing property \"spark_master_hostname\"");
			return false;
		}
		if (properties.getProperty("zk_quorum") == null) {
			logger.error("Missing property \"zk_quorum\" (Zk Quorum  list of one or more zookeeper servers that make quorum)");
			return false;
		}
		if (properties.getProperty("microbatch_period") == null) {
			logger.error("Missing property \"microbatch_period\"");
			return false;
		}
		if (properties.getProperty("atos_topic") == null) {
			logger.error("Missing property \"atos_topic\"");
			return false;
		}
		if (properties.getProperty("atos_consumer_group") == null) {
			logger.error("Missing property \"atos_consumer_group\"");
			return false;
		}
		if (properties.getProperty("atos_hbase_table") == null) {
			logger.error("Missing property \"atos_hbase_table\"");
			return false;
		}
		if (properties.getProperty("siemens_topic") == null) {
			logger.error("Missing property \"siemens_topic\"");
			return false;
		}
		if (properties.getProperty("siemens_consumer_group") == null) {
			logger.error("Missing property \"siemens_consumer_group\"");
			return false;
		}
		if (properties.getProperty("siemens_hbase_table") == null) {
			logger.error("Missing property \"siemens_hbase_table\"");
			return false;
		}
		if (properties.getProperty("senercon_topic") == null) {
			logger.error("Missing property \"senercon_topic\"");
			return false;
		}
		if (properties.getProperty("senercon_consumer_group") == null) {
			logger.error("Missing property \"senercon_consumer_group\"");
			return false;
		}
		if (properties.getProperty("senercon_hbase_table") == null) {
			logger.error("Missing property \"senercon_hbase_table\"");
			return false;
		}

		return true;
	};
	
}
