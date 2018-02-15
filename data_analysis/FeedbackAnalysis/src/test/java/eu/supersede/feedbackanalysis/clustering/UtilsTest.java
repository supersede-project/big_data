package eu.supersede.feedbackanalysis.clustering;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import eu.supersede.feedbackanalysis.clustering.Utils;

/**
 * 
 * @author fitsum
 *
 */
public class UtilsTest {

	@Test
	public void testReadStopWords() {
		String path = "stopwords_en.txt";
		Set<String> stopWords = Utils.readStopWords(path);
		assertNotNull(stopWords);
		assertFalse(stopWords.isEmpty());
	}

}
