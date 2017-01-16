package eu.supersede.feedbackanalysis.classification;


import java.util.List;

import weka.classifiers.Classifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;


public interface FeedbackClassifier {

	/**
	 * Trains a Feedback Classifier model reading the training set from pathIn and storing the model in pathOut
	 * The training set in pathIn is in ARFF format
	 * This method is meant to be called in a batch manner (e.g., once a day)
	 *
	 * @param pathIn file with the training set
	 * @param pathOut file where to store the model
	 * @return true if everything went fine
	 * @throws Exception
	 */
	public boolean train(String pathIn, String pathOut) throws Exception;

	/**
	 * Classifies the input user feedback by evaluating the model stored in modelPath
	 * This method is meant to be called in a real-time manner (i.e., as soon as one feedback arrives)

	 * @param modelPath the path where the model is stored
	 * @param userFeedback a user feedback to analyze
	 * @return classification result for userFeedback
	 * @throws Exception
	 */
	public ClassificationResult classify(String modelPath, UserFeedback userFeedback) throws Exception;

	// TODO: to be removed
	public Classifier train (String path, boolean arff) throws Exception;
}
