package eu.supersede.bdma.sa.offline;

import com.clearspring.analytics.util.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.GermanFeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.clustering.FeedbackClusterer;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.GermanSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.pubsub.evolution.EvolutionPublisher;
import eu.supersede.integration.federation.SupersedeFederation;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntClass;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Senercon_Y3ValidationAlerts {

    public static void sendAlerts(String[] contents, String appId) {
        FeedbackClassifier feedbackClassifier = new GermanFeedbackClassifier();
        String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("german_classify.model").toString().replace("file:","");
        String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("german_sentiment.model").toString().replace("file:","");

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
        String ontologyFile = "SDO_ontology.ttl";
        String language = "de";
        String wordnetDbPath = "/home/snadal/UPC/Sergi/SUPERSEDE/Development/big_data/data_analysis/StreamingAnalysis/WordNet-3.0-dict";
        StringBuilder sb = new StringBuilder();
        feedbackClassified.forEach((s, userFeedbacks) -> {
            sb.append("Classification: "+s+"\n");

            FeedbackClusterer feedbackClusterer = new FeedbackClusterer(ontologyFile, wordnetDbPath, language);
            try {
                Alert SE_alert = new Alert();
                SE_alert.setId(UUID.randomUUID().toString());
                SE_alert.setApplicationId(appId);
                SE_alert.setTimestamp(System.currentTimeMillis());
                SE_alert.setTenant("senercon");
                List<Condition> conditions = Lists.newArrayList();
                conditions.add(new Condition(DataID.UNSPECIFIED, Operator.EQ, 1.0));
                SE_alert.setConditions(conditions);

                List<UserRequest> userRequests = Lists.newArrayList();

                Map<Set<OntClass>, List<UserFeedback>> feedbackClusters = feedbackClusterer.clusterUserFeedbackConceptsUnion(Lists.newArrayList(userFeedbacks),5);
                feedbackClusters.forEach((concepts, feedbacksInACluster) -> {
                    feedbacksInACluster.forEach(finc -> {
                        ClassificationResult classification = null;
                        try {
                            classification = feedbackClassifier.classify(pathToClassificationModel,finc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        RequestClassification rq = null;
                        switch (classification.getLabel().toLowerCase()) {
                            case "enhancement request": {
                                rq = RequestClassification.EnhancementRequest;
                                break;
                            }
                            case "bug report": {
                                rq = RequestClassification.BugFixRequest;
                                break;
                            }
                            case "feature request": {
                                rq = RequestClassification.FeatureRequest;
                                break;
                            }
                            case "other": {
                                rq = RequestClassification.Other;
                                break;
                            }
                        }

                        SentimentAnalyzer sa = new GermanSentimentAnalyzer();
                        SentimentAnalysisResult saRes = null;
                        try {
                            saRes = sa.classify(pathToSentimentAnalysisModel,finc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        userRequests.add(new UserRequest(UUID.randomUUID().toString(),rq,classification.getAccuracy(),
                                "["+concepts.stream().map(t->t.getLocalName()).collect(Collectors.joining(", "))+"] - "+finc.getFeedbackText(),
                                (int)(saRes.getPositiveSentiment()*100),
                                (int)(saRes.getNegativeSentiment()*100),(int)(saRes.getOverallSentiment()*100), new String[0],new String[0]));
                    });
                });
                SE_alert.setRequests(userRequests);
                System.out.println(new Gson().toJson(SE_alert));
                try {
                SupersedeFederation fed = new SupersedeFederation();

                EvolutionPublisher publisher = new EvolutionPublisher(true, "production");
                publisher.publishEvolutionAlertMesssage(SE_alert);
                publisher.closeTopicConnection();
                } catch (NamingException e) {
                    e.printStackTrace();
                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public static void processFeedback(String appId, String path) throws Exception {
        List<String> allJsons = Lists.newArrayList();
        String json = "";
        for (String l : Files.lines(new File(path).toPath()).collect(Collectors.toList())) {
            json += (l.replace("\n",""));
            //json = (l.replace("\n",""));
            try {
                JSONObject a = (JSONObject) JSONValue.parse(json.substring(0,json.length()-1));
                if (a != null) {
                    allJsons.add(json);
                    json = "";
                }
            } catch (Exception e) {
            }
        }
        List<String> allFeedbacks = Lists.newArrayList();
        allJsons.forEach(aJSON -> {
            String feedback = "";
            for (String feedbackPiece : Utils.extractFeatures(aJSON,"Attributes/textFeedbacks/text")) {
                if (!feedbackPiece.contains("@")) feedback += " " + feedbackPiece;
            }
            feedback = feedback.replace("\n","");
            if (!feedback.isEmpty()) {
                allFeedbacks.add(feedback);

            }
        });
        sendAlerts(Iterables.toArray(allFeedbacks,String.class),appId);

    }



    public static void main(String[] args) throws Exception {
        processFeedback("Senercon Energy Management", "/home/snadal/Desktop/senercon/senercon_feedback_up_to_20180403_09_54.json");

    }
}
