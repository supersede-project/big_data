package eu.supersede.bdma.sa.stream_processes;

import com.clearspring.analytics.util.Lists;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.utils.MonitorReconfigurationJSON;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;
import net.minidev.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import scala.Tuple2;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.time.LocalDateTime;
import java.util.List;

public class FeedbackReconfigurationBasedOnDiskConsumption {

    private static LocalDateTime lastAlert = null;

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
        kafkaStream
                .mapToPair(t -> new Tuple2<String,String>(t.topic(),t.value()))
                .filter(t -> Main.properties.getProperty("DISK_CONSUMPTION_TOPICS").contains(t._1))
                .filter(t -> {
                    return lastAlert != null && LocalDateTime.now().minusDays(1).isAfter(lastAlert);
                })
                .foreachRDD(rdd -> {
                    rdd.foreach(t -> {
                        double threshold = Double.parseDouble(Main.properties.getProperty("THRESHOLD_RECONFIGURATION_CONSUMPTION"));
                        JSONObject json = MonitorReconfigurationJSON.adaptJSON(t._2);
                        double total = 0;
                        if (json.getAsNumber("number_files_audio").doubleValue() > 0) {
                            total += json.getAsNumber("disk_consumption_audio").doubleValue()/json.getAsNumber("number_files_audio").doubleValue();
                        }
                        if (json.getAsNumber("number_files_attachments").doubleValue() > 0) {
                            total += json.getAsNumber("disk_consumption_attachments").doubleValue()/json.getAsNumber("number_files_attachments").doubleValue();
                        }

                        System.out.println("threshold "+threshold+" -- total "+total);

                        if (total > threshold) {
                            lastAlert = LocalDateTime.now();
                            
                            Alert alert = new Alert();

                            alert.setId("id"+ System.currentTimeMillis());
                            alert.setApplicationId("feedback");
                            alert.setTimestamp(1481717773760L);
                            alert.setTenant(ModelSystem.SenerconFG);

                            List<Condition> conditions = Lists.newArrayList();
                            conditions.add(new Condition(new DataID("FGTool", "diskC"), Operator.GT, threshold)); //threshold for both attachments and audios

                            alert.setConditions(conditions);

                            List<AttachedValue> attach = Lists.newArrayList();
                            double diskAttachments = json.getAsNumber("number_files_attachments").doubleValue() == 0
                                    ? 0
                                    : json.getAsNumber("disk_consumption_attachments").doubleValue()/json.getAsNumber("number_files_attachments").doubleValue();
                            double diskAudio = json.getAsNumber("number_files_audio").doubleValue() == 0
                                    ? 0
                                    : json.getAsNumber("disk_consumption_audio").doubleValue()/json.getAsNumber("number_files_audio").doubleValue();
                            attach.add(new AttachedValue("attachment", String.valueOf(diskAttachments))); //calculated value for attachments
                            attach.add(new AttachedValue("audio", String.valueOf(diskAudio))); // calculated value for audios

                            alert.setAttachedValues(attach);

                            TopicPublisher publisher = null;
                            try {
                                publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_ADAPTATION_EVENT_TOPIC,true, "development");
                                publisher.publishTextMesssageInTopic(new Gson().toJson(alert));
                                publisher.closeTopicConnection();
                            } catch (NamingException e) {
                                e.printStackTrace();
                            } catch (JMSException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });

    }

}
