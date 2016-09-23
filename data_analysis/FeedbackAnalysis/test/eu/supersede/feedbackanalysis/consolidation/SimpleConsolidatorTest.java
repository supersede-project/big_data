/**
 * 
 */
package eu.supersede.feedbackanalysis.consolidation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.supersede.feedbackanalysis.ds.AnalysisReport;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class SimpleConsolidatorTest {

	protected List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
	
	@Before
	public void setUp(){
		UserFeedback f1 = new UserFeedback("However since _ XPropertySet ] 21: LOG Execute: getPropertySetInfo 21: Method getPropertySetInfo finished with state OK");
		UserFeedback f2 = new UserFeedback("I would like to have the window minimizing automatically when it loses focus.");
		UserFeedback f3 = new UserFeedback("Automatic window minimize does not work. Please fix it, I really hate it.");
		UserFeedback f4 = new UserFeedback("I would like a new search feature that works on voice commands.");
		
		
		userFeedbacks.add(f1);
		userFeedbacks.add(f2);
		userFeedbacks.add(f3);
		userFeedbacks.add(f4);
	}
	
	
	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.consolidation.SimpleConsolidator#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		AnalysisConsolidator analysisConsolidator = SimpleConsolidator.getInstance();
		assertNotNull(analysisConsolidator);
	}

	/**
	 * Test method for {@link eu.supersede.feedbackanalysis.consolidation.SimpleConsolidator#getSummary(java.util.List)}.
	 */
	@Test
	public void testGetSummary() {
		AnalysisConsolidator analysisConsolidator = SimpleConsolidator.getInstance();
		AnalysisReport summary = analysisConsolidator.getSummary(userFeedbacks);
		assertTrue(summary.getClassificationResult().size() == userFeedbacks.size());
		assertTrue(summary.getSentimentResult().size() == userFeedbacks.size());
		System.out.println(summary.getSummary());
	}

	
	@Test
	public void testGetJSON(){
		AnalysisConsolidator analysisConsolidator = SimpleConsolidator.getInstance();
		AnalysisReport summary = analysisConsolidator.getSummary(userFeedbacks);
		assertTrue(summary.getClassificationResult().size() == userFeedbacks.size());
		assertTrue(summary.getSentimentResult().size() == userFeedbacks.size());
		System.out.println(summary.getJSON());
	}
}
