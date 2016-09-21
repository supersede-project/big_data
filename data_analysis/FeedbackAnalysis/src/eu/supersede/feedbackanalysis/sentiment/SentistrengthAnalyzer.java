/**
 * 
 */
package eu.supersede.feedbackanalysis.sentiment;

import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.sentistrength.SentiStrength;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class SentistrengthAnalyzer implements SentimentAnalyzer {

	private SentiStrength sentiStrength;
	private String dataFilesPath = "resources/SentStrength_Data/";

	/**
	 * 
	 */
	public SentistrengthAnalyzer() {
		// create a list to hold all the config options
		List<String> config = new ArrayList<String>();

		// set the path to the resources folder
		config.add("sentidata");
		config.add(dataFilesPath);

		// we use the "scale" option when running SentiStrength
		config.add("scale");

		// if debug is on then add the "explain" option as well
		boolean debug = false; // IMPORTANT !! debug must be FALSE, otherwise, we cannot parse the output!
		if (debug)
			config.add("explain");

		// if other options have been provided then add them to the end of the
		// config.add("text");

		// ... so finally we can create...
		sentiStrength = new SentiStrength();

		// ... and initialise the library, unfortunately if this fails we don't
		// get an exception thrown there is just a message printed out
		sentiStrength.initialise(config.toArray(new String[config.size()]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer#determineSentiment
	 * (java.util.List)
	 */
	@Override
	public List<SentimentAnalysisResult> determineSentiment(List<UserFeedback> userFeedbacks) {
		List<SentimentAnalysisResult> analysisResults = new ArrayList<SentimentAnalysisResult>();
		for (UserFeedback userFeedback : userFeedbacks) {
			String sentimentScores = sentiStrength.computeSentimentScores(userFeedback.getFeedbackText());
			String[] strScores = sentimentScores.split("\\s+");
			double[] scores = new double[strScores.length];
			for (int i = 0; i < strScores.length; i++) {
				scores[i] = Double.parseDouble(strScores[i]);
			}
			SentimentAnalysisResult result = new SentimentAnalysisResult();
			result.setPositiveSentiment(scores[0]);
			result.setNegativeSentiment(scores[1]);
			result.setOverallSentiment(scores[2]);

			analysisResults.add(result);
		}
		return analysisResults;
	}

}
