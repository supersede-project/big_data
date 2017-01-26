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

    //final static Logger logger = LogManager.getLogger(Main.class);
    public static Properties properties;
    //static String HADOOP_COMMON_PATH = "C:\\Users\\Sergi Nadal\\Downloads\\winutils";

    public static void main(String[] args) throws Exception {


        //System.setProperty("hadoop.home.dir", HADOOP_COMMON_PATH);

        if (args.length != 1) {
            throw new Exception("Usage: [0]=config.properties path");
        }
        properties = new Properties(args[0]);
        if (!validProperties(properties)) {
            throw new Exception("Invalid properties, stopping execution");
        }

        //logger.debug("Defining Spark context with master = "+properties.getProperty("SPARK_MASTER_HOSTNAME"));



        SparkConf conf = new SparkConf().setAppName("StreamProcessing").setMaster(properties.getProperty("SPARK_MASTER_HOSTNAME"));
        JavaSparkContext context = new JavaSparkContext(conf);
        JavaStreamingContext streamContext = new JavaStreamingContext(context, new Duration(Long.parseLong(properties.getProperty("MICROBATCH_PERIOD"))));
//        streamContext.checkpoint("checkpoint");

        Logger.getRootLogger().setLevel(Level.OFF);

        StreamProcessing processor = new StreamProcessing();
        processor.process(context,streamContext);

        streamContext.start();
        streamContext.awaitTermination();

    }

    private static boolean validProperties(Properties properties) {
        if (properties.getProperty("BOOTSTRAP_SERVERS_CONFIG") == null) {
            //logger.error("Missing property \"BOOTSTRAP_SERVERS_CONFIG\"");
            return false;
        }
        if (properties.getProperty("KEY_SERIALIZER_CLASS_CONFIG") == null) {
            //logger.error("Missing property \"KEY_SERIALIZER_CLASS_CONFIG\"");
            return false;
        }
        if (properties.getProperty("VALUE_SERIALIZER_CLASS_CONFIG") == null) {
            //logger.error("Missing property \"VALUE_SERIALIZER_CLASS_CONFIG\"");
            return false;
        }
        if (properties.getProperty("SPARK_MASTER_HOSTNAME") == null) {
            //logger.error("Missing property \"SPARK_MASTER_HOSTNAME\"");
            return false;
        }
        if (properties.getProperty("MICROBATCH_PERIOD") == null) {
            //logger.error("Missing property \"MICROBATCH_PERIOD\"");
            return false;
        }
        return true;
    }

}
