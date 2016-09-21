/**
 * 
 */
package eu.supersede.feedbackanalysis.feature;

import java.util.ArrayList;
import java.util.List;

import eu.supersede.feedbackanalysis.ds.FeatureExtractionResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class SimpleFeatureExtractor implements FeatureExtractor {

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.feature.FeatureExtractor#extractFeatures(java.util.List)
	 */
	@Override
	public List<FeatureExtractionResult> extractFeatures(List<UserFeedback> userFeedbacks) {
		List<FeatureExtractionResult> results = new ArrayList<FeatureExtractionResult>();
		
		// FIXME just a simple place holder
		for (UserFeedback userFeedback : userFeedbacks){
			FeatureExtractionResult result = new FeatureExtractionResult();
			List<String> features = new ArrayList<String>();
			features.add(userFeedback.getFeedbackText().split("\\s+")[0]);  // return the first word of the text
			result.setFeatures(features);
			results.add(result);
		}
		return results;
	}

}
