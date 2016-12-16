package eu.supersede.feedbackanalysis.ds;

import java.util.List;


public class AnalysisReport {
	private UserFeedback userFeedback;
	private FeatureExtractionResult featureExtractionResult;
	private SentimentAnalysisResult sentimentAnalysisResult;
	private ClassificationResult classificationResult;
	
	public String getSummary(){
//		FeatureExtractionResult featureResult = featureExtractionResult;
		StringBuffer summary = new StringBuffer();
//		for (int i = 0; i < userFeedbacks.size(); i++){
//			ClassificationResult classifyResult = classificationResult.get(i);
//			SentimentAnalysisResult sentimentResult = sentimentAnalysisResult.get(i);
			
		summary.append("Feedback text: " + userFeedback.getFeedbackText() + "\n");
		summary.append("Label: " + classificationResult.getLabel() + "(" + classificationResult.getAccuracy() * 100 + "%) \n");
		summary.append("Overallsentiment: " + sentimentAnalysisResult.getOverallSentiment() + "\n");
		summary.append("Main topics: ");
		for (String feature : featureExtractionResult.getFeatures()){
			summary.append(feature + ", ");
		}
		summary.append("\n");
//		}
		return summary.toString();
	}
	
	public String getJSON(){
		StringBuffer json = new StringBuffer();
		json.append("{\n");
//		FeatureExtractionResult featureResult = featureExtractionResult;
//		for (int i = 0; i < userFeedbacks.size(); i++){
//			ClassificationResult classifyResult = classificationResult.get(i);
//			SentimentAnalysisResult sentimentResult = sentimentAnalysisResult.get(i);
			
		json.append("\"result\":{\n");
		json.append("\"description\": \"" + userFeedback.getFeedbackText() + "\",\n");
		json.append("\"classification\": \"" + classificationResult.getLabel() + "\",\n");
		json.append("\"classification_accuracy\":" + classificationResult.getAccuracy() + ",\n");
		json.append("\"sentiment\": {\n");
		json.append("\"positive\": " + sentimentAnalysisResult.getPositiveSentiment() + ",\n");
		json.append("\"negative\": " + sentimentAnalysisResult.getNegativeSentiment() + ",\n");
		json.append("\"overall\": " + sentimentAnalysisResult.getOverallSentiment() + ",\n");
		json.append("},\n");
		json.append("\"main_topics\": [");
		List<String> features = featureExtractionResult.getFeatures();
		for (int j = 0; j < features.size(); j++){
			String feature = features.get(j);
			if (j > 0){
				json.append(",");
			}
			json.append("\"" + feature + "\"");
		}
		json.append("],\n},\n");
//		}
		json.append("\n}");
		return json.toString();
	}
	
	public FeatureExtractionResult getFeatureExtractionResult() {
		return featureExtractionResult;
	}
	public void setFeatureExtractionResult(FeatureExtractionResult featureExtractionResult2) {
		this.featureExtractionResult = featureExtractionResult2;
	}
	public SentimentAnalysisResult getSentimentResult() {
		return sentimentAnalysisResult;
	}
	public void setSentimentResult(SentimentAnalysisResult sentimentResult) {
		this.sentimentAnalysisResult = sentimentResult;
	}
	public ClassificationResult getClassificationResult() {
		return classificationResult;
	}
	public void setClassificationResult(ClassificationResult classificationResult) {
		this.classificationResult = classificationResult;
	}

	public UserFeedback getUserFeedback() {
		return userFeedback;
	}

	public void setUserFeedback(UserFeedback userFeedback) {
		this.userFeedback = userFeedback;
	}
	
}
