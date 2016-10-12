package eu.supersede.feedbackanalysis.preprocessing.utils;

import java.util.List;

import eu.supersede.feedbackanalysis.ds.UserFeedback;

public class SentiArffManager {

	public static String getArff(List<UserFeedback> userFeedbacks){
		
		// sentiment arff only needs the text of each user feedback
		String header = "@relation feedabck_sentiment \n\n";
		header += "@attribute sentiment {-1,0,1} \n";
		header += "@attribute text String \n";
		header += "@data \n";
		
		String data = "";
		
		for (UserFeedback uf: userFeedbacks)
		{
			data += "?, \"" + uf.getFeedbackText() + "\" \n" ;
		}
		
		return header + data;
		
	}
}
