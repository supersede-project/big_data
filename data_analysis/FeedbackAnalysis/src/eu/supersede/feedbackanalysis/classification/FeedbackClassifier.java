package eu.supersede.feedbackanalysis.classification;


import java.util.List;

import weka.classifiers.Classifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;


public interface FeedbackClassifier {

	/**
	 * Trains a Feedback Classifier model reading the training set from pathIn and storing the model in pathOut
	 *
	 * @param pathIn file with the training set
	 * @param pathOut file where to store the model
	 * @return true if everything went fine
	 * @throws Exception
	 */
	public boolean train (String pathIn, String pathOut) throws Exception;

	/**
	 * Classifies the list of input user feedbacks
	 *
	 * @param userFeedback
	 * @return
	 * @throws Exception
	 */
	public List<ClassificationResult> classify(List<UserFeedback> userFeedback) throws Exception;

	// TODO: to be removed
	public Classifier train (String path, boolean arff) throws Exception;
}
