package eu.supersede.feedbackanalysis.sentiment;

import java.util.List;

import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

public interface SentimentAnalyzer {

	/**
	 * Trains a Sentiment Analysis model reading the training set from pathIn and storing the model in pathOut
	 * This method is meant to be called in a batch manner (e.g., once a day)

	 * @param pathIn file with the training set
	 * @param pathOut file where to store the model
	 * @return true if everything went fine
	 * @throws Exception
	 */
	public boolean train (String pathIn, String pathOut) throws Exception;

	/**
	 * Classifies the list of input user feedbacks evaluating the model stored in modelPath
	 * This method is meant to be called in a real-time manner (i.e., as soon as one feedback arrives)

	 * @param modelPath the path where the model is stored
	 * @param userFeedback a user feedback to analyze
	 * @return sentiment analysis result for userFeedback
	 * @throws Exception
	 */
	public SentimentAnalysisResult classify(String modelPath, UserFeedback userFeedback) throws Exception;


	// TODO: to be removed
	public List<SentimentAnalysisResult> determineSentiment(List<UserFeedback> userFeedback);
}
