package eu.supersede.bdma.sa.eca_rules;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.datastore.integration.types.SupersedePlatform;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;
import eu.supersede.integration.federation.SupersedeFederation;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;


/**
 * Created by snadal on 27/01/17.
 */
public class ThresholdExceededAlert {

    public static void sendAlert(String API, Double responseTime) {
        Alert alert = new Alert();

        alert.setId("id"+ System.currentTimeMillis());
        alert.setApplicationId(API);
        alert.setTimestamp(System.currentTimeMillis());
        alert.setTenant(ModelSystem.Siemens);

        List<Condition> conditions = Lists.newArrayList();
        conditions.add (new Condition(new DataID("Tool", "response_time"), Operator.GEq, responseTime));
        alert.setConditions(conditions);

        TopicPublisher publisher = null;
        try {
            publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_ADAPTATION_EVENT_TOPIC,true,
                    //new SupersedeFederation().getFederatedSupersedePlatform("development").getPlatform()
                    "development");
            publisher.publishTextMesssageInTopic(new Gson().toJson(alert));
            publisher.closeTopicConnection();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}