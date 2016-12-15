package eu.supersede.feedbackanalysis.sentiment;

import java.util.List;

import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

public interface SentimentAnalyzer {

	/**
	 * Trains a Sentiment Analysis model reading the training set from pathIn and storing the model in pathOut
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
	public List<SentimentAnalysisResult> classify(List<UserFeedback> userFeedback) throws Exception;


	// TODO: to be removed
	public List<SentimentAnalysisResult> determineSentiment(List<UserFeedback> userFeedback);
}
