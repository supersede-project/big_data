package eu.supersede.bdma.sa.eca_rules;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
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

        if (r.getEvent().getPlatform().contains("siemens")) {
            alert.setTenant(ModelSystem.Siemens);
        }
        else if (r.getEvent().getPlatform().contains("atos")) {
            alert.setTenant(ModelSystem.Atos_HSK);
        }


        List<Condition> conditions = Lists.newArrayList();

        Operator o = null;
        if (r.getPredicate().val().equals(PredicatesTypes.EQ)) o = Operator.EQ;
        else if (r.getPredicate().val().equals(PredicatesTypes.GT)) o = Operator.GT;
        else if (r.getPredicate().val().equals(PredicatesTypes.LT)) o = Operator.LT;

        for (eu.supersede.integration.api.mdm.types.Condition c : r.getConditions()) {
            conditions.add(new Condition(new DataID("Tool", c.getAttribute()), o, Double.parseDouble(r.getValue().toString())));

        }
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
