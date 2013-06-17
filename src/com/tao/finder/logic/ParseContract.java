package com.tao.finder.logic;

import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ParseContract {
	public static final String APPLICATION_ID = "t2zKB0ekNi8wWLoAmfc2usjmV03kAAygt4tzI0Dx";
	public static final String CLIENT_KEY = "ks4ddRM4qaCOGl4ZPLN0xxFD3AiaZ6Vj2elbFwmP";
	
	public interface Event
	{
		public static final String TABLE_NAME = "events";
		public static final String NAME = "name";
		public static final String LOCATION = "location";
		public static final String STARTING_TIME = "starting_time";
		public static final String ENDING_TIME = "ending_time";
		public static final String DESCRIPTION = "description";	
	}
	
	public static boolean isLoggedIn()
	{
		if(ParseUser.getCurrentUser()==null)
			return false;
		return true;
	}
}
