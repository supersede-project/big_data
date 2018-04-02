/**
 * 
 */
package eu.supersede.feedbackanalysis.preprocessing.utils;

/**
 * @author fitsum
 *
 */
public class Utils {
	public static int max (int[] array){
		if (array == null || array.length == 0) {
			return -1;
		}else {
			int m = array[0];
			for (int e : array) {
				if (e > m) {
					m = e;
				}
			}
			return m;
		}
	}
}
