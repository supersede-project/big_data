package eu.supersede.bdma.sa.stream_processes;

import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.eca_rules.MonitorReconfigurationNonDeterministicAlert;
import eu.supersede.bdma.sa.eca_rules.ThresholdExceededAlert;
import eu.supersede.integration.api.adaptation.types.ModelSystem;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaSparkContext;
import org.joda.time.Period;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

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
                            LocalDateTime date = null;
                            try {
                                date = LocalDateTime.parse(o.getAsString("Date"), formatter1);
                            } catch (Exception e) {
                                date = LocalDateTime.parse(o.getAsString("Date"), formatter2);
                            }
                            return date.isAfter(LocalDateTime.now().minusHours(1));
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
                    double sum = 0;
                    int count = 0;
                    for (String t : Files.lines(new File(Main.properties.getProperty("PATH_ALARMS")).toPath()).collect(Collectors.toList())) {
                        System.out.println(t);
                        if (!t.contains("GroupedMethodName")) {
                            String API = t.split(",")[0].replace("\"","");
                            Double responseTime = Double.parseDouble(t.split(",")[1]);
                            sum+=responseTime;
                            ++count;
                            System.out.println("sending alert for "+API+" - "+responseTime);
                        }
                    };
                    if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_ALERT_BUILDINGS"))) {
                        ThresholdExceededAlert.sendAlert(ModelSystem.Siemens_Buildings,sum/count);
                    }
                    if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_ALERT_TYPES"))) {
                        ThresholdExceededAlert.sendAlert(ModelSystem.Siemens_Types,sum/count);
                    }
                    if (Boolean.parseBoolean(Main.properties.getProperty("LAUNCH_ALERT_DATE"))) {
                        ThresholdExceededAlert.sendAlert(ModelSystem.Siemens_GetMinMaxDates,sum/count);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                System.out.println("Thresholds successfully evaluated");
            }

        },Long.parseLong(Main.properties.getProperty("THRESHOLD_EVALUATION_FIRST_TIME_MS")), Long.parseLong(Main.properties.getProperty("THRESHOLD_EVALUATION_PERIOD_MS")));

    }

}
