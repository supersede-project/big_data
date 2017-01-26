package eu.supersede.feedbackanalysis.classification;

import java.util.List;

import weka.classifiers.Classifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

public class BagOfWordsBasedClassifier implements FeedbackClassifier {

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.classification.FeedbackClassifier#train(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean train(String pathIn, String pathOut) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.classification.FeedbackClassifier#classify(java.lang.String, eu.supersede.feedbackanalysis.ds.UserFeedback)
	 */
	@Override
	public ClassificationResult classify(String modelPath,
			UserFeedback userFeedback) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
