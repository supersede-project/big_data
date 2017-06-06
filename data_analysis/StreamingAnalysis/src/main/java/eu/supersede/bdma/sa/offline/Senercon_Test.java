package eu.supersede.bdma.sa.offline;

import com.google.common.collect.Lists;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.feedbackanalysis.translation.Translator;
import eu.supersede.integration.api.dm.types.RequestClassification;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import sun.nio.cs.UTF_32;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;


/**
 * Created by snadal on 20/03/17.
 */
public class Senercon_Test {

    private static String TRACEABILITY_FILE = "/home/snadal/Desktop/KPI_Traceability_Senercon.xml";

    private static void writeToFile(String theText) {
        try {
            Files.write(Paths.get(TRACEABILITY_FILE), Lists.newArrayList(theText), UTF_8, APPEND, CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {



        SparkSession spark = SparkSession.builder().master("local[*]").appName("Senercon_Y2Validation").getOrCreate();

        JavaRDD<String> feedbacks = spark.sparkContext().textFile("/home/snadal/Desktop/senercon_feedback_final.json",1).toJavaRDD();

        writeToFile("<traceability>");

        feedbacks.foreach(f -> {
            writeToFile("<feedback userId='"+Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/userIdentification").get(0)+
                    "' applicationId='"+Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/applicationId").get(0)+"'" +
                    " date='"+Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/contextInformation/localTime").get(0)+"'>");

            writeToFile("<contextInformation>");
            writeToFile("<userAgent>"+Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/contextInformation/userAgent").get(0)+"</userAgent>");
            writeToFile("<resolution>"+Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/contextInformation/resolution").get(0)+"</resolution>");
            writeToFile("</contextInformation>");

            if (((JSONArray)JSONValue.parse(((JSONObject) JSONValue.parse(f)).getAsString("attachmentFeedbacks"))).size() > 0) {
                writeToFile("<attachmentFeedbacks>");
                Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/attachmentFeedbacks/name").forEach(extractedElement -> {
                    writeToFile("<name>" + extractedElement + "</name>");
                });
                writeToFile("</attachmentFeedbacks>");
            }
            if (((JSONArray)JSONValue.parse(((JSONObject) JSONValue.parse(f)).getAsString("ratingFeedbacks"))).size() > 0) {
                writeToFile("<ratingFeedbacks>");
                Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/ratingFeedbacks/rating").forEach(extractedElement -> {
                    writeToFile("<rating>" + extractedElement + "</rating>");
                });
                writeToFile("</ratingFeedbacks>");
            }
            if (((JSONArray)JSONValue.parse(((JSONObject) JSONValue.parse(f)).getAsString("screenshotFeedbacks"))).size() > 0) {
                writeToFile("<screenshotFeedbacks>");
                Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/screenshotFeedbacks/path").forEach(extractedElement -> {
                    writeToFile("<path>" + extractedElement + "</path>");
                });
                writeToFile("</screenshotFeedbacks>");
            }
            if (((JSONArray)JSONValue.parse(((JSONObject) JSONValue.parse(f)).getAsString("textFeedbacks"))).size() > 0) {
                writeToFile("<textFeedbacks>");
                Utils.extractFeatures(f, "http://www.BDIOntology.com/global/Feature/textFeedbacks/text").forEach(extractedElement -> {
                    writeToFile("<originalFeedback>" + extractedElement.replace("&","&amp;") + "</originalFeedback>");
                    writeToFile("<translatedFeedback>" + new Translator().translate(extractedElement).replace("&","&amp;") + "</translatedFeedback>");

                    FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
                    String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
                    String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("NB-3-scale-translated-mixed.model").toString().replace("file:","");
                    //String pathToFeatureExtractor = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

                    ClassificationResult classification = null;
                    try {
                        classification = feedbackClassifier.classify(pathToClassificationModel,new UserFeedback(extractedElement));
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
                        saRes = sa.classify(pathToSentimentAnalysisModel,new UserFeedback(extractedElement));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    writeToFile("<feedbackAnalysis>");

                    writeToFile("<classification accuracy='" + classification.getAccuracy() + "'>" + classification.getLabel() + "</classification>");
                    writeToFile("<sentiment overall='"+saRes.getOverallSentiment()+"' positive='"+saRes.getPositiveSentiment()
                            +"' negative='"+saRes.getNegativeSentiment()+"' />");

                    writeToFile("</feedbackAnalysis>");
                });
                writeToFile("</textFeedbacks>");
            }

            writeToFile("</feedback>");
        });

        writeToFile("</traceability>");

    }
}
