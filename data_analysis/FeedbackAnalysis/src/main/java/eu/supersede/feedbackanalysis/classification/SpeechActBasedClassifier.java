package eu.supersede.feedbackanalysis.classification;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.preprocessing.utils.AttributeExtractor;
import eu.supersede.feedbackanalysis.preprocessing.utils.SMOTE;
import eu.supersede.feedbackanalysis.preprocessing.utils.Utils;

public class SpeechActBasedClassifier implements FeedbackClassifier {

	@Override
	public ClassificationResult classify(String modelPath, UserFeedback userFeedback) throws Exception {
		
		ClassificationResult result = new ClassificationResult ();
		
		// first apply feature extraction
		AttributeExtractor extractor = new AttributeExtractor();
		String arff = extractor.getARFF(userFeedback);
		
		InputStream arffInputStream = new ByteArrayInputStream(arff.getBytes());
		DataSource source = new DataSource(arffInputStream);
		Instances instances = source.getDataSet();
//		System.out.println(instances.toString());
		
		// then load the trained model
		RandomForest cls = (RandomForest) weka.core.SerializationHelper.read(modelPath); //+"rf.model");
		
		instances.setClassIndex(instances.numAttributes() - 1);
		// finally apply the model on the new data
//		for (Instance instance : instances){
		Instance instance = instances.get(0); // only one instance
		double classIndex = cls.classifyInstance(instance);
		double[] distributionForInstance = cls.distributionForInstance(instance);
		instance.setClassValue(classIndex);
		Attribute classAttribute = instance.classAttribute();
//			System.out.println(instance.toString());
		String classLabel = classAttribute.value((int)classIndex);
//			System.out.println(classLabel);
		
		result.setLabel(classLabel);
		result.setAccuracy(distributionForInstance[(int)classIndex]);
			
//		}
		return result;
	}


	/**
	 * pathIn: ARFF file containing trining data
	 * pathOut: path to where the model should be saved.
	 */
	@Override
	public boolean train(String pathIn, String pathOut) { //throws Exception {
		boolean result = true;
		DataSource dataSource = null;
		Instances instances = null;
		
		// train a model
		RandomForest rf = new RandomForest();
		String[] options;
		try {
			// load training set
			dataSource = new DataSource(pathIn);
			instances = dataSource.getDataSet();
			System.out.println(instances.toString());
			
			// train classifier
			options = weka.core.Utils.splitOptions("-P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1");
			rf.setOptions(options);
			instances.setClassIndex(instances.numAttributes() - 1);
			// apply SMOTE for oversampling of minority classes
			instances = applyOversampling(instances);
			rf.buildClassifier(instances);
			
			// serialize the model to file
			ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream(pathOut + File.separatorChar + "rf.model")); // "resources/models/rf.model"
			oos.writeObject(rf);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}

		return result;
	}

	public Instances applyOversampling (Instances instances) throws Exception {
		AttributeStats attributeStats = instances.attributeStats(instances.classIndex());
		int[] counts = attributeStats.nominalCounts;
		Arrays.sort(counts);
		double max = Utils.max(counts);
		// iterate over the labels, calculate the percentage, and resample
		// heruistic: if attr1 < 0.8*max ==> resample by ((max-attr1)/attr1)*100
		double threshold = 0.8; //80%
		for (int i = 0; i < counts.length - 1; i++) {
			double w = counts[i];
			if (threshold * max > w) {
				SMOTE smote = new SMOTE();
				smote.setInputFormat(instances);
				double percentage = (max - w)/w * 100;
				smote.setPercentage(percentage);
				instances = Filter.useFilter(instances, smote);
			}
		}
		return instances;
	}
	
	public static void main(String[] args) throws Exception {
		//  for testing purposes only 
		
		UserFeedback f1 = new UserFeedback("I encountered problems changing my meters (Gas, water, electricity). After entering a new meter (meter number), I am not able anymore to access already entered values. The earlier comparisons are therefore lost.");
		UserFeedback f2 = new UserFeedback("Hello, something is wrong with my water section.");
		UserFeedback f3 = new UserFeedback("Last month, our data were corrected in the water. After entering the meter readings for month end completely wrong numbers are displayed again. :( I ask for examination.");
		UserFeedback f4 = new UserFeedback("Hello, I had created two electricity meters; for the house and the apartment. When entering the last year's consumption figures, I have found out that the meter data are added together. How can I prevent this?");
		
		List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
		userFeedbacks.add(f1);
		userFeedbacks.add(f2);
		userFeedbacks.add(f3);
		userFeedbacks.add(f4);
		
		FeedbackClassifier classifier = new SpeechActBasedClassifier();
//		String csvPath = Thread.currentThread().getContextClassLoader().getResource("trainingsets/SENERCON_translated_300_feedback.csv").toString().replace("file:","");
//		String arffPath = "resources/trainingsets/SENERCON_translated_300_feedback.csv.arff";
//		String arffPath = Thread.currentThread().getContextClassLoader().getResource("trainingsets/comments_order_0_confirmed_textonly.csv.arff").toString().replace("file:","");
//		classifier.train(arffPath, true);
		
		String modelPath = Thread.currentThread().getContextClassLoader().getResource("models/rf.model").toString().replace("file:","");
		for (UserFeedback feedback : userFeedbacks){
			ClassificationResult result = classifier.classify(modelPath, feedback);
			System.out.println(result.getLabel() + " " + result.getAccuracy() + "%");
		}
		
		
	}
}
