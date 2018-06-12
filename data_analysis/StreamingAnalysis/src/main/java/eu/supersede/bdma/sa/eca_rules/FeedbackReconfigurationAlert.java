package eu.supersede.bdma.sa.eca_rules;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Created by snadal on 27/01/17.
 */
public class FeedbackReconfigurationAlert {

    public static void sendAlert(ECA_Rule r) {
        Alert alert = new Alert();
        alert.setId("id"+ System.currentTimeMillis());
        alert.setApplicationId("Senercon");
        alert.setTimestamp(System.currentTimeMillis());
        alert.setTenant(ModelSystem.FeedbackGatheringReconfiguration);

        long nFeedbacksToday = Main.ctx.textFile(Main.properties.getProperty("PATH_FEEDBACK_FILE"))
                .filter(t -> t != null)
                .filter(t -> !t.isEmpty())
                .filter(t -> !t.contains("test"))
                .filter(t -> t.contains("localTime"))
                .filter(t -> !t.contains("2017-"))
                .filter(t -> {
                    Calendar c = new GregorianCalendar();
                    c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    return Long.parseLong(Utils.extractFeatures(t,"Attributes/contextInformation/localTime").get(0))>c.getTimeInMillis();
                })
                .count();

        List<Condition> conditions = Lists.newArrayList();
        conditions.add(new Condition(new DataID("FGTool", "diskC"), Operator.GT, 110000.0));

        alert.setConditions(conditions);

        List<AttachedValue> attach = Lists.newArrayList();
        attach.add(new AttachedValue("attachment", "8000.0"));
        attach.add(new AttachedValue("screenshot", "8000.0"));
        attach.add(new AttachedValue("audio", "96000.0"));

        alert.setAttachedValues(attach);

        TopicPublisher publisher = null;
        try {
            publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_ADAPTATION_EVENT_TOPIC,true, Main.properties.getProperty("SUPERSEDE_DEFAULT_PLATFORM"));
            publisher.publishTextMesssageInTopic(new Gson().toJson(alert));
            publisher.closeTopicConnection();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
