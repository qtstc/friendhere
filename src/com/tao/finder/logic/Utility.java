package com.tao.finder.logic;

/**
 * Common methods.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class Utility {
	public static String truncString(String s, int length) {
		if (s.length() < length)
			return s;
		s = s.substring(0, length - 3) + "...";
		return s;
	}
}
