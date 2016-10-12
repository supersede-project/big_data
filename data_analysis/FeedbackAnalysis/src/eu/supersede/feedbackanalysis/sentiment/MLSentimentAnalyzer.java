package eu.supersede.feedbackanalysis.sentiment;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.preprocessing.utils.SentiArffManager;

/**
 * @author Emitzá
 *
 */


public class MLSentimentAnalyzer implements SentimentAnalyzer{
	
	public List<SentimentAnalysisResult> determineSentiment(List<UserFeedback> userFeedbacks){
		
		
		List<SentimentAnalysisResult> results = new ArrayList<SentimentAnalysisResult> ();

		
		try{
			// Converting user feedback to arff
			String arff = SentiArffManager.getArff(userFeedbacks);
			
			System.out.println(arff);
			
			InputStream arffInputStream = new ByteArrayInputStream(arff.getBytes());
			DataSource source = new DataSource(arffInputStream);
			Instances instances = source.getDataSet();
			instances.setClassIndex(0); 
			
			// Loading stored models and making predictions
			FilteredClassifier cls = (FilteredClassifier) weka.core.SerializationHelper.read("resources/models/sentiment_classifier.model"); 
			
			// Instance prediction
			for (Instance instance : instances){
				
				double classIndex = cls.classifyInstance(instance);
				instance.setClassValue(classIndex);
			
				Attribute classAttribute = instance.classAttribute();
				System.out.println(instance.toString());
				String classLabel = classAttribute.value((int)classIndex);
				
				// Representing results in expected format
				SentimentAnalysisResult res = new SentimentAnalysisResult();
				res.setOverallSentiment(Integer.parseInt(classLabel));
				
				results.add(res);
			}	
		}
		
		catch (Exception ex){
			System.out.println("Error during sentiment classification "+ ex.getMessage());
		}
		
		return results;
	}
	
	/*
	 * Parameters: path to arff file
	 */
	public static Classifier train(String arff_path) throws Exception {
		
		DataSource dataSource = new DataSource(arff_path);
		Instances instances = dataSource.getDataSet();
		System.out.println(instances.toString());
		
		// Category to predict is the first one
		instances.setClassIndex(0); 
		
		StringToWordVector filter = new StringToWordVector();
	    filter.setInputFormat(instances);
	    filter.setIDFTransform(true);
		SnowballStemmer stemmer = new SnowballStemmer();
		filter.setStemmer(stemmer);
		filter.setLowerCaseTokens(true);
	    
		
		FilteredClassifier classifier = new FilteredClassifier(); 
		classifier.setFilter(filter); 
		classifier.setClassifier(new NaiveBayesMultinomial());
		
		
		classifier.buildClassifier(instances);
		
		
		// serialize the model to file
		ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream("resources/models/sentiment_classifier.model"));
		oos.writeObject(classifier);
		oos.flush();
		oos.close();

		return classifier;
	}

	
// TODO: Remove this code after consolidation of all components has been done (or make test with it)
	public static void main(String arg[]) throws Exception{
		
		UserFeedback f1 = new UserFeedback("However since _ XPropertySet ] 21: LOG Execute: getPropertySetInfo 21: Method getPropertySetInfo finished with state OK");
		UserFeedback f2 = new UserFeedback("I would like to have the window minimizing automatically when it loses focus.");
		UserFeedback f3 = new UserFeedback("Automatic window minimize does not work. Please fix it, I really hate it.");
		UserFeedback f4 = new UserFeedback("I would like a new search feature that works on voice commands.");
		
		List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
		userFeedbacks.add(f1);
		userFeedbacks.add(f2);
		userFeedbacks.add(f3);
		userFeedbacks.add(f4);
		
		//new MLSentimentAnalyzer().train("resources/trainingsets/sentiment_reviews_3_scale.arff");
		
		new MLSentimentAnalyzer().determineSentiment(userFeedbacks);
		
	}
	
}
