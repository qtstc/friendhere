package com.tao.finder.logic;

public class Utility {
	public static String truncString(String s, int length)
	{
		if(s.length()<length)
			return s;
		s = s.substring(0,length-3)+"...";
		return s;
	}
}
