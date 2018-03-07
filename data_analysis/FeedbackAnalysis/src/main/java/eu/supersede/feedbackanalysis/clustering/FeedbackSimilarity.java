/**
 * 
 */
package eu.supersede.feedbackanalysis.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.jena.ontology.OntClass;

import eu.supersede.feedbackanalysis.ds.SimilarityResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class FeedbackSimilarity {
	
	public enum SimilarityMeasure {
		HAMMING, JACCARD, KNN
	}
	
	FeedbackAnnotator feedbackAnnotator;
	
	SimilarityMeasure similarityMeasure;
	
	/**
	 * 
	 */
	public FeedbackSimilarity(String ontologyFile, SimilarityMeasure sm) {
		similarityMeasure = sm;
		feedbackAnnotator = new FeedbackAnnotator(ontologyFile);
	}
	
	public FeedbackSimilarity(String ontologyFile, String language) {
		similarityMeasure = SimilarityMeasure.JACCARD;
		boolean classLabelsOnly = false;
		boolean directLinksOnly = true;
		feedbackAnnotator = new FeedbackAnnotator(ontologyFile, language, classLabelsOnly, directLinksOnly);
	}
	
	public FeedbackSimilarity(String ontologyFile) {
		similarityMeasure = SimilarityMeasure.JACCARD;
		boolean classLabelsOnly = false;
		boolean directLinksOnly = true;
		String language = "en";
		feedbackAnnotator = new FeedbackAnnotator(ontologyFile, language, classLabelsOnly, directLinksOnly);
	}
	
	Map<UserFeedback, Double> getSimilarFeedback (List<UserFeedback> allFeedbacks, UserFeedback feedback, int N){
		
		// Get feedback concepts
		Set<OntClass> feedbackConcepts = feedbackAnnotator.annotateFeedback2(new FeedbackMessage(feedback.getFeedbackText()));
		
		// Get concepts from each of the strings in the set
		Map<UserFeedback, Set<OntClass>> setConcepts = new LinkedHashMap<>();
		for (UserFeedback fb : allFeedbacks) {
			FeedbackMessage fm = new FeedbackMessage(fb.getFeedbackText());
			Set<OntClass> itemConcepts = feedbackAnnotator.annotateFeedback2(fm);
			setConcepts.put(fb, itemConcepts);
		}
		
		Map<UserFeedback, Double> similarFeedbacks = computeSimilarity(feedbackConcepts, setConcepts);
		
		// return the top N only (values are sorted by descending order of similarity scores)
		Map<UserFeedback, Double> result = new LinkedHashMap<UserFeedback, Double>();
		Iterator<Entry<UserFeedback, Double>> iterator = similarFeedbacks.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<UserFeedback, Double> next = iterator.next();
			result.put(next.getKey(), next.getValue());
			if (result.size() == N) {
				break;
			}
		}
		
		return result;
	}
	

	private Map<UserFeedback, Double> computeSimilarity(Set<OntClass> queryConcepts, Map<UserFeedback, Set<OntClass>> setConcepts) {
		
		Map<UserFeedback, Double> result = new LinkedHashMap<UserFeedback, Double>();
		OntologyWrapper ontologyWrapper = feedbackAnnotator.getOntologyWrapper();
		
		// Compute the topN similar items

		if (similarityMeasure == SimilarityMeasure.HAMMING) {
			/*
			 * Compute the similarity based on Hamming distance between the FV of the feedback and each requirement.
			 * Hamming distance is basically the count of the number of times corresponding vector values differ.
			 * Since we want similarity, the method returns d / (d+1)
			 */
			for (Entry<UserFeedback, Set<OntClass>> entry : setConcepts.entrySet()) {
				double d;
				// map feedback and requirement concepts to feature vector
				int[] feedbackFV = ontologyWrapper.conceptsToFeatureVector(queryConcepts);
				int[] requirementFV = ontologyWrapper.conceptsToFeatureVector(entry.getValue());
	
				d = Utils.computeHammingSimilarity(feedbackFV, requirementFV);
				result.put(entry.getKey(), d);
			}
		} else if (similarityMeasure == SimilarityMeasure.JACCARD) {
			/*
			 *  compute Jaccard similarity index: size of intersection divided by size of union
			 */
			for (Entry<UserFeedback, Set<OntClass>> entry : setConcepts.entrySet()) {
				double d;
				d = Utils.computeJaccardSimilarity(queryConcepts, entry.getValue());
				result.put(entry.getKey(), d);
			}
			
		} else {
			throw new RuntimeException("Unsupported similarity measure: " + similarityMeasure);
		}
			
		return result;
	}
}
