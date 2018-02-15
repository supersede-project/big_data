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
public class GermanSentimentAnalyzerTest {

	
	GermanSentimentAnalyzer germanSentimentAnalyzer;
	File trainFile;
	File modelFile;
	
	UserFeedback userFeedback = new UserFeedback("some german text here");
	
	@Before
	public void setup() {
		String dataset = "src/test/resources/trainingsets/german/supersede-german-trainingset_ALL.tsv"; //"src/test/resources/trainingsets/german_train.tsv";
		String modelPath = "src/test/resources/models/german_sentiment.model"; //"src/test/resources/trainingsets/german_train.tsv.ta.model";
		trainFile = new File(dataset);
		modelFile = new File(modelPath);
		germanSentimentAnalyzer = new GermanSentimentAnalyzer();
	}
	
	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.sentiment.GermanSentimentAnalyzer#train(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testTrain() {
		boolean outcome = true;
		try {
			outcome = germanSentimentAnalyzer.train(trainFile.getAbsolutePath(), modelFile.getAbsolutePath());
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
				SentimentAnalysisResult sentimentAnalysisResult = germanSentimentAnalyzer.classify(modelFile.getAbsolutePath(), userFeedback);
				assertNotNull(sentimentAnalysisResult);
				System.out.println(sentimentAnalysisResult.getOverallSentiment());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
