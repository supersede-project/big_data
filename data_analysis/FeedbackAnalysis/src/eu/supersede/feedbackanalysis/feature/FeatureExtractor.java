package eu.supersede.feedbackanalysis.feature;


import java.util.List;

import eu.supersede.feedbackanalysis.ds.FeatureExtractionResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;


public interface FeatureExtractor {
	public List<FeatureExtractionResult> extractFeatures(List<UserFeedback> userFeedback);
}
