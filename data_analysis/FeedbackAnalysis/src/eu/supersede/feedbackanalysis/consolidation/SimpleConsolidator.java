/**
 * 
 */
package eu.supersede.feedbackanalysis.consolidation;

import java.util.ArrayList;
import java.util.List;

import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.AnalysisReport;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.FeatureExtractionResult;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.feature.FeatureExtractor;
import eu.supersede.feedbackanalysis.feature.SimpleFeatureExtractor;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentistrengthAnalyzer;

/**
 * @author fitsum
 *
 */
public class SimpleConsolidator implements AnalysisConsolidator {

	
	private FeedbackClassifier feedbackClassifier;
	private FeatureExtractor featureExtractor;
	private SentimentAnalyzer sentimentAnalyzer;
	
	private static SimpleConsolidator instance;
	
	public static SimpleConsolidator getInstance (){
		if (instance == null){
			instance = new SimpleConsolidator();
		}
		return instance;
	}
	
	/**
	 * 
	 */
	private SimpleConsolidator() {
		feedbackClassifier = new SpeechActBasedClassifier();
		featureExtractor = new SimpleFeatureExtractor();
		sentimentAnalyzer = new SentistrengthAnalyzer();
	}
	


	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.consolidation.AnalysisConsolidator#getSummary(java.util.List)
	 */
	@Override
	public AnalysisReport getSummary(List<UserFeedback> userFeedbacks) {
		AnalysisReport report = new AnalysisReport();
		report.setUserFeedbacks(userFeedbacks);
		try {
			List<ClassificationResult> classificationResult = feedbackClassifier.classify(userFeedbacks);
			List<SentimentAnalysisResult> sentimentAnalysisResult = sentimentAnalyzer.determineSentiment(userFeedbacks);
			List<FeatureExtractionResult> featureExtractionResult = featureExtractor.extractFeatures(userFeedbacks);
			
			report.setClassificationResult(classificationResult);
			report.setSentimentResult(sentimentAnalysisResult);
			report.setFeatureExtractionResult(featureExtractionResult);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return report;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//  for testing purposes only 
		
		UserFeedback f1 = new UserFeedback("However since _ XPropertySet ] 21: LOG Execute: getPropertySetInfo 21: Method getPropertySetInfo finished with state OK");
		UserFeedback f2 = new UserFeedback("I would like to have the window minimizing automatically when it loses focus.");
		UserFeedback f3 = new UserFeedback("Automatic window minimize does not work. Please fix it, I really hate it.");
		UserFeedback f4 = new UserFeedback("I would like a new search feature that works on voice commands.");
		
		List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
		userFeedbacks.add(f1);
		userFeedbacks.add(f2);
		userFeedbacks.add(f3);
		userFeedbacks.add(f4);
		
		AnalysisConsolidator consolidator = new SimpleConsolidator();
		AnalysisReport summary = consolidator.getSummary(userFeedbacks);
		System.out.println(summary.getSummary());
		
		
	}
	
}
