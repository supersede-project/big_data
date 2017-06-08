package eu.supersede.bdma.sa.eca_rules;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.mdm.types.OperatorTypes;
import eu.supersede.integration.api.mdm.types.PredicatesTypes;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by snadal on 27/01/17.
 */
public class DynamicAdaptationAlert {

    public static void sendAlert(SerializableECA_Rule r) throws Exception {
        Alert alert = new Alert();

        alert.setId("id"+ System.currentTimeMillis());
        alert.setApplicationId("dynamic");
        alert.setTimestamp(Calendar.getInstance().getTimeInMillis());
        alert.setTenant(ModelSystem .Atos_HSK);

        List<Condition> conditions = Lists.newArrayList();

        Operator o = null;
        if (r.getPredicate().val().equals(PredicatesTypes.EQ)) o = Operator.EQ;
        else if (r.getPredicate().val().equals(PredicatesTypes.GT)) o = Operator.GT;
        else if (r.getPredicate().val().equals(PredicatesTypes.LT)) o = Operator.LT;


        conditions.add (new Condition(new DataID("Tool", r.getFeature()), o, Double.parseDouble(r.getValue().toString())));
        // response_time: quality attribute
        // 10.0: threshold

        alert.setConditions(conditions);

        TopicPublisher publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_ADAPTATION_EVENT_TOPIC,true);
        publisher.publishTextMesssageInTopic(new Gson().toJson(alert));
        publisher.closeTopicConnection();

        System.out.println("Im sending a dynamic adaptation alert!");
    }
}
