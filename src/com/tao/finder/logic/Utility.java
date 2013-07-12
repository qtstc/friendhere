package com.tao.finder.logic;

import com.tao.finder.R;

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
	
	
	/**
	 * Get the tag used to identify different Fragments. This is necessary
	 * because identifying fragment by Id will give NullPointerException.
	 * 
	 * @param pos
	 *            the position of the tab.
	 * @return the Tag of the fragment.
	 */
	public static String getFragmentTag(int id, int pos) {
		return "android:switcher:" + id + ":" + pos;
	}
}
