package eu.supersede.bdma.sa.offline;

import com.clearspring.analytics.util.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.eca_rules.conditions.ConditionEvaluator;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.clustering.FeedbackAnnotator;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.integration.api.adaptation.types.Tenant;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.mdm.types.Event;
import eu.supersede.integration.api.mdm.types.Parameter;
import eu.supersede.integration.api.pubsub.evolution.EvolutionPublisher;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Atos_feedback_dump_with_ontology_distance {

    public static void sendAlert(List<String> feedbacks, String alertId, String appId) {
        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");


        Alert SE_alert = new Alert();
        SE_alert.setId(alertId);
        SE_alert.setApplicationId(appId);
        SE_alert.setTimestamp(System.currentTimeMillis());
        SE_alert.setTenant(Tenant.ATOS.getId());

        List<Condition> conditions = Lists.newArrayList();
        conditions.add(new Condition(DataID.UNSPECIFIED, Operator.EQ, 1.0));
        SE_alert.setConditions(conditions);

        List<UserRequest> userRequests = Lists.newArrayList();
        for (String feedback : feedbacks) {
            ClassificationResult classification = null;
            try {
                classification = feedbackClassifier.classify(pathToClassificationModel,new UserFeedback(feedback));
            } catch (Exception e) {
                e.printStackTrace();
            }
            RequestClassification rq = null;
            System.out.println(classification.getLabel().toLowerCase());
            switch (classification.getLabel().toLowerCase()) {
                case "enhancement": {
                    rq = RequestClassification.EnhancementRequest;
                    break;
                }
                case "defect": {
                    rq = RequestClassification.BugFixRequest;
                    break;
                }
                case "feature": {
                    rq = RequestClassification.FeatureRequest;
                    break;
                }
                case "other": {
                    rq = RequestClassification.Other;
                    break;
                }
            }

            SentimentAnalyzer sa = new MLSentimentAnalyzer();
            SentimentAnalysisResult saRes = null;
            try {
                saRes = sa.classify(pathToSentimentAnalysisModel,new UserFeedback(feedback));
            } catch (Exception e) {
                e.printStackTrace();
            }

            userRequests.add(new UserRequest(UUID.randomUUID().toString(),rq,classification.getAccuracy(),
                    org.apache.commons.lang.StringEscapeUtils.escapeHtml(feedback),(int)(saRes.getPositiveSentiment()*100),
                    (int)(saRes.getNegativeSentiment()*100),(int)(saRes.getOverallSentiment()*100), new String[0],new String[0]));

        }
        SE_alert.setRequests(userRequests);
        System.out.println(new Gson().toJson(SE_alert));
        try {
            EvolutionPublisher publisher = new EvolutionPublisher(true,"development");
            publisher.publishEvolutionAlertMesssage(SE_alert);
            publisher.closeTopicConnection();
        } catch (Exception e) {
            e.printStackTrace();
                e.printStackTrace();
        }

    }

    private static Map<String, String> operators = ImmutableMap.<String,String>builder()
            .put("EQUAL", "==")
            .put("NOT_EQUAL","!=")
            .put("GREATER_THAN",">")
            .put("GREATER_OR_EQUAL",">=")
            .put("LESS_THAN","<")
            .put("LESS_OR_EQUAL","<=")
            .build();

    public static List<String> evaluateOntologicalDistanceRule(String operator, String ruleValue, String[] values, List<Parameter> parameters, String tenant) {
        List<String> res = Lists.newArrayList();

        operator = operators.get(operator);
        System.out.println("evaluateOntologicalDistanceRule("+operator+","+ruleValue+","+ Arrays.toString(values)+", "+parameters);
        int nRules = 0;
        Set<String> keywords = Sets.newHashSet(parameters.stream().filter(kv -> kv.getKey().equals("keyword"))
                .map(kv -> kv.getValue()).collect(Collectors.toList()));
        String ontologyFile = "ATOS_ontology_1_1.ttl";
        boolean classLabelsOnly = false;
        boolean direct = true;
        String language = "en";
        String wordnetDbPath = "/home/snadal/UPC/Sergi/SUPERSEDE/Development/big_data/data_analysis/StreamingAnalysis/WordNet-3.0-dict";
        FeedbackAnnotator feedbackAnnotator = new FeedbackAnnotator(ontologyFile, wordnetDbPath, language, classLabelsOnly, direct);
        for (String str : values) {
            int isNegative = ConditionEvaluator.evaluateEnglishOverallSentimentRule("LESS_THAN",String.valueOf(0),
                    new String[]{str});

            double ontologicalDistance = feedbackAnnotator.ontologicalDistance(new UserFeedback(str), keywords);
            switch (operator) {
                case "==":
                    if (ontologicalDistance == Double.valueOf(ruleValue) && isNegative==1) {
                        ++nRules; res.add(str);
                    }
                    break;
                case ">":
                    if (ontologicalDistance > Double.valueOf(ruleValue) && isNegative==1) {
                        ++nRules; res.add(str);
                    }
                    break;
                case "<":
                    if (ontologicalDistance < Double.valueOf(ruleValue) && isNegative==1) {
                        ++nRules; res.add(str);
                    }
                    break;
                case ">=":
                    if (ontologicalDistance >= Double.valueOf(ruleValue) && isNegative==1) {
                        ++nRules; res.add(str);
                    }
                    break;
                case "<=":
                    if (ontologicalDistance <= Double.valueOf(ruleValue) && isNegative==1) {
                        ++nRules; res.add(str);
                    }
                    break;
                default: break;
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        List<String> allJsons = Lists.newArrayList();
        String json = "";
        for (String l : Files.lines(new File("/home/snadal/Desktop/atos/6e1cc9e2-5bd1-4fd4-8509-75b3c4e40e1c.txt").toPath()).collect(Collectors.toList())) {
            json = (l.replace("\n",""));
            allJsons.add(json);
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

        List<Parameter> parameters1 = Lists.newArrayList();
        Parameter p11 = new Parameter();p11.setKey("keyword");p11.setValue("network");
        Parameter p12 = new Parameter();p12.setKey("keyword");p12.setValue("connection");
        Parameter p13 = new Parameter();p13.setKey("keyword");p13.setValue("video");
        parameters1.add(p11);parameters1.add(p12);parameters1.add(p13);
        List<String> resRule1 = evaluateOntologicalDistanceRule("EQUAL",String.valueOf(Double.valueOf("3")),
                Iterables.toArray(allFeedbacks, String.class), parameters1, "atos");

        List<Parameter> parameters2 = Lists.newArrayList();
        Parameter p21 = new Parameter();p21.setKey("keyword");p21.setValue("network");
        Parameter p22 = new Parameter();p22.setKey("keyword");p22.setValue("connection");
        Parameter p23 = new Parameter();p23.setKey("keyword");p23.setValue("load");
        Parameter p24 = new Parameter();p24.setKey("keyword");p24.setValue("time");
        parameters2.add(p21);parameters2.add(p22);parameters2.add(p23);parameters2.add(p24);
        List<String> resRule2 = evaluateOntologicalDistanceRule("EQUAL",String.valueOf(Double.valueOf("4")),
                Iterables.toArray(allFeedbacks, String.class), parameters2, "atos");

        List<Parameter> parameters3 = Lists.newArrayList();
        Parameter p31 = new Parameter();p31.setKey("keyword");p31.setValue("statistics");
        parameters3.add(p31);
        List<String> resRule3 = evaluateOntologicalDistanceRule("EQUAL",String.valueOf(Double.valueOf("1")),
                Iterables.toArray(allFeedbacks, String.class), parameters3, "atos");

        List<Parameter> parameters4 = Lists.newArrayList();
        Parameter p41 = new Parameter();p41.setKey("keyword");p41.setValue("flash");
        Parameter p42 = new Parameter();p42.setKey("keyword");p42.setValue("player");
        Parameter p43 = new Parameter();p43.setKey("keyword");p43.setValue("compatible");
        parameters4.add(p41);parameters4.add(p42);parameters4.add(p43);
        List<String> resRule4 = evaluateOntologicalDistanceRule("GREATER_OR_EQUAL",String.valueOf(Double.valueOf("2")),
                Iterables.toArray(allFeedbacks, String.class), parameters4, "atos");

        List<Parameter> parameters5 = Lists.newArrayList();
        Parameter p51 = new Parameter();p51.setKey("keyword");p51.setValue("flash");
        Parameter p52 = new Parameter();p52.setKey("keyword");p52.setValue("player");
        Parameter p53 = new Parameter();p53.setKey("keyword");p53.setValue("version");
        parameters5.add(p51);parameters5.add(p52);parameters5.add(p53);
        List<String> resRule5 = evaluateOntologicalDistanceRule("GREATER_OR_EQUAL",String.valueOf(Double.valueOf("2")),
                Iterables.toArray(allFeedbacks, String.class), parameters5, "atos");


        sendAlert(resRule1,
                "Rule #1: Video connectivity issues (SENTIMENT(f) = 'NEG' && ONTOLOGY_DISTANCE(f, [\"network\", \"connection\", \"video\"]) = 3)",
                "Atos Smart Player feedback analysis Y3 validation");
        sendAlert(resRule2,
                "Rule #2: Loading time issues (SENTIMENT(f) = 'NEG' && ONTOLOGY_DISTANCE(f, [\"network\", \"connection\", \"load\", \"time\"]) = 4)",
                "Atos Smart Player feedback analysis Y3 validation");
        sendAlert(resRule3,
                "Rule #3: Improve the statistics page (SENTIMENT(f) = 'NEG' && ONTOLOGY_DISTANCE(f, [\"statistics\"]) = 1)",
                "Atos Smart Player feedback analysis Y3 validation");
        sendAlert(resRule4,
                "Rule #4: Flash compatibility (I) (SENTIMENT(f) = 'NEG' && ONTOLOGY_DISTANCE(f, [\"flash\", \"player\", \"compatible\"]) >= 2)",
                "Atos Smart Player feedback analysis Y3 validation");
        sendAlert(resRule5,
                "Rule #5: Flash compatibility (II) (SENTIMENT(f) = 'NEG' && ONTOLOGY_DISTANCE(f, [\"flash\", \"player\", \"version\"]) >= 2)",
                "Atos Smart Player feedback analysis Y3 validation");    }
}
