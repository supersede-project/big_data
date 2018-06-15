package eu.supersede.bdma.sa;

import eu.supersede.bdma.sa.stream_processes.ThresholdEvaluation;
import eu.supersede.bdma.sa.stream_processes.ThresholdRecomputation;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import eu.supersede.bdma.sa.utils.Properties;

/**
 * Created by snadal on 20/09/16.
 */
public class Main {

    public static Properties properties;
    public static JavaSparkContext ctx;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Usage: [0]=config.properties path");
        }
        properties = new Properties(args[0]);
        if (!validProperties(properties)) {
            throw new Exception("Invalid properties, stopping execution");
        }
        SparkConf conf = new SparkConf()
                .set("spark.streaming.backpressure.enabled","true")
                .set("spark.streaming.kafka.consumer.cache.enabled", "false")
                //.set("spark.streaming.backpressure.initialRate","10")
                //.set("spark.streaming.kafka.maxRatePerPartition","1")
                //.set("spark.streaming.receiver.maxRate","1") //Property spark.streaming.receiver.maxRate applies to number of records per second.
                .setAppName("StreamProcessing").setMaster(properties.getProperty("SPARK_MASTER_HOSTNAME"));

        JavaSparkContext ctx = new JavaSparkContext(conf);

        System.out.println("microbatch period = "+properties.getProperty("MICROBATCH_PERIOD"));
        JavaStreamingContext streamContext = new JavaStreamingContext(ctx, new Duration(Long.parseLong(properties.getProperty("MICROBATCH_PERIOD"))));
        streamContext.checkpoint("checkpoint");
        Logger.getRootLogger().setLevel(Level.OFF);
        StreamProcessing processor = new StreamProcessing();
        processor.process(ctx,streamContext);
        streamContext.start();
        streamContext.awaitTermination();
    }

    private static boolean validProperties(Properties properties) {
        return !(
            properties.getProperty("BOOTSTRAP_SERVERS_CONFIG") == null ||
            properties.getProperty("KEY_SERIALIZER_CLASS_CONFIG") == null ||
            properties.getProperty("VALUE_SERIALIZER_CLASS_CONFIG") == null ||
            properties.getProperty("SPARK_MASTER_HOSTNAME") == null ||
            properties.getProperty("MICROBATCH_PERIOD") == null ||
            properties.getProperty("GROUP_ID") == null ||
            properties.getProperty("AUTO_OFFSET_RESET") == null ||

            properties.getProperty("FG_RECONFIGURATION_PERIOD_MS") == null ||
            properties.getProperty("FG_RECONFIGURATION_FROM_MS") == null ||
            properties.getProperty("PATH_FEEDBACK_FILE") == null ||
            properties.getProperty("IDs_FOR_CATEGORIES") == null ||

            properties.getProperty("WINDOW_SIZE_EVOLUTION_MS") == null ||
            properties.getProperty("WINDOW_SIZE_DYNAMIC_ADAPTATION_MS") == null ||
            properties.getProperty("WINDOW_SIZE_MONITOR_RECONF_MS") == null ||

            properties.getProperty("LAUNCH_PRINT_STREAM_TO_STDOUT") == null ||
            properties.getProperty("LAUNCH_DISPATCHER") == null ||
            properties.getProperty("LAUNCH_STREAM_UNIFIER_CEP") == null ||
            properties.getProperty("LAUNCH_FEEDBACK_VIEWER") == null ||
            properties.getProperty("LAUNCH_DATA_SOURCE_STATISTICS") == null ||
            properties.getProperty("LAUNCH_GENERIC_STREAM_STATISTICS") == null ||
            properties.getProperty("LAUNCH_RAW_DATA_TO_LIVE_FEED") == null ||
            properties.getProperty("LAUNCH_RULE_EVALUATION") == null ||
            properties.getProperty("LAUNCH_FG_RECONFIGURATION") == null ||
            properties.getProperty("LAUNCH_1_FEEDBACK_1_ALERT") == null ||
            properties.getProperty("LAUNCH_THRESHOLD_RECOMPUTATION") == null ||
            properties.getProperty("LAUNCH_THRESHOLD_EVALUATION") == null
        );
    }

}
