/**
 * 
 */
package eu.supersede.feedbackanalysis.classification;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class GermanFeedbackClassifierTest {

	GermanFeedbackClassifier germanClassifier;
	File trainFile;
	File modelFile;
	
	UserFeedback userFeedback = new UserFeedback("some german text here");
	
	@Before
	public void setup() {
		String dataset = "src/test/resources/trainingsets/german/supersede-german-trainingset_feedbacks_global_since_2009.tsv"; //supersede-german-trainingset_iesa_userfeedback_from_ticket_system_2016.tsv"; //supersede-german-trainingset_ALL.tsv"; //"src/test/resources/trainingsets/german_train.tsv";
		String modelPath = "src/test/resources/models/german_classify_2009.model"; //"src/test/resources/trainingsets/german_train.tsv.ta.model";
		trainFile = new File(dataset);
		modelFile = new File(modelPath);
		germanClassifier = new GermanFeedbackClassifier();
	}
	
	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.classification.GermanFeedbackClassifier#train(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testTrain() {
		boolean outcome = true;
		if (modelFile.exists())
			modelFile.delete();
		try {
			outcome = germanClassifier.train(trainFile.getAbsolutePath(), modelFile.getAbsolutePath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(outcome);
	}

	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.classification.GermanFeedbackClassifier#classify(java.lang.String, eu.supersede.feedbackanalysis.ds.UserFeedback)}.
	 */
	@Test
	public void testClassify() {
		try {
			
			// if model does not exist?
			if (modelFile.exists()) {
				ClassificationResult classificationResult = germanClassifier.classify(modelFile.getAbsolutePath(), userFeedback);
				assertNotNull(classificationResult);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testClassifySenercon() throws Exception {
		String csvFile = "src/test/resources/trainingsets/senercon_german_classification.csv";
		Reader reader = new FileReader(csvFile);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader("label", "text").parse(reader);
		for (CSVRecord record : records) {
			if (!record.get("label").equals("label")) {
				String label = record.get("label");
				String text = record.get("text");
				ClassificationResult classificationResult = germanClassifier.classify(modelFile.getAbsolutePath(), new UserFeedback(text));
				if (!classificationResult.getLabel().equalsIgnoreCase(label)) {
					System.out.println("old ==> new " + label + " ==> " + classificationResult.getLabel());
				}
			}
		}
	}
	
}
