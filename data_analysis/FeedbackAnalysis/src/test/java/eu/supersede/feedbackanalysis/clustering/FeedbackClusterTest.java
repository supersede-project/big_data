/**
 * 
 */
package eu.supersede.feedbackanalysis.clustering;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import eu.supersede.feedbackanalysis.clustering.FeedbackClusterer;
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
	
	@Before
	public void init() {
		
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
		fCluster.computeClusters(k);
		
		Instances nearestNeighbors = fCluster.computeNearestNeighbors(k);
		assertTrue(nearestNeighbors.size() >= k);
	}

}
