package eu.supersede.bdma.sa.offline;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
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
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class Atos_webinar_final {


    public static void main(String[] args) throws Exception {
        SparkConf conf = new SparkConf().setAppName("atos_webinar").setMaster("local[*]");
        JavaSparkContext context = new JavaSparkContext(conf);

        List<String> allJsons = Lists.newArrayList();
        String json = "";
        for (String l : Files.lines(new File("/home/snadal/UPC/Sergi/SUPERSEDE/T2.1/webinar_feedback_final.json").toPath()).collect(Collectors.toList())) {
            allJsons.add(l);
        }

        List<String> feedbacks = Lists.newArrayList();
        allJsons.forEach(j -> {
            feedbacks.add(Utils.extractFeatures(j,"Attributes/textFeedbacks/text").get(0));
        });

        ECA_Rule rule = MDMProxy.getRules().stream().filter(r -> r.getName().contains("sentiment")).collect(Collectors.toList()).get(0);

        //SoftwareEvolutionAlert.sendAlert(rule, Iterables.toArray(feedbacks,String.class));




    }
}
