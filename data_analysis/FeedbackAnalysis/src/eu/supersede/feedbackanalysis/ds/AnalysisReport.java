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
