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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Atos_feedback_dump {

    public static void main(String[] args) throws Exception {
        SparkConf conf = new SparkConf().setAppName("atos_webinar").setMaster("local[*]");
        JavaSparkContext context = new JavaSparkContext(conf);

        List<String> allJsons = Lists.newArrayList();
        String json = "";
        for (String l : Files.lines(new File("/home/snadal/Desktop/atos/6e1cc9e2-5bd1-4fd4-8509-75b3c4e40e1c.txt").toPath()).collect(Collectors.toList())) {
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

        AtomicInteger i = new AtomicInteger(0);

        allJsons.forEach(aJSON -> {
            String feedback = "";
            for (String feedbackPiece : Utils.extractFeatures(aJSON,"Attributes/textFeedbacks/text")) {
                if (!feedbackPiece.contains("@")) feedback += " " + feedbackPiece;
            }
            feedback = feedback.replace("\n","");
            if (!feedback.isEmpty()) {
                System.out.println(feedback);
                System.out.println("################");
                i.addAndGet(1);

            }
        });
        System.out.println(i);

    }
}
