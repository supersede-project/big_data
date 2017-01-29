package eu.supersede.bdma.sa.eca_rules;

import com.clearspring.analytics.util.Lists;
import com.google.gson.Gson;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;

import java.util.List;

/**
 * Created by snadal on 24/01/17.
 */
public class SoftwareEvolutionAlert {

    public static void sendAlert(String[] contents) throws Exception {
        TopicPublisher publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_EVENT_TOPIC,true);

        Alert alert = new Alert();

        alert.setID("id1");
        alert.setApplicationID("appId1");
        alert.setTimestamp(1481717773760L);
        alert.setTenant("Delta");

        List<Condition> conditions = Lists.newArrayList();
        conditions.add (new Condition(DataID.UNSPECIFIED, Operator.GEq, 10.5));
        alert.setConditions(conditions);

        List<UserRequest> requests = Lists.newArrayList();
        String[] feedbackIDs = new String[]{"feedbackId1"};
        String[] features = new String[]{"UI","backend"};

        for (int i = 0; i < contents.length; ++i) {
            String current = contents[i];
            UserRequest ur = new UserRequest("id"+String.valueOf(i), RequestClassification.EnhancementRequest, 0.5, current, 1, 1, 0, feedbackIDs, features);
            requests.add(ur);
        }

        alert.setRequests(requests);

        publisher.publishTextMesssageInTopic(new Gson().toJson(alert));
    }
}
