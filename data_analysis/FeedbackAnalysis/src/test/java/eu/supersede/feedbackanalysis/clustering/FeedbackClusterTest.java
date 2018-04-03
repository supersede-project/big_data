/**
 * 
 */
package eu.supersede.feedbackanalysis.clustering;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import eu.supersede.feedbackanalysis.clustering.FeedbackClusterer;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

/**
 * @author fitsum
 *
 */
public class FeedbackClusterTest {

	String dataset = "trainingsets/SDO_ontology.ttl.class_prop_direct.fv.csv";
	boolean arff = false;
	boolean file = true;
	double percent = 0.7;
	int k = 5;
	
	List<UserFeedback> feedbackMessages = null;
	
	@Before
	public void init() {
		String feedback1 = "meter readings appearing incorrect. can you please have a look? thank you.";
		String feedback2 = "Hello I use your portal my consumption measurements thus I have good overview Suggestion data entry Would possible change program so I can enter electricity gas water photovoltaic total self consumption simultaneously one page without returning overview page each time In anticipation short feedback ";
		String feedback3 = "My electricity consumption should 871 higher than similar households What's wrong ";
		String feedback4 = "Hello my engergy savings account heating diagram do shows oil level input from 01.12.2015 What should I do Best wishes ";
		String feedback5 = "I wonder about my consumption my current provider classifies me to monthly 130 euros since january how the consumptions/costs in your app arise?";
		
//		String csvPath = "/data/SUPERSEDE/WP2/SENERCON-data/SENERCON_translated_300_feedback_3_scale.csv";
//		feedbackMessages = feedbackAnnotator.getFeedbackMessages(csvPath);
		
		feedbackMessages = new ArrayList<UserFeedback>();
		feedbackMessages.add(new UserFeedback(feedback1));
		feedbackMessages.add(new UserFeedback(feedback2));
		feedbackMessages.add(new UserFeedback(feedback3));
		feedbackMessages.add(new UserFeedback(feedback4));
		feedbackMessages.add(new UserFeedback(feedback5));
	}
	
	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.clustering.FeedbackClusterer#FeedbackCluster(java.lang.String, boolean, boolean)}.
	 */
	@Test
	public void testFeedbackClusterStringBooleanBoolean() {
		FeedbackClusterer fCluster = new FeedbackClusterer(dataset , arff, file, percent);
		assertNotNull(fCluster);
	}

	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.clustering.FeedbackClusterer#computeClusters(int)}.
	 */
	@Test
	public void testComputeClusters() {
		FeedbackClusterer fCluster = new FeedbackClusterer(dataset, arff, file, percent);
		
		Instances nearestNeighbors = fCluster.computeNearestNeighbors(k);
		assertTrue(nearestNeighbors.size() >= k);
	}

	@Test
	public void testComputeClustersFromFeedback() throws Exception {
		String ontologyFile = "SDO_ontology.ttl";
		String wordnetDbPath = null;
		String language = "en";
		
		String csvPath = "trainingsets/SENERCON_userfeedback_clustering.csv";
		List<UserFeedback> userFeedbacks = FeedbackAnnotator.getUserFeedbackForClustering(csvPath);
		
		FeedbackClusterer feedbackClusterer = new FeedbackClusterer(ontologyFile, wordnetDbPath, language);
		int numClusters = 40;
//		Clusterer clusterer = feedbackClusterer.computeClusters(userFeedbacks, numClusters);
		
		Map<Integer, List<UserFeedback>> feedbackClusters = feedbackClusterer.clusterUserFeedback(userFeedbacks, numClusters);
		for (Entry<Integer, List<UserFeedback>> entry : feedbackClusters.entrySet()) {
			System.out.println("Cluster: " + entry.getKey());
			for (UserFeedback fb : entry.getValue()) {
				System.out.println(fb.getFeedbackText());
			}
		}
	}
	
	@Test
	public void testComputeClustersFromLabeledFeedback() throws Exception {
		String ontologyFile = "SDO_ontology.ttl";
		String wordnetDbPath = null;
		String language = "en";
		
		String csvPath = "trainingsets/SENERCON_userfeedback_clustering.csv"; //SENERCON_userfeedback_clustering_old.csv";
		List<FeedbackMessage> feedbackMessages = FeedbackAnnotator.getFeedbackMessagesForClustering(csvPath);
		
		FeedbackClusterer feedbackClusterer = new FeedbackClusterer(ontologyFile, wordnetDbPath, language);
		int numClusters = 40;
		
		Map<Integer, List<FeedbackMessage>> feedbackClusters = feedbackClusterer.clusterFeedbackMessages(feedbackMessages, numClusters);
		for (Entry<Integer, List<FeedbackMessage>> entry : feedbackClusters.entrySet()) {
			System.out.print("Cluster: " + entry.getKey() + " ==> ");
			for (FeedbackMessage fb : entry.getValue()) {
				System.out.print(fb.getClusterId() + ", ");
			}
			System.out.println();
		}
		
	}
	
}
