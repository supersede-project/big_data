/**
 * 
 */
package eu.supersede.feedbackanalysis.sentiment;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import eu.supersede.feedbackanalysis.classification.GermanFeedbackClassifier;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class MLSentimentAnalyzerTest {

	
	MLSentimentAnalyzer mlSentimentAnalyzer;
	File trainFile;
	File modelFile;
	
	UserFeedback userFeedback = new UserFeedback("I never really liked this piece of software. Please fix it");
	
	@Before
	public void setup() {
		String dataset = "src/test/resources/trainingsets/sentiment_reviews_3_scale.arff";
		String modelPath = "src/test/resources/models/sentiment_reviews_3_scale.arff.model";
		trainFile = new File(dataset);
		modelFile = new File(modelPath);
		mlSentimentAnalyzer = new MLSentimentAnalyzer();
	}
	
	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.sentiment.GermanSentimentAnalyzer#train(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testTrain() {
		boolean outcome = true;
		try {
			outcome = mlSentimentAnalyzer.train(trainFile.getAbsolutePath(), modelFile.getAbsolutePath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(outcome);
	}

	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.sentiment.GermanSentimentAnalyzer#classify(java.lang.String, eu.supersede.feedbackanalysis.ds.UserFeedback)}.
	 */
	@Test
	public void testClassify() {
		try {
			if (modelFile.exists()) {
				SentimentAnalysisResult sentimentAnalysisResult = mlSentimentAnalyzer.classify(modelFile.getAbsolutePath(), userFeedback);
				assertNotNull(sentimentAnalysisResult);
				System.out.println(sentimentAnalysisResult.getOverallSentiment());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
