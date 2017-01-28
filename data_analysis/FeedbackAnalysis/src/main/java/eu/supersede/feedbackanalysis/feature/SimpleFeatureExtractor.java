/**
 * 
 */
package eu.supersede.feedbackanalysis.feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import eu.supersede.feedbackanalysis.ds.FeatureExtractionResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;


public class SimpleFeatureExtractor implements FeatureExtractor {

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.feature.FeatureExtractor#extractFeatures(java.util.List)
	 */
	
	public FeatureExtractionResult extractFeatures(List<UserFeedback> userFeedbacks) {
		
		FeatureExtractionResult result = new FeatureExtractionResult();
		List<String> features = new ArrayList<String>();
	
		// Convert list of user feedback into file input ...
		try{
			File logFile = new File(System.getProperty("user.dir") + "/resources/scripts/feature_input");

			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			
			for (UserFeedback feedback: userFeedbacks){
				writer.write(feedback.getFeedbackText());
			}
			
			writer.close();
			
		}catch(Exception e)
		{
			System.out.println("Problem writing feedback to file! ");
			System.out.println(e.getStackTrace());
		}
		
		
		// Calling Python script for feature extraction
		String [] command = new String[4];
		command[0] = "python";
		command[1] = System.getProperty("user.dir") + "/resources/scripts/nGrams.py"; //path to the script
		command[2] = System.getProperty("user.dir") + "/resources/scripts/feature_input"; //input feedback //a nice file for demoing is in datasets/test
		command[3] = System.getProperty("user.dir") + "/resources/scripts/extracted_features.csv"; //output file
		
		System.out.println("Output file " + command[3] + " input file " + command[2]);
		System.out.println(command[0] + " " + command [1] + " " + command[2] + " " + command [3]);

		try{
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			process.destroy();

			System.out.println("Python program finished extracting features!");

			}catch(Exception e){
				System.out.println("Something went wrong in feature extraction ");
				System.out.println(e.getStackTrace());
			}
		
		// TODO: empty output, since at the moment script stores features into a file
		try{
			BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/resources/scripts/extracted_features.csv"));
			String line;
			
			while ((line = br.readLine()) !=null) {
				String[] row = line.split(",");
				features.add(row[0]);	
			}
		
			result.setFeatures(features);
		}
		catch(Exception ex){
			
			System.out.println("Problem reading from file and converting to expected output");
			System.out.println(ex.getStackTrace());
		}
		
		return result;
	}
	
	
	// TODO: for testing purposes, this should be removed once results are consolidated
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
		new SimpleFeatureExtractor().extractFeatures(userFeedbacks);
		
	}


	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.feature.FeatureExtractor#batch(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean batch(String pathIn, String pathOut) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.feature.FeatureExtractor#single(java.lang.String, eu.supersede.feedbackanalysis.ds.UserFeedback)
	 */
	@Override
	public FeatureExtractionResult single(String featurePath,
			UserFeedback userFeedback) {
		// TODO Auto-generated method stub
		FeatureExtractionResult result = new FeatureExtractionResult();
		List<String> features = new ArrayList<String>();
		result.setFeatures(features);
		return result;
	}

}
