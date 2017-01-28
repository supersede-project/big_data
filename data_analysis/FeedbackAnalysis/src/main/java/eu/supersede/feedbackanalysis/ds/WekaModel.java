package eu.supersede.feedbackanalysis.ds;

import weka.classifiers.Classifier;

public class WekaModel { //extends ClassifierModel {
	private Classifier model;

	public Classifier getModel() {
		return model;
	}

	public void setModel(Classifier model) {
		this.model = model;
	}
}
