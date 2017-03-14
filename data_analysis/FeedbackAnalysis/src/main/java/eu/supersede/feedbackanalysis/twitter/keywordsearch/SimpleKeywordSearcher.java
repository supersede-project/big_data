package eu.supersede.feedbackanalysis.twitter.keywordsearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.KeywordSearchResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.twitter.keywordsearch.KeywordSearcher;
//import eu.supersede.feedbackanalysis.ds.UserFeedback;

public class SimpleKeywordSearcher implements KeywordSearcher{

	public KeywordSearchResult search(String pathToKW, UserFeedback userFeedback)
	{
		// was static in the other one, should probably be in the class
		List<String> lines = new ArrayList<String>();
		KeywordSearchResult result = new KeywordSearchResult();
		List<String> found_keywords = new ArrayList<String>();

		try{
			FileReader fileReader = new FileReader(pathToKW);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
       
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
		}
		catch(IOException ex){
			System.out.println("Keyword list was not found" + ex);
		}
        
		String [] keywords = lines.toArray(new String[lines.size()]);

		for (String keyword : keywords) {
			// TODO: in case we want subwords to be found use this!
			//if (userFeedback.getFeedbackText().toLowerCase().contains(keyword.toLowerCase())) {
			if (isContain(userFeedback.getFeedbackText().toLowerCase(), keyword.toLowerCase())){
				found_keywords.add(keyword.toLowerCase());
			}
		}
		result.setFoundKeywords(found_keywords);
		return result;
	}

	private static boolean isContain(String source, String subItem){
		String pattern = "\\b"+subItem+"\\b";
		Pattern p=Pattern.compile(pattern);
		Matcher m=p.matcher(source);
		return m.find();
	}
	
	//For testing purposes only
	public static void main(String[] args){

		String pathToKW = Thread.currentThread().getContextClassLoader().getResource("search_keywords.txt").toString().replace("file:","");
		SimpleKeywordSearcher k = new SimpleKeywordSearcher();


		UserFeedback f1 = new UserFeedback("Firefox Chrome Safari");
		UserFeedback f2 = new UserFeedback("I would like to have the window minimizing automatically when it loses focus.");
		UserFeedback f3 = new UserFeedback("Automatic window minimize does not work. Please fix it.");
		UserFeedback f4 = new UserFeedback("I would like a new search feature that works on voice commands.");

		List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
		userFeedbacks.add(f1);
		userFeedbacks.add(f2);
		userFeedbacks.add(f3);
		userFeedbacks.add(f4);


		for (UserFeedback feedback : userFeedbacks){
			KeywordSearchResult result = k.search(pathToKW, feedback);

			if (result.getFoundKeywords().size() > 0) {
				System.out.println("************");
				System.out.println("Found keywords for user feedback with following text!");
				System.out.println(feedback.getFeedbackText());

				for (String keyword : result.getFoundKeywords())
					System.out.println(keyword);
			}
		}
	}

}
