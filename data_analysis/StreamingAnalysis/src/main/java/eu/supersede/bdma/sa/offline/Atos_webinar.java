package eu.supersede.bdma.sa.offline;

import com.clearspring.analytics.util.Lists;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class Atos_webinar {

    public static void analyzeFeedback(String s) throws Exception {


        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        //String pathToClassificationModel = "/home/snadal/UPC/Sergi/SUPERSEDE/Development/big_data/data_analysis/StreamingAnalysis/src/main/resources/";

        String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

        System.out.println(pathToClassificationModel);
        ClassificationResult classification = feedbackClassifier.classify(pathToClassificationModel, new UserFeedback(s));
        SentimentAnalyzer sa = new MLSentimentAnalyzer();
        SentimentAnalysisResult saRes = sa.classify(pathToSentimentAnalysisModel,new UserFeedback(s));

        s += "\n";
        s += "Classifier: "+classification.getLabel() +"\n";
        s += "Sentiment: "+saRes.getOverallSentiment() + "\n";
        s += "################################## \n";
        System.out.println(s);
//        com.google.common.io.Files.append(s,new File("/home/snadal/Desktop/webinar.txt"), Charset.defaultCharset());
    }

    public static void main(String[] args) throws Exception {
        SparkConf conf = new SparkConf().setAppName("atos_webinar").setMaster("local[*]");
        JavaSparkContext context = new JavaSparkContext(conf);

        List<String> allJsons = Lists.newArrayList();
        String json = "";
        for (String l : Files.lines(new File("/home/snadal/UPC/Sergi/SUPERSEDE/T2.1/webinar_feedback_final.json").toPath()).collect(Collectors.toList())) {
            allJsons.add(l);
            /*json += (l.replace("\n",""));
            try {
                JSONObject a = (JSONObject)JSONValue.parse(json.substring(0,json.length()-1));
                if (a != null) {
                    allJsons.add(json);
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
                if (feedback.contains("lash")) {
                    try {
                        analyzeFeedback(feedback);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("################");
                }

            }
        });

    }
}
