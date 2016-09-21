package eu.supersede.feedbackanalysis.ds;

public class SentimentAnalysisResult {
	private double overallSentiment;
	private double positiveSentiment;
	private double negativeSentiment;
	public double getOverallSentiment() {
		return overallSentiment;
	}
	public void setOverallSentiment(double overallSentiment) {
		this.overallSentiment = overallSentiment;
	}
	public double getPositiveSentiment() {
		return positiveSentiment;
	}
	public void setPositiveSentiment(double positiveSentiment) {
		this.positiveSentiment = positiveSentiment;
	}
	public double getNegativeSentiment() {
		return negativeSentiment;
	}
	public void setNegativeSentiment(double negativeSentiment) {
		this.negativeSentiment = negativeSentiment;
	}
}
