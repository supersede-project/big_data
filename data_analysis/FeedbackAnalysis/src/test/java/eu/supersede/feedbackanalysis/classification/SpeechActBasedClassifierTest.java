package eu.supersede.feedbackanalysis.classification;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.util.MathUtil;
import org.junit.Before;
import org.junit.Test;

import com.opencsv.CSVReader;

import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.preprocessing.utils.*;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;

public class SpeechActBasedClassifierTest {

//	private List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
	
//	@Before
//	public void setUp(){
//		// load training set
//		UserFeedback f1 = new UserFeedback("However since _ XPropertySet ] 21: LOG Execute: getPropertySetInfo 21: Method getPropertySetInfo finished with state OK");
//		UserFeedback f2 = new UserFeedback("I would like to have the window minimizing automatically when it loses focus.");
//		UserFeedback f3 = new UserFeedback("Automatic window minimize does not work. Please fix it.");
//		UserFeedback f4 = new UserFeedback("I would like a new search feature that works on voice commands.");
//		userFeedbacks.add(f1);
//		userFeedbacks.add(f2);
//		userFeedbacks.add(f3);
//		userFeedbacks.add(f4);
//	}
	
	String resourcesDir = Thread.currentThread().getContextClassLoader().getResource(".").toString().replaceAll("file:","");
	String dataset = "ATOS_userfeedback_category"; //"Y2_DEMO_input"; //"SENERCON_userfeedback_from_tool_test"; //"SENERCON_autotranslated_600_3_scale";
	String csvPath = Thread.currentThread().getContextClassLoader().getResource("trainingsets/" + dataset + ".csv").toString().replace("file:",""); //resourcesDir + "/trainingsets/" + dataset + ".csv";
	
	@Test
	public void testTrain() throws Exception {
		AttributeExtractor attributeExtractor = new AttributeExtractor();
		FileManager fileManager = new FileManager();
		String arff = attributeExtractor.getARFF(csvPath);
		fileManager.writeFile(resourcesDir + "trainingsets/", dataset + ".csv.arff", arff);
		String pathIn = resourcesDir + "trainingsets/" + dataset + ".csv.arff";
		String pathOut = resourcesDir + "models/" + dataset + ".rf.model";
		FeedbackClassifier classifier = new SpeechActBasedClassifier();
		classifier.train(pathIn, pathOut);
	}

	@Test
	public void testSMOTEFilter () throws Exception {
		String pathIn = resourcesDir + "trainingsets/" + dataset + ".csv.arff";
		DataSource dataSource = new DataSource(pathIn);
		Instances instances = dataSource.getDataSet();
		instances.setClassIndex(instances.numAttributes() - 1);
		SpeechActBasedClassifier classifier = new SpeechActBasedClassifier();
		instances = classifier.applyOversampling(instances);
		System.out.println(instances.toString());
	}
	
	@Test
	public void testClassify() throws Exception{
		FeedbackClassifier classifier = new SpeechActBasedClassifier();
		String modelPath = Thread.currentThread().getContextClassLoader().getResource("models/" + dataset + ".rf.model").toString().replace("file:","");

//		List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
		CSVReader reader = new CSVReader(new FileReader(csvPath));
		String [] nextLine;
		int correct = 0;
		int sample = 0;
		while (sample < 100 && (nextLine = reader.readNext()) != null) {
			String feedback = nextLine[0];
			String label = nextLine[1];
			UserFeedback userFeedback = new UserFeedback(feedback);
			ClassificationResult classificationResult = classifier.classify(modelPath, userFeedback);
			System.out.println("predicted: " + classificationResult.getLabel() + "  actual: " + label + " (" + classificationResult.getAccuracy() + ")");
			if (label.equalsIgnoreCase(classificationResult.getLabel())){
				correct++;
			}
			sample++;
		}
		reader.close();
		System.out.println(correct + " out of " + sample);
		
//		for (UserFeedback feedback : userFeedbacks){
//			ClassificationResult result = classifier.classify(modelPath, feedback);
//			System.out.println(result.getLabel() + " " + result.getAccuracy() + "%");
//		}
	}
}
