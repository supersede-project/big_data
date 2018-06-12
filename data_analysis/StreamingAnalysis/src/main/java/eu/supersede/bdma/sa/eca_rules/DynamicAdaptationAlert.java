package eu.supersede.bdma.sa.eca_rules;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.Main;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.mdm.types.PredicatesTypes;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;
import java.util.List;
import eu.supersede.integration.api.mdm.types.ECA_Rule;

import javax.jms.JMSException;
import javax.naming.NamingException;


/**
 * Created by snadal on 27/01/17.
 */
public class DynamicAdaptationAlert {

    public static void sendAlert(ECA_Rule r) {
        Alert alert = new Alert();


        alert.setId("id"+ System.currentTimeMillis());
        alert.setApplicationId("dynamic");

        if (r.getEvent().getTenant().getId().contains("siemens")) {
            alert.setTenant(ModelSystem.SiemensMonitoring);
        }
        else if (r.getEvent().getTenant().getId().contains("atos")) {
            alert.setTenant(ModelSystem.Atos_HSK);
        }


        List<Condition> conditions = Lists.newArrayList();

        for (eu.supersede.integration.api.mdm.types.Condition c : r.getConditions()) {
            Operator o = null;
            if (c.getPredicate().equals("EQUALS")) o = Operator.EQ;
            else if (c.getPredicate().equals("GREATER_THAN")) o = Operator.GT;
            else if (c.getPredicate().equals("LESS_THAN")) o = Operator.LT;

            String[] IRIparts = c.getAttribute().split("/");
            String attrName = IRIparts[IRIparts.length-1];

            conditions.add(new Condition(new DataID("Tool", attrName), o, Double.parseDouble(c.getValue().toString())));

        }
        alert.setConditions(conditions);

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
