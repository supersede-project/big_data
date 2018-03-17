package eu.supersede.bdma.sa.eca_rules;

import com.clearspring.analytics.util.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import eu.supersede.integration.api.mdm.types.Event;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;
import eu.supersede.integration.api.pubsub.evolution.EvolutionPublisher;
import scala.Tuple2;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by snadal on 24/01/17.
 */
public class SoftwareEvolutionAlert {

    public static void sendAlert(Event event, String[] contents) {
        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
        String pathToFeatureExtractor = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

        Map<String, Set<UserFeedback>> feedbackClassified = Maps.newHashMap();
        for (String feedback : contents) {
            if (!feedback.trim().isEmpty()) {
                ClassificationResult classification = null;
                try {
                    classification = feedbackClassifier.classify(pathToClassificationModel, new UserFeedback(feedback));
                    if (!feedbackClassified.containsKey(classification.getLabel())) {
                        feedbackClassified.put(classification.getLabel(), Sets.newHashSet());
                    }
                    feedbackClassified.get(classification.getLabel()).add(new UserFeedback(feedback));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(feedbackClassified);

        for (String classificationLabel : feedbackClassified.keySet()) {
            Alert SE_alert = new Alert();
            SE_alert.setId(UUID.randomUUID().toString());
            SE_alert.setApplicationId("App ID?");
            SE_alert.setTimestamp(System.currentTimeMillis());
            SE_alert.setTenant(event.getTenant());

            List<Condition> conditions = Lists.newArrayList();
            conditions.add(new Condition(DataID.UNSPECIFIED, Operator.EQ, 1.0));
            SE_alert.setConditions(conditions);

            List<UserRequest> userRequests = Lists.newArrayList();
            for (UserFeedback feedback : feedbackClassified.get(classificationLabel)) {

                ClassificationResult classification = null;
                try {
                    classification = feedbackClassifier.classify(pathToClassificationModel,feedback);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestClassification rq = null;
                switch (classification.getLabel()) {
                    case "ENHANCEMENT": {
                        rq = RequestClassification.EnhancementRequest;
                        break;
                    }
                    case "DEFECT": {
                        rq = RequestClassification.BugFixRequest;
                        break;
                    }
                    case "FEATURE": {
                        rq = RequestClassification.FeatureRequest;
                        break;
                    }
                }

                SentimentAnalyzer sa = new MLSentimentAnalyzer();
                SentimentAnalysisResult saRes = null;
                try {
                    saRes = sa.classify(pathToSentimentAnalysisModel,feedback);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                userRequests.add(new UserRequest(UUID.randomUUID().toString(),rq,classification.getAccuracy(),feedback.getFeedbackText(),(int)(saRes.getPositiveSentiment()*100),
                        (int)(saRes.getNegativeSentiment()*100),(int)(saRes.getOverallSentiment()*100), new String[0],new String[0]));

            }
            SE_alert.setRequests(userRequests);

            try {
                EvolutionPublisher publisher = new EvolutionPublisher(true,event.getPlatform());
                publisher.publishEvolutionAlertMesssage(SE_alert);
                publisher.closeTopicConnection();
            } catch (NamingException e) {
                e.printStackTrace();
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
    }
}
