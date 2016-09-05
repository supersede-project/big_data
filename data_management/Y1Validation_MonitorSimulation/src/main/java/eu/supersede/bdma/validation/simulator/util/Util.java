package eu.supersede.bdma.validation.simulator.util;

public class Util {
	
	public static String cutString(String S) {
		return S.length() < 180 ? S.substring(0,S.length()-1) : S.substring(0,180);
	}
}
