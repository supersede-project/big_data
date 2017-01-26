/**
 * 
 */
package eu.supersede.feedbackanalysis.sentiment;

import java.util.ArrayList;
import java.util.List;

import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class SentiWordAnalyzer implements SentimentAnalyzer {

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer#train(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean train(String pathIn, String pathOut) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer#classify(java.lang.String, eu.supersede.feedbackanalysis.ds.UserFeedback)
	 */
	@Override
	public SentimentAnalysisResult classify(String modelPath,
			UserFeedback userFeedback) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
