package eu.supersede.feedbackanalysis.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author fitsum
 *
 */
public class Utils {
	public static Set<String> readStopWords(String path) {
        Set<String> stopWords = new HashSet<String>();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
                while (bufferedReader.ready()) {
                        stopWords.add(bufferedReader.readLine());
                }
                bufferedReader.close();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        return stopWords;
}

}
