package eu.supersede.bdma.sa.statisticalAnalysis;

import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.*;
import eu.supersede.feedbackanalysis.feature.FeatureExtractor;
import eu.supersede.feedbackanalysis.feature.SimpleFeatureExtractor;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;


/**
 * Created by snadal on 26/01/17.
 */
public class FeedbackAnalysis {

    public static AnalysisReport getFeedbackAnalysisSummary(String strUserFeedback) {
        String classifierModelPath = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        String sentimentModelPath = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
        String extractedFeaturesPath = "";

        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        FeatureExtractor featureExtractor = new SimpleFeatureExtractor();
        SentimentAnalyzer sentimentAnalyzer = new MLSentimentAnalyzer();

        UserFeedback userFeedback = new UserFeedback(strUserFeedback);

        AnalysisReport report = new AnalysisReport();
        report.setUserFeedback(userFeedback);
        try {
            ClassificationResult classificationResult = feedbackClassifier.classify(classifierModelPath, userFeedback);
            SentimentAnalysisResult sentimentAnalysisResult = sentimentAnalyzer.classify(sentimentModelPath, userFeedback);
            FeatureExtractionResult featureExtractionResult = featureExtractor.single(extractedFeaturesPath, userFeedback);

            report.setClassificationResult(classificationResult);
            report.setSentimentResult(sentimentAnalysisResult);
            report.setFeatureExtractionResult(featureExtractionResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return report;
    }
}
