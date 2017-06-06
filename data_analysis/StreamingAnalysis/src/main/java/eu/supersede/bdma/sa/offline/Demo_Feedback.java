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
import eu.supersede.feedbackanalysis.translation.Translator;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.pubsub.evolution.EvolutionPublisher;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 20/03/17.
 */
public class Demo_Feedback {

    //private static String pathToKW = "/home/snadal/UPC/Sergi/SUPERSEDE/Development/big_data/data_analysis/FeedbackAnalysis/src/main/resources/search_keywords.txt";

    private static PrintStream originalStream = System.out;
    private static PrintStream dummyStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    });

    public static void main(String[] args) {
        //System.setOut(dummyStream);

        SparkSession spark = SparkSession.builder().master("local[*]").appName("Senercon_Y2Validation").getOrCreate();

        JavaRDD<String> feedbacks = spark.sparkContext().textFile("/home/snadal/UPC/Sergi/SUPERSEDE/Y2 Validation/Review_Meeting/feedback_with_rating_less_than_three.json",1).toJavaRDD();

        JavaRDD<String> allFeedbacks = feedbacks.flatMap(f -> {
            List<String> texts = Lists.newArrayList();

            Utils.extractFeatures(f,"http://www.BDIOntology.com/global/Feature/textFeedbacks/text").forEach(extractedElement -> {
                String original = extractedElement;
                texts.add(original);
            });
            return texts.iterator();
        }).distinct();

        JavaPairRDD<String,String> feedbackByType = allFeedbacks.mapToPair(f -> {
            FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
            String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
            String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
            String pathToFeatureExtractor = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

            ClassificationResult classification = null;
            try {
                classification = feedbackClassifier.classify(pathToClassificationModel,new UserFeedback(f));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Tuple2<String,String>(classification.getLabel(),f);
        });

        feedbackByType.groupByKey().foreach(f -> {
            Alert SE_alert = new Alert();
            SE_alert.setId(UUID.randomUUID().toString());
            SE_alert.setApplicationId("Demo");
            SE_alert.setTimestamp(System.currentTimeMillis());
            SE_alert.setTenant("atos");

            // The third attribute should accept strings
            List<Condition> conditions = Lists.newArrayList();
            conditions.add(new Condition(DataID.UNSPECIFIED, Operator.EQ, 1.0));
            SE_alert.setConditions(conditions);

            List<UserRequest> userRequests = Lists.newArrayList();
            f._2().forEach(feedback -> {
                FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
                String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
                String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("NB-3-scale-translated-mixed.model").toString().replace("file:","");
                //String pathToFeatureExtractor = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

                ClassificationResult classification = null;
                try {
                    classification = feedbackClassifier.classify(pathToClassificationModel,new UserFeedback(feedback));
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
                    saRes = sa.classify(pathToSentimentAnalysisModel,new UserFeedback(feedback));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                userRequests.add(new UserRequest(UUID.randomUUID().toString(),rq,classification.getAccuracy(),feedback,(int)(saRes.getPositiveSentiment()*100),
                        (int)(saRes.getNegativeSentiment()*100),(int)(saRes.getOverallSentiment()*100), new String[0],new String[0]));
            });

            SE_alert.setRequests(userRequests);


            EvolutionPublisher publisher = new EvolutionPublisher(true);
            publisher.publishEvolutionAlertMesssage(SE_alert);
            publisher.closeTopicConnection();
        });

    }
}
