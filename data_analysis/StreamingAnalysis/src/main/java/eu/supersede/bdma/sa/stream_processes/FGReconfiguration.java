package eu.supersede.bdma.sa.stream_processes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.feedback.orchestrator.types.Configuration;
import eu.supersede.integration.api.feedback.proxies.FeedbackOrchestratorProxy;
import eu.supersede.integration.api.pubsub.adaptation.AdaptationPublisher;
import eu.supersede.integration.federation.SupersedeFederation;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;
import scala.Tuple2;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Created by snadal on 28/05/17.
 */
public class FGReconfiguration {

    public static void process(JavaSparkContext ctx) {
        List<String> IDs = Lists.newArrayList(Main.properties.getProperty("IDs_FOR_CATEGORIES").split(","));

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Alert alert = new Alert();
                alert.setId("id"+ System.currentTimeMillis());
                alert.setApplicationId("Senercon");
                alert.setTimestamp(System.currentTimeMillis());
                alert.setTenant(ModelSystem.SenerconFG);
                List<Condition> conditions = Lists.newArrayList();
                conditions.add(new Condition(new DataID("FGTool", "category"), Operator.GT, 1.0)); //feature category
                alert.setConditions(conditions);

                List<ActionOnAttribute> actions = Lists.newArrayList();

                List<Tuple2<Integer,String>> frequencies = ctx.textFile(Main.properties.getProperty("PATH_FEEDBACK_FILE"))
                   .filter(t -> t != null)
                   .filter(t -> !t.isEmpty())
                   .filter(t -> !t.contains("test"))
                   .filter(t -> t.contains("localTime"))
                   .filter(t -> !t.contains("2017-"))
                   .filter(t -> {
                       long untilTime = System.currentTimeMillis()-Long.parseLong(Main.properties.getProperty("FG_RECONFIGURATION_FROM_MS"));
                       return Long.parseLong(Utils.extractFeatures(t,"Attributes/contextInformation/localTime").get(0))>untilTime;
                   })
                   .flatMapToPair(t -> {
                       List<Tuple2<String,Integer>> out = Lists.newArrayList();
                       Utils.extractFeatures(t,"Attributes/categoryFeedbacks/parameterId")
                               .forEach(id -> out.add(new Tuple2<String,Integer>(id,1)));
                       return out.iterator();
                   })
                   .filter(t -> IDs.contains(t._1))
                   .reduceByKey((v1, v2) -> v1+v2)
                   .mapToPair(t -> new Tuple2<Integer,String>(t._2,t._1))
                   .sortByKey(true)
                   .collect();

                for (int i = 0; i < frequencies.size(); ++i) {
                    actions.add(new ActionOnAttribute("category_type.id_"+(frequencies.get(i)._2)+".order",AttributeAction.update,i+1));
                }
                //Check for 0s
                IDs.forEach(id -> {
                    if (actions.stream().filter(a -> a.getId().equals(id)).collect(Collectors.toList()).isEmpty()) {
                        actions.add(new ActionOnAttribute("category_type.id_"+id+".order",AttributeAction.update,actions.size()+1));
                    }
                });

                alert.setActionAttributes(actions);

                try {
                    AdaptationPublisher publisher = new AdaptationPublisher(true, Main.properties.getProperty("SUPERSDE_DEFAULT_PLATFORM"));
                    publisher.publishAdaptationAlertMesssage(alert);
                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }
        },1000, Long.parseLong(Main.properties.getProperty("FG_RECONFIGURATION_PERIOD_MS")));

    }

}
