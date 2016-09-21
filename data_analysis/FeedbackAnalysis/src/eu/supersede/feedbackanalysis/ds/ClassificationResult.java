package eu.supersede.feedbackanalysis.ds;

public class ClassificationResult {
	private String label;
	private double accuracy;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
}
