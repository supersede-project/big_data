package eu.supersede.bdma.sa.offline;

import com.clearspring.analytics.util.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.eca_rules.SoftwareEvolutionAlert;
import eu.supersede.bdma.sa.proxies.MDMProxy;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
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
import java.util.stream.Collectors;

public class SIEMENS_AppFeedback {

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
            SE_alert.setTenant("siemens");

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
            //System.out.println(new Gson().toJson(SE_alert));

            try {
                SupersedeFederation fed = new SupersedeFederation();

                EvolutionPublisher publisher = new EvolutionPublisher(true, fed.getLocalFederatedSupersedePlatform().getPlatform());
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
        String array = Files.lines(new File(path).toPath()).collect(Collectors.toList()).get(0);
        JSONArray arr = (JSONArray) JSONValue.parse(array);

        arr.forEach(j -> {
            sendAlert(Iterables.toArray(Utils.extractFeatures(String.valueOf(j),"Attributes/textFeedbacks/text"),String.class),appId);
        });

    }



    public static void main(String[] args) throws Exception {
        processFeedback("app2", "/home/snadal/UPC/Sergi/SUPERSEDE/T2.1/SiemensAppFeedback/app2.json");
        processFeedback("app3", "/home/snadal/UPC/Sergi/SUPERSEDE/T2.1/SiemensAppFeedback/app3.json");
        processFeedback("app4", "/home/snadal/UPC/Sergi/SUPERSEDE/T2.1/SiemensAppFeedback/app4.json");
        processFeedback("app5", "/home/snadal/UPC/Sergi/SUPERSEDE/T2.1/SiemensAppFeedback/app5.json");

    }
}
