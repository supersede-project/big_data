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
public class MonitorReconfigurationAlert {

    public static void sendAlert(ECA_Rule r, List<String> data) {
        Alert alert = new Alert();

        alert.setId("id"+ System.currentTimeMillis());
        alert.setApplicationId("dynamic");

        alert.setTenant(ModelSystem.AtosMonitoring);

        List<Condition> conditions = Lists.newArrayList();

        /*for (eu.supersede.integration.api.mdm.types.Condition c : r.getConditions()) {
            Operator o = null;
            if (c.getPredicate().equals("EQUALS")) o = Operator.EQ;
            else if (c.getPredicate().equals("GREATER_THAN")) o = Operator.GT;
            else if (c.getPredicate().equals("LESS_THAN")) o = Operator.LT;

            String[] IRIparts = c.getAttribute().split("/");
            String attrName = IRIparts[IRIparts.length-1];

            conditions.add(new Condition(new DataID("Tool", attrName), o, Double.parseDouble(c.getValue().toString())));
        }*/
        Operator o = Operator.GT;

        //String[] IRIparts = c.getAttribute().split("/");
        //String attrName = IRIparts[IRIparts.length-1];

        conditions.add(new Condition(new DataID("Tool", "responseTime"), o,
                Double.parseDouble(Utils.extractFeatures(data.get(0),"Attributes/HttpMonitoredData/DataItems/responseTime").get(0))));

        alert.setConditions(conditions);

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

}
