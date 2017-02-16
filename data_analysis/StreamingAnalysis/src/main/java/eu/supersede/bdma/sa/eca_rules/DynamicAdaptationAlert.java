package eu.supersede.bdma.sa.eca_rules;

import com.google.gson.Gson;
import eu.supersede.dynadapt.dm.datamodel.*;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snadal on 27/01/17.
 */
public class DynamicAdaptationAlert {

    public static void sendAlert() throws Exception {
        /*
        List<Condition> conditions = new ArrayList<>();
        List<ActionOnFeature> actionsF = new ArrayList<>();
        List<ActionOnAttribute> actionsA = new ArrayList<>();

        DataID idMonitored = new DataID();

        conditions.add(new Condition(idMonitored, Operator.GEq, 0.7));
        actionsF.add(new ActionOnFeature("attachment",FeatureAction.deselect));

        Alert alert = new Alert( "id1", "appId1", System.currentTimeMillis(), "atos", conditions,  actionsF, actionsA);

        TopicPublisher publisher = new TopicPublisher(SubscriptionTopic.ANALISIS_DM_ADAPTATION_EVENT_TOPIC,true);
        publisher.publishTextMesssageInTopic(new Gson().toJson(alert));
        */
        System.out.println("Im sending a dynamic adaptation alert!");
    }
}
