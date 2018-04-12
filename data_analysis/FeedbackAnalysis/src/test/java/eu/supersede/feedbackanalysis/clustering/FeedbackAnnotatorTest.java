package eu.supersede.feedbackanalysis.clustering;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.opencsv.CSVWriter;

import eu.supersede.feedbackanalysis.clustering.FeedbackAnnotator;
import eu.supersede.feedbackanalysis.clustering.FeedbackMessage;
import eu.supersede.feedbackanalysis.clustering.FeedbackAnnotator.AnalysisType;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.preprocessing.utils.FileManager;

/**
 * 
 * @author fitsum
 *
 */
public class FeedbackAnnotatorTest {

	List<FeedbackMessage> feedbackMessages = null;
	String ontologyFile = "ATOS_ontology.ttl";
	boolean classLabelsOnly = false;
	boolean direct = true; 
	String language = "en";
	String wordnetDbPath = ""; // let it be located from classpath
	FeedbackAnnotator feedbackAnnotator = new FeedbackAnnotator(ontologyFile, wordnetDbPath, language, classLabelsOnly, direct);
	
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
	
	@Test
	public void testAtoRule () throws IOException {
		Set<String> q1 = new HashSet<>();
		q1.addAll(Arrays.asList("network, Connection, Video".split(",")));
		
		Set<String> q2 = new HashSet<>();
		q2.addAll(Arrays.asList("network, Connection, load, Time".split(",")));
		
		Set<String> q3 = new HashSet<>();
		q3.addAll(Arrays.asList("Statistics"));
		
		Set<String> q4 = new HashSet<>();
		q4.addAll(Arrays.asList("Flash, Player, compatible".split(",")));
		
		Set<String> q5 = new HashSet<>();
		q5.addAll(Arrays.asList("Flash, Player, version".split(",")));
		
		String csvFile = "src/test/resources/trainingsets/ATOS_userfeedback_sentiment.csv";
		String reportFile = "src/test/resources/trainingsets/ATOS_rules_sentiment_jaccard_classonly.csv";
		Reader reader = new FileReader(new File(csvFile));
		CSVWriter csvWriter = new CSVWriter(new FileWriter(reportFile));
		String[] header = "feedback,q1,q2,q3,q4,jq5,jq1,jq2,jq3,jq4,jq5,sentiment".split(",");
		csvWriter.writeNext(header);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader("sentiment","Feedback").parse(reader);
		for (CSVRecord record : records) {
			if (!"sentiment".equalsIgnoreCase(record.get("sentiment"))){
				UserFeedback uf = new UserFeedback(record.get("Feedback"));
				String sentiment = record.get("sentiment");
				
				//intersection count
				double d1 = feedbackAnnotator.ontologicalDistance(uf, q1);
				double d2 = feedbackAnnotator.ontologicalDistance(uf, q2);
				double d3 = feedbackAnnotator.ontologicalDistance(uf, q3);
				double d4 = feedbackAnnotator.ontologicalDistance(uf, q4);
				double d5 = feedbackAnnotator.ontologicalDistance(uf, q5);
				
				//jaccard score
				double jd1 = feedbackAnnotator.ontologicalDistanceJaccard(uf, q1);
				double jd2 = feedbackAnnotator.ontologicalDistanceJaccard(uf, q2);
				double jd3 = feedbackAnnotator.ontologicalDistanceJaccard(uf, q3);
				double jd4 = feedbackAnnotator.ontologicalDistanceJaccard(uf, q4);
				double jd5 = feedbackAnnotator.ontologicalDistanceJaccard(uf, q5);
				String[] line = { uf.getFeedbackText() , ""+d1 , ""+d2 , ""+d3 , ""+d4 , ""+d5, ""+jd1 , ""+jd2 , ""+jd3 , ""+jd4 , ""+jd5 , sentiment };
				csvWriter.writeNext(line);
			}
		}
		csvWriter.close();
	}
	
}
