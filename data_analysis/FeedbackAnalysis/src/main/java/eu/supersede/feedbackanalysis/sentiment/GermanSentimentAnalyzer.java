/**
 * 
 */
package eu.supersede.feedbackanalysis.sentiment;

import eu.fbk.ict.fm.nlp.synaptic.classification.sa.SentimentClassify;
import eu.fbk.ict.fm.nlp.synaptic.classification.sa.SentimentLearn;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class GermanSentimentAnalyzer implements SentimentAnalyzer {

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer#train(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean train(String pathIn, String pathOut) throws Exception {
		SentimentLearn sentimentLearn = new SentimentLearn();
		sentimentLearn.run(pathIn, pathOut);
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer#classify(java.lang.String, eu.supersede.feedbackanalysis.ds.UserFeedback)
	 */
	@Override
	public SentimentAnalysisResult classify(String modelPath, UserFeedback userFeedback) throws Exception {
		SentimentClassify sentimentClassify = new SentimentClassify(modelPath);
		String[] outcome = sentimentClassify.run(userFeedback.getFeedbackText());
		SentimentAnalysisResult saResult = new SentimentAnalysisResult();
		// map verbal sentiment to numerical value
		double sentiment = 0;
		switch(outcome[0]) {
		case "negative" : sentiment = -1;
			break;
		case "positive" : sentiment = 1;
		}
		saResult.setOverallSentiment(sentiment);
		return saResult;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
