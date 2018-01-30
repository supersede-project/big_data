package eu.supersede.bdma.sa;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import eu.supersede.bdma.sa.usecase_specific.Atos_SA;
import eu.supersede.bdma.sa.usecase_specific.Siemens_SA;
import eu.supersede.bdma.sa.utils.Properties;

import java.net.URL;
import java.util.Enumeration;

/**
 * Created by snadal on 20/09/16.
 */
public class Main {

    public static Properties properties;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Usage: [0]=config.properties path; [1] evolution/adaptation");
        }
        properties = new Properties(args[0]);
        if (!validProperties(properties)) {
            throw new Exception("Invalid properties, stopping execution");
        }
        SparkConf conf = new SparkConf().setAppName("StreamProcessing").setMaster(properties.getProperty("SPARK_MASTER_HOSTNAME"));
        JavaSparkContext context = new JavaSparkContext(conf);
        System.out.println("microbatch period = "+properties.getProperty("MICROBATCH_PERIOD"));
        JavaStreamingContext streamContext = new JavaStreamingContext(context, new Duration(Long.parseLong(properties.getProperty("MICROBATCH_PERIOD"))));
        streamContext.checkpoint("checkpoint");
        Logger.getRootLogger().setLevel(Level.OFF);
        StreamProcessing processor = new StreamProcessing();
        processor.process(context,streamContext);
        streamContext.start();
        streamContext.awaitTermination();
    }

    private static boolean validProperties(Properties properties) {
        if (properties.getProperty("BOOTSTRAP_SERVERS_CONFIG") == null ||
            properties.getProperty("KEY_SERIALIZER_CLASS_CONFIG") == null ||
            properties.getProperty("VALUE_SERIALIZER_CLASS_CONFIG") == null ||
            properties.getProperty("SPARK_MASTER_HOSTNAME") == null ||
            properties.getProperty("MICROBATCH_PERIOD") == null ||
            properties.getProperty("GROUP_ID") == null ||
            properties.getProperty("AUTO_OFFSET_RESET") == null ||
            properties.getProperty("LAUNCH_DISPATCHER") == null ||
            properties.getProperty("LAUNCH_DATA_SOURCE_STATISTICS") == null ||
            properties.getProperty("LAUNCH_GENERIC_STREAM_STATISTICS") == null ||
            properties.getProperty("LAUNCH_RAW_DATA_TO_LIVE_FEED") == null ||
            properties.getProperty("LAUNCH_RULE_EVALUATION") == null
        ) {
            return false;
        }
        return true;
    }

}
