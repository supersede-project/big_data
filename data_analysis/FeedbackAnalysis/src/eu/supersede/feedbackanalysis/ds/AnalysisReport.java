package eu.supersede.feedbackanalysis.ds;

import java.util.List;


public class AnalysisReport {
	private List<UserFeedback> userFeedbacks;
	private List<FeatureExtractionResult> featureExtractionResult;
	private List<SentimentAnalysisResult> sentimentAnalysisResult;
	private List<ClassificationResult> classificationResult;
	
	public String getSummary(){
		StringBuffer summary = new StringBuffer();
		for (int i = 0; i < userFeedbacks.size(); i++){
			ClassificationResult classifyResult = classificationResult.get(i);
			SentimentAnalysisResult sentimentResult = sentimentAnalysisResult.get(i);
			FeatureExtractionResult featureResult = featureExtractionResult.get(i);
			
			summary.append("Feedback text: " + userFeedbacks.get(i).getFeedbackText() + "\n");
			summary.append("Label: " + classifyResult.getLabel() + "(" + classifyResult.getAccuracy() * 100 + "%) \n");
			summary.append("Overallsentiment: " + sentimentResult.getOverallSentiment() + "\n");
			summary.append("Main topics: ");
			for (String feature : featureResult.getFeatures()){
				summary.append(feature + ", ");
			}
			summary.append("\n");
		}
		return summary.toString();
	}
	
	public String getJSON(){
		StringBuffer json = new StringBuffer();
		json.append("{\n");
		for (int i = 0; i < userFeedbacks.size(); i++){
			ClassificationResult classifyResult = classificationResult.get(i);
			SentimentAnalysisResult sentimentResult = sentimentAnalysisResult.get(i);
			FeatureExtractionResult featureResult = featureExtractionResult.get(i);
			
			json.append("\"result\":{\n");
			json.append("\"description\": \"" + userFeedbacks.get(i).getFeedbackText() + "\",\n");
			json.append("\"classification\": \"" + classifyResult.getLabel() + "\",\n");
			json.append("\"classification_accuracy\":" + classifyResult.getAccuracy() + ",\n");
			json.append("\"sentiment\": {\n");
			json.append("\"positive\": " + sentimentResult.getPositiveSentiment() + ",\n");
			json.append("\"negative\": " + sentimentResult.getNegativeSentiment() + ",\n");
			json.append("\"overall\": " + sentimentResult.getOverallSentiment() + ",\n");
			json.append("},\n");
			json.append("\"main_topics\": [");
			List<String> features = featureResult.getFeatures();
			for (int j = 0; j < features.size(); j++){
				String feature = features.get(j);
				if (j > 0){
					json.append(",");
				}
				json.append("\"" + feature + "\"");
			}
			json.append("],\n},\n");
		}
		json.append("\n}");
		return json.toString();
	}
	
	public List<FeatureExtractionResult> getFeatureExtractionResult() {
		return featureExtractionResult;
	}
	public void setFeatureExtractionResult(List<FeatureExtractionResult> featureExtractionResult) {
		this.featureExtractionResult = featureExtractionResult;
	}
	public List<SentimentAnalysisResult> getSentimentResult() {
		return sentimentAnalysisResult;
	}
	public void setSentimentResult(List<SentimentAnalysisResult> sentimentResult) {
		this.sentimentAnalysisResult = sentimentResult;
	}
	public List<ClassificationResult> getClassificationResult() {
		return classificationResult;
	}
	public void setClassificationResult(List<ClassificationResult> classificationResult) {
		this.classificationResult = classificationResult;
	}

	public List<UserFeedback> getUserFeedbacks() {
		return userFeedbacks;
	}

	public void setUserFeedbacks(List<UserFeedback> userFeedbacks) {
		this.userFeedbacks = userFeedbacks;
	}
	
}
