package eu.supersede.feedbackanalysis.sentiment;

import java.util.List;

import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

public interface SentimentAnalyzer {
	public List<SentimentAnalysisResult> determineSentiment(List<UserFeedback> userFeedback);
}
