package eu.supersede.bdma.sa.stream_processes;

import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.eca_rules.MonitorReconfigurationNonDeterministicAlert;
import eu.supersede.bdma.sa.eca_rules.ThresholdExceededAlert;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by snadal on 28/05/17.
 */
public class ThresholdEvaluation {

    public static void process(JavaSparkContext ctx) {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("evaluating thresholds");

                //First, delete the last generated CSV and previous thresholds (if exists)
                FileUtils.deleteQuietly(new File(Main.properties.getProperty("PATH_CONVERTED_HOURLY_LOG_FILE")));
                FileUtils.deleteQuietly(new File(Main.properties.getProperty("PATH_ALARMS")));

                //Convert the historical JSON to CSV
                ctx.textFile(Main.properties.getProperty("PATH_LOG_FILE"))/*.sample(true,0.01)*/
                        .filter(t -> !t.isEmpty())
                        .map(t -> (JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) JSONValue.parse(t)).get("JSONFiles")).get("DataItems")).get(0))
                        .filter(t -> t.getAsString("level") != null && t.getAsString("Date") != null && t.getAsString("class_name") != null
                                && t.getAsString("message") != null)
                        .filter(o -> {
                            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
                            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS", Locale.ENGLISH);
                            LocalDate date = null;
                            try {
                                date = LocalDate.parse(o.getAsString("Date"), formatter1);
                            } catch (Exception e) {
                                date = LocalDate.parse(o.getAsString("Date"), formatter2);
                            }
                            return date.isAfter(LocalDate.now().minusDays(3));
                        })
                        .map(obj -> obj.getAsString("level") + " | " + obj.getAsString("Date") + " | " + obj.getAsString("class_name") +
                                " | " + obj.getAsString("message"))
                        .repartition(1)
                        .saveAsTextFile(Main.properties.getProperty("PATH_CONVERTED_HOURLY_LOG_FILE"));

                System.out.println("last hour data generated");

                //Call the R script to recompute thresholds
                try {
                    System.out.println(Main.properties.getProperty("COMMAND_EXECUTE_EVALUATE_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_CONVERTED_HOURLY_LOG_FILE") + "/part-00000" + " " +
                            Main.properties.getProperty("PATH_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_ALARMS") + " " +
                            Main.properties.getProperty("PATH_METHOD_CLUSTERING"));
                    Process p = Runtime.getRuntime().exec(Main.properties.getProperty("COMMAND_EXECUTE_EVALUATE_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_CONVERTED_HOURLY_LOG_FILE") + "/part-00000" + " " +
                            Main.properties.getProperty("PATH_THRESHOLDS") + " " +
                            Main.properties.getProperty("PATH_ALARMS") + " " +
                            Main.properties.getProperty("PATH_METHOD_CLUSTERING"));
                    p.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Process the generated CSV
                try {
                    Files.lines(new File(Main.properties.getProperty("PATH_ALARMS")).toPath()).forEach(t -> {
                        System.out.println(t);
                        if (!t.contains("GroupedMethodName")) {
                            String API = t.split(",")[0].replace("\"","");
                            Double responseTime = Double.parseDouble(t.split(",")[1]);
                            System.out.println("sending alert for "+API+" - "+responseTime);
                            ThresholdExceededAlert.sendAlert(API,responseTime);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }


                System.out.println("Thresholds successfully evaluated");
            }

        },1000, Long.parseLong(Main.properties.getProperty("THRESHOLD_EVALUATION_PERIOD_MS")));

    }

}
