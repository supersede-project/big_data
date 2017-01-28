package eu.supersede.feedbackanalysis.ds;

public class UserFeedback {
	private String feedbackText;
	private ClassificationResult classificationResult;
	
	/**
	 * 
	 */
	public UserFeedback() {
		// TODO Auto-generated constructor stub
	}
	
	public UserFeedback(String text) {
		feedbackText = text;
	}
	
	public String getFeedbackText() {
		return feedbackText;
	}

	public void setFeedbackText(String feedbackText) {
		this.feedbackText = feedbackText;
	}

	public ClassificationResult getClassificationResult() {
		return classificationResult;
	}

	public void setClassificationResult(ClassificationResult classificationResult) {
		this.classificationResult = classificationResult;
	}
	
}
