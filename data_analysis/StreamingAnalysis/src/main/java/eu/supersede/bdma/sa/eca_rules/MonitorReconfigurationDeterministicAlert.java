package eu.supersede.bdma.sa.eca_rules;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;


/**
 * Created by snadal on 27/01/17.
 */
public class MonitorReconfigurationDeterministicAlert {

    public static void sendAlert(ECA_Rule r, List<String> data) {
        Alert alert = new Alert();

        alert.setId("id"+ System.currentTimeMillis());
        alert.setApplicationId("httpMonitor");
        alert.setTimestamp(System.currentTimeMillis());
        alert.setTenant(ModelSystem.AtosMonitoring);

        List<Condition> conditions = Lists.newArrayList();
        conditions.add(new Condition(new DataID("HTTPMonitor", "startMonitor"), Operator.EQ, 1.0)); //start http monitors

        alert.setConditions(conditions);

        // FIXME here I'm adding actions (will not be used) b/c deterministic cases are determined by the presence of actions
        List<ActionOnAttribute> actions = Lists.newArrayList();
        // here put any attribute/action, it is not used in any way at the moment. We'll change it in the future if needed
        actions.add(new ActionOnAttribute("httpMonitor", AttributeAction.update, 2));

        /*
        For the case 2 we will not have actions
         */

        alert.setActionAttributes(actions);

        TopicPublisher publisher = null;
        try {
            publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_ADAPTATION_EVENT_TOPIC,true, r.getEvent().getPlatform());
            publisher.publishTextMesssageInTopic(new Gson().toJson(alert));
            publisher.closeTopicConnection();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        sendAlert(null,null);
    }

}
