package eu.supersede.feedbackanalysis.classification;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.preprocessing.utils.AttributeExtractor;

public class SpeechActBasedClassifier implements FeedbackClassifier {

	@Override
	public List<ClassificationResult> classify(List<UserFeedback> userFeedback) throws Exception {
		
		List<ClassificationResult> results = new ArrayList<ClassificationResult> ();
		
		// first apply feature extraction
		AttributeExtractor extractor = new AttributeExtractor();
		String arff = extractor.getARFF(userFeedback);
		
		InputStream arffInputStream = new ByteArrayInputStream(arff.getBytes());
		DataSource source = new DataSource(arffInputStream);
		Instances instances = source.getDataSet();
//		System.out.println(instances.toString());
		
		// then load the trained model
		RandomForest cls = (RandomForest) weka.core.SerializationHelper.read("resources/models/rf.model"); //oo_comment_0_RandomForest.model");
		
		instances.setClassIndex(instances.numAttributes() - 1);
		// finally apply the model on the new data
		for (Instance instance : instances){
			double classIndex = cls.classifyInstance(instance);
			double[] distributionForInstance = cls.distributionForInstance(instance);
			instance.setClassValue(classIndex);
			Attribute classAttribute = instance.classAttribute();
//			System.out.println(instance.toString());
			String classLabel = classAttribute.value((int)classIndex);
//			System.out.println(classLabel);
			
			ClassificationResult result = new ClassificationResult();
			result.setLabel(classLabel);
			result.setAccuracy(distributionForInstance[(int)classIndex]);
			
			results.add(result);
		}
		return results;
	}


	@Override
	public Classifier train(String path, boolean arff) throws Exception {
		
		DataSource dataSource;
		if (!arff){
			// get the arff from the 'path'
			AttributeExtractor extractor = new AttributeExtractor();
			String arffString = extractor.getARFF(path);
			InputStream arffInputStream = new ByteArrayInputStream(arffString.getBytes());
			dataSource = new DataSource(arffInputStream);
			
			// save the arff for future use?
			
		}else{
			dataSource = new DataSource(path);
		}
		Instances instances = dataSource.getDataSet();
		System.out.println(instances.toString());
		
		// train a model
		RandomForest rf = new RandomForest();
		String[] options = weka.core.Utils.splitOptions("-P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1");
		rf.setOptions(options);
		instances.setClassIndex(instances.numAttributes() - 1);
		rf.buildClassifier(instances);
		
		// serialize the model to file
		ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream("resources/models/rf.model"));
		oos.writeObject(rf);
		oos.flush();
		oos.close();
		
////		 return the trained model
//		WekaModel model = new WekaModel();
//		model.setModel(rf);

		return rf;
	}

	public static void main(String[] args) throws Exception {
		//  for testing purposes only 
		
		UserFeedback f1 = new UserFeedback("However since _ XPropertySet ] 21: LOG Execute: getPropertySetInfo 21: Method getPropertySetInfo finished with state OK");
		UserFeedback f2 = new UserFeedback("I would like to have the window minimizing automatically when it loses focus.");
		UserFeedback f3 = new UserFeedback("Automatic window minimize does not work. Please fix it.");
		UserFeedback f4 = new UserFeedback("I would like a new search feature that works on voice commands.");
		
		List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
		userFeedbacks.add(f1);
		userFeedbacks.add(f2);
		userFeedbacks.add(f3);
		userFeedbacks.add(f4);
		
		FeedbackClassifier classifier = new SpeechActBasedClassifier();
		String csvPath = "resources/trainingsets/SENERCON_translated_300_feedback.csv";
//		String arffPath = "resources/trainingsets/SENERCON_translated_300_feedback.csv.arff";
		String arffPath = "resources/trainingsets/comments_order_0_confirmed_textonly.csv.arff";
//		classifier.train(arffPath, true);
		List<ClassificationResult> classificationResult = classifier.classify(userFeedbacks);
		
		for (ClassificationResult result : classificationResult){
			System.out.println(result.getLabel() + " " + result.getAccuracy() + "%");
		}
		
		
	}
}
