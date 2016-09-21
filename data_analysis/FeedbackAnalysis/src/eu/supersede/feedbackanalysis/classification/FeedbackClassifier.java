package eu.supersede.feedbackanalysis.classification;


import java.util.List;

import weka.classifiers.Classifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;


public interface FeedbackClassifier {
	
	public Classifier train (String path, boolean arff) throws Exception;
	
	public List<ClassificationResult> classify(List<UserFeedback> userFeedback) throws Exception;
}
