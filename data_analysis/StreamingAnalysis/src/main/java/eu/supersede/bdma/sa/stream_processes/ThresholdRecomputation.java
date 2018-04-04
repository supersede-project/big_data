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
import org.apache.spark.api.java.JavaSparkContext;
import org.jsontocsv.writer.CSVWriter;
import scala.Tuple2;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Created by snadal on 28/05/17.
 */
public class ThresholdRecomputation {

    public static void process(JavaSparkContext ctx) {
        /*Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {*/
                ctx.textFile(Main.properties.getProperty("PATH_LOG_FILE"))
                    .map(t -> (JSONObject) ((JSONArray)((JSONObject)((JSONObject) JSONValue.parse(t)).get("JSONFiles")).get("DataItems")).get(0))
                    .filter(t -> t.getAsString("level") != null && t.getAsString("Date") != null && t.getAsString("class_name") != null
                        && t.getAsString("message") != null)
                    .map(obj -> obj.getAsString("level") + " | " + obj.getAsString("Date") + " | " + obj.getAsString("class_name") +
                            " | " + obj.getAsString("message"))
                    .saveAsTextFile("/home/snadal/UPC/Sergi/SUPERSEDE/SIEMENS_DynAdapt/jsonData/converted.csv");


            /*
        },1000, Long.parseLong(Main.properties.getProperty("THRESHOLD_RECOMPUTATION_PERIOD_MS")));*/

    }

}
