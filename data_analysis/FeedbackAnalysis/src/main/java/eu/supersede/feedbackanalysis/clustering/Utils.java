package eu.supersede.feedbackanalysis.clustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;


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
	
	/**
	 * computes the distance between the two feature vectors
	 * 
	 * @param feedbackFV
	 * @param fv
	 * @return
	 */
	public static double computeHammingSimilarity(int[] fv1, int[] fv2) {
		return 1d - (double) Utils.hammingDistance(fv1, fv2) / (double) fv1.length;
	}

	/**
	 * Computes the Hamming distance between the two integer arrays.
	 */
	public static int hammingDistance(int[] x, int[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException(
					String.format("Arrays have different length: x[%d], y[%d]", x.length, y.length));

		int dist = 0;
		for (int i = 0; i < x.length; i++) {
			if (x[i] != y[i])
				dist++;
		}

		return dist;
	}

	/**
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static <T> double computeJaccardSimilarity(Set<T> set1, Set<T> set2) {
		if (set1.isEmpty() || set2.isEmpty()) {
			return 0;
		}
		Set<T> intersection = new HashSet<T>(set1);
		intersection.retainAll(set2);
		Set<T> union = new HashSet<T>();
		union.addAll(set2);
		union.addAll(set1);
		return (double) intersection.size() / (double) union.size();
	}

}
