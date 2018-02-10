package eu.supersede.feedbackanalysis.clustering;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.supersede.feedbackanalysis.clustering.FeedbackAnnotator;
import eu.supersede.feedbackanalysis.clustering.FeedbackMessage;
import eu.supersede.feedbackanalysis.clustering.FeedbackAnnotator.AnalysisType;
import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * 
 * @author fitsum
 *
 */
public class FeedbackAnnotatorTest {

	List<FeedbackMessage> feedbackMessages = null;
	String ontologyFile = "SDO_ontology.ttl";
	boolean classLabelsOnly = false;
	boolean direct = true; 
	String language = "en";
	FeedbackAnnotator feedbackAnnotator = new FeedbackAnnotator(ontologyFile, language, classLabelsOnly, direct);
	
	@Before
	public void init() {
		String feedback1 = "meter readings appearing incorrect. can you please have a look? thank you.";
		String feedback2 = "Hello I use your portal my consumption measurements thus I have good overview Suggestion data entry Would possible change program so I can enter electricity gas water photovoltaic total self consumption simultaneously one page without returning overview page each time In anticipation short feedback ";
		String feedback3 = "My electricity consumption should 871 higher than similar households What's wrong ";
		String feedback4 = "Hello my engergy savings account heating diagram do shows oil level input from 01.12.2015 What should I do Best wishes ";
		String feedback5 = "I wonder about my consumption my current provider classifies me to monthly 130 euros since january how the consumptions/costs in your app arise?";
		
//		String csvPath = "/data/SUPERSEDE/WP2/SENERCON-data/SENERCON_translated_300_feedback_3_scale.csv";
//		feedbackMessages = feedbackAnnotator.getFeedbackMessages(csvPath);
		
		feedbackMessages = new ArrayList<FeedbackMessage>();
		feedbackMessages.add(new FeedbackMessage(feedback1));
		feedbackMessages.add(new FeedbackMessage(feedback2));
		feedbackMessages.add(new FeedbackMessage(feedback3));
		feedbackMessages.add(new FeedbackMessage(feedback4));
		feedbackMessages.add(new FeedbackMessage(feedback5));

//		File statFile = new File (feedbackAnnotator.getStatFilePath());
//		FileUtils.writeStringToFile(statFile , feedbackAnnotator.getStat());
	}
	
	@Test
	public void testAnnotateFeedback() {
		for (FeedbackMessage feedbackMessage : feedbackMessages) {
			Map<String, Set<Resource>> annotatedFeedback = feedbackAnnotator.annotateFeedback(feedbackMessage);
			for (Entry<String, Set<Resource>> entry : annotatedFeedback.entrySet()) {
				System.out.println("TERM: " + entry.getKey() + " Num Concepts found: " + entry.getValue().size());
				for (Resource concept: entry.getValue()) {
					System.out.println("CONCEPT: " + concept.getLocalName() + " : " + concept.getURI());
				}
			}	
		}
	}

	@Test
	public void testAnnotateFeedback2() {
		Map<FeedbackMessage, Set<OntClass>> annotatedFeedbacks = feedbackAnnotator.annotateFeedbacks2(feedbackMessages);
		assertTrue(annotatedFeedbacks.size() == feedbackMessages.size());
	}
	
	@Test
	public void testGetFeedbackMessages () {
		String csvPath = "trainingsets/SENERCON_translated_ALL.csv";
		int numMessages = 576;
		List<FeedbackMessage> feedbackMessages = feedbackAnnotator.getFeedbackMessages(csvPath);
		assertEquals(numMessages, feedbackMessages.size());
		for (FeedbackMessage message : feedbackMessages) {
			System.out.println(message.toString());
		}
	}
	
	@Test
	public void testOntologicalDistance() {
		UserFeedback uf = new UserFeedback("I really like the screen shot feature for video snapshots freezing bug reporting of heating devices");
		Set<String> keywords = new HashSet<>();
		keywords.add("video");
		keywords.add("freeze");
		keywords.add("heating");
		double ontologicalDistance = feedbackAnnotator.ontologicalDistance(uf, keywords);
		System.out.println(ontologicalDistance);
	}
	
}
