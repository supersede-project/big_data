package eu.supersede.bdma.sa.stream_processes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.pubsub.adaptation.AdaptationPublisher;
import eu.supersede.integration.federation.SupersedeFederation;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaSparkContext;
import org.jsontocsv.writer.CSVWriter;
import scala.Tuple2;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Created by snadal on 28/05/17.
 */
public class ThresholdRecomputation {

    public static void process(JavaSparkContext ctx) {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //First, delete the last generated CSV and previous thresholds (if exists)
                FileUtils.deleteQuietly(new File(Main.properties.getProperty("PATH_CONVERTED_LOG_FILE")));
                FileUtils.deleteQuietly(new File(Main.properties.getProperty("PATH_THRESHOLDS")));

                //Convert the historical JSON to CSV
                ctx.textFile(Main.properties.getProperty("PATH_LOG_FILE"))/*.sample(true,0.01)*/
                        .filter(t -> !t.isEmpty())
                        .map(t -> (JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) JSONValue.parse(t)).get("JSONFiles")).get("DataItems")).get(0))
                        .filter(t -> t.getAsString("level") != null && t.getAsString("Date") != null && t.getAsString("class_name") != null
                                && t.getAsString("message") != null)
                        .map(obj -> obj.getAsString("level") + " | " + obj.getAsString("Date") + " | " + obj.getAsString("class_name") +
                                " | " + obj.getAsString("message"))
                        .repartition(1)
                        .saveAsTextFile(Main.properties.getProperty("PATH_CONVERTED_LOG_FILE"));

                //Call the R script to recompute thresholds
                try {
		    System.out.println(Main.properties.getProperty("COMMAND_EXECUTE_GET_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_CONVERTED_LOG_FILE") + "/part-00000" + " " +
                            Main.properties.getProperty("PATH_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_METHOD_CLUSTERING"));
                    Process p = Runtime.getRuntime().exec(Main.properties.getProperty("COMMAND_EXECUTE_GET_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_CONVERTED_LOG_FILE") + "/part-00000" + " " +
                            Main.properties.getProperty("PATH_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_METHOD_CLUSTERING"));
                    p.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Thresholds successfully recomputed");

            }
        },1000, Long.parseLong(Main.properties.getProperty("THRESHOLD_RECOMPUTATION_PERIOD_MS")));

    }

}
