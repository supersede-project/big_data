package eu.supersede.bdma.sa.tests;

/**
 * Created by snadal on 7/06/17.
 */
import eu.supersede.integration.api.pubsub.adaptation.AdaptationAlertMessageListener;
import eu.supersede.integration.api.pubsub.adaptation.AdaptationSubscriber;

public class AdaptationListener {

    public static void main(String[] args) throws Exception {
        AdaptationSubscriber subscriber = new AdaptationSubscriber();
        subscriber.openTopicConnection();
        AdaptationAlertMessageListener messageListener
                = subscriber.createAdaptationAlertSubscriptionAndKeepListening();
        try {
            while (!messageListener.areMessageReceived()) {
                Thread.sleep(1000); //FIXME Configure sleeping time
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Received alert: " + messageListener.getNextAlert());

    }
}