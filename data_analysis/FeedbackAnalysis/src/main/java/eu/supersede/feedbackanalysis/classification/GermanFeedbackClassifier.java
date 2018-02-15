/**
 * 
 */
package eu.supersede.feedbackanalysis.classification;

import java.io.File;

import eu.fbk.ict.fm.nlp.synaptic.classification.tc.TypeClassify;
import eu.fbk.ict.fm.nlp.synaptic.classification.tc.TypeLearn;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class GermanFeedbackClassifier implements FeedbackClassifier {

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.classification.FeedbackClassifier#train(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean train(String pathIn, String pathOut) throws Exception {
		TypeLearn typeLearn = new TypeLearn();
		typeLearn.run(pathIn, pathOut);
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.supersede.feedbackanalysis.classification.FeedbackClassifier#classify(java.lang.String, eu.supersede.feedbackanalysis.ds.UserFeedback)
	 */
	@Override
	public ClassificationResult classify(String modelPath, UserFeedback userFeedback) throws Exception {
		ClassificationResult classificationResult = new ClassificationResult();
		TypeClassify typeClassify = new TypeClassify(modelPath);
//		String[] feedback = {userFeedback.getFeedbackText()};
		String[] outcome = typeClassify.run(userFeedback.getFeedbackText());
		classificationResult.setLabel(outcome[0]);
		classificationResult.setAccuracy(Double.parseDouble(outcome[1]));
		return classificationResult;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String pathname = "src/test/resources/trainingsets/german_train.tsv";
		File trainFile = new File(pathname);
		File model = new File("src/test/resources/trainingsets/german_train.tsv.ta.model");
		if (model.exists())
			model.delete();
		GermanFeedbackClassifier germanClassifier = new GermanFeedbackClassifier();
		try {
			germanClassifier.train(trainFile.getAbsolutePath(), model.getAbsolutePath());
			
			UserFeedback userFeedback = new UserFeedback("this is german text");
			ClassificationResult classificationResult = germanClassifier.classify(model.getAbsolutePath(), userFeedback);
			System.out.println(classificationResult.getLabel() + " " + classificationResult.getAccuracy());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
