package eu.supersede.feedbackanalysis.feature;


import java.util.List;

import eu.supersede.feedbackanalysis.ds.FeatureExtractionResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;


public interface FeatureExtractor {

	/**
	 * Generates the feature file in pathOut given a batch set of feedbacks in pathIn
	 * This method is meant to be called in a batch manner (e.g., once a day)
	 *
	 * @param pathIn file with the training set
	 * @param pathOut file where to store the model
	 * @return true if everything went fine
	 */
	public boolean batch(String pathIn, String pathOut);

	/**
	 * Generates the feature file in pathOut given a batch set of feedbacks in pathIn
	 * This method is meant to be called in a real-time manner (i.e., as soon as one feedback arrives)
	 *
	 * @param featurePath file with the batch of features
	 * @param userFeedback a user feedback to analyze
	 * @return extracted features for userFeedback
	 */
	public FeatureExtractionResult single(String featurePath, UserFeedback userFeedback);
}
