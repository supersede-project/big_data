package eu.supersede.bdma.sa.offline;

import com.clearspring.analytics.util.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.pubsub.evolution.EvolutionPublisher;
import eu.supersede.integration.federation.SupersedeFederation;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Atos_AppFeedback {

    public static void sendAlert(String[] contents, String appId) {
        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

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

        for (String classificationLabel : feedbackClassified.keySet()) {
            Alert SE_alert = new Alert();
            SE_alert.setId(UUID.randomUUID().toString());
            SE_alert.setApplicationId(appId);
            SE_alert.setTimestamp(System.currentTimeMillis());
            SE_alert.setTenant("atos");

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
                SupersedeFederation fed = new SupersedeFederation();

                EvolutionPublisher publisher = new EvolutionPublisher(true, fed.getFederatedSupersedePlatform("platform").getPlatform());
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

    public static void processFeedback(String appId, String path) throws Exception {
        List<String> allJsons = Lists.newArrayList();
        String json = "";
        for (String l : Files.lines(new File(path).toPath()).collect(Collectors.toList())) {
            //json += (l.replace("\n",""));
            json = (l.replace("\n",""));
            /*try {
                JSONObject a = (JSONObject)JSONValue.parse(json.substring(0,json.length()-1));
                if (a != null) {*/
            allJsons.add(json);/*
                    json = "";
                }
            } catch (Exception e) {
            }*/
        }
        allJsons.forEach(aJSON -> {
            String feedback = "";
            for (String feedbackPiece : Utils.extractFeatures(aJSON,"Attributes/textFeedbacks/text")) {
                if (!feedbackPiece.contains("@")) feedback += " " + feedbackPiece;
            }
            feedback = feedback.replace("\n","");
            if (!feedback.isEmpty()) {
                sendAlert(new String[]{feedback},appId);
            }
        });

    }



    public static void main(String[] args) throws Exception {
        processFeedback("SmartPlayer", "/home/snadal/Desktop/atos/6e1cc9e2-5bd1-4fd4-8509-75b3c4e40e1c.txt");

    }
}
