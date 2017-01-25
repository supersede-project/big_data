package eu.supersede.bdma.sa.rules;

import com.clearspring.analytics.util.Lists;
import com.google.gson.Gson;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;

import java.util.List;

/**
 * Created by snadal on 24/01/17.
 */
public class Action {

    private static Alert createAlert() {
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
        requests.add(new UserRequest("id1", RequestClassification.FeatureRequest,
                0.5, "description string", 1, 2, 0, feedbackIDs, features));
        alert.setRequests(requests);

        return alert;
    }

    public static void sendAlert() throws Exception {
        TopicPublisher publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_EVENT_TOPIC,true);
        publisher.publishTextMesssageInTopic(new Gson().toJson(createAlert()));
    }
}
