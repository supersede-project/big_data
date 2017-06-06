package eu.supersede.bdma.sa.offline;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterators;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.KeywordSearchResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.feature.FeatureExtractor;
import eu.supersede.feedbackanalysis.feature.SimpleFeatureExtractor;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.feedbackanalysis.twitter.keywordsearch.SimpleKeywordSearcher;
import eu.supersede.integration.api.dm.types.*;
import eu.supersede.integration.api.pubsub.SubscriptionTopic;
import eu.supersede.integration.api.pubsub.TopicPublisher;
import eu.supersede.integration.api.pubsub.evolution.EvolutionPublisher;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 20/03/17.
 */
public class Atos_Twitter {

    private static String pathToKW = "/home/snadal/UPC/Sergi/SUPERSEDE/Development/big_data/data_analysis/FeedbackAnalysis/src/main/resources/search_keywords.txt";

    private static PrintStream originalStream = System.out;
    private static PrintStream dummyStream = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    });

    public static void main(String[] args) {
        System.setOut(dummyStream);

        SparkSession spark = SparkSession.builder().master("local[*]").appName("AtoS_Twitter").getOrCreate();

        JavaRDD<Tuple2<String,String>> files = spark.sparkContext().wholeTextFiles("/home/snadal/UPC/Sergi/SUPERSEDE/Data/AtoS/twitterOlympics/",1).toJavaRDD();
        JavaRDD<String> AllTweets = files.map(f -> f._2()).flatMap(f -> Arrays.asList(f.split("\n")).iterator())./*sample(false,0.1).*/flatMap(f -> {
            List<String> tweets = Lists.newArrayList();
            Utils.extractFeatures(f,"http://www.BDIOntology.com/global/Feature/SocialNetworksMonitoredData/DataItems/message").forEach(extractedElement -> {
                tweets.add(extractedElement);
            });
            return tweets.iterator();
        }).distinct();


        AllTweets.flatMapToPair(t -> {
            SimpleKeywordSearcher sks = new SimpleKeywordSearcher();

            List<Tuple2<String,String>> rules = Lists.newArrayList();
            UserFeedback uf = new UserFeedback(t);
            KeywordSearchResult res = sks.search(pathToKW,uf);
            for (String kw : res.getFoundKeywords()) {
                rules.add(new Tuple2<String,String>(kw,t));
            }
            return rules.iterator();
        }).groupByKey().foreach(r -> {
            Alert SE_alert = new Alert();
            SE_alert.setId(UUID.randomUUID().toString());
            SE_alert.setApplicationId("Twitter_Atos_OfflineAnalysis");
            SE_alert.setTimestamp(System.currentTimeMillis());
            SE_alert.setTenant("atos");

            // The third attribute should accept strings
            List<Condition> conditions = Lists.newArrayList();
            conditions.add(new Condition(DataID.UNSPECIFIED, Operator.EQ, 1.0));
            SE_alert.setConditions(conditions);

            List<UserRequest> userRequests = Lists.newArrayList();
            r._2().forEach(tweet -> {
                FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
                String pathToClassificationModel = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
                String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
                String pathToFeatureExtractor = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

                ClassificationResult classification = null;
                try {
                    classification = feedbackClassifier.classify(pathToClassificationModel,new UserFeedback(tweet));
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
                    saRes = sa.classify(pathToSentimentAnalysisModel,new UserFeedback(tweet));
                } catch (Exception e) {
                    e.printStackTrace();
                }



                System.out.println(classification.getLabel());
                userRequests.add(new UserRequest(UUID.randomUUID().toString(),rq,classification.getAccuracy(),"Keyword: "+r._1()+" ["+tweet+"]",(int)(saRes.getPositiveSentiment()*100),
                        (int)(saRes.getNegativeSentiment()*100),(int)(saRes.getOverallSentiment()*100), new String[0],new String[0]));
            });

            SE_alert.setRequests(userRequests);

            System.setOut(originalStream);
            System.out.println("Keyword: "+r._1());
            for (UserRequest ur : SE_alert.getRequests()) {
                System.out.println("    "+ur.getDescription());
            }
            System.setOut(dummyStream);



            EvolutionPublisher publisher = new EvolutionPublisher(true);
            publisher.publishEvolutionAlertMesssage(SE_alert);
            System.exit(0);
        });

    }
}
