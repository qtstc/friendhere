package com.tao.finder.logic;

import java.util.Date;
import java.util.List;

import android.location.Location;
import android.widget.TableLayout;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ParseContract {
	public static final String APPLICATION_ID = "t2zKB0ekNi8wWLoAmfc2usjmV03kAAygt4tzI0Dx";
	public static final String CLIENT_KEY = "ks4ddRM4qaCOGl4ZPLN0xxFD3AiaZ6Vj2elbFwmP";
	
	public static class User
	{
		public static final String NAME = "name";
		public static final String PHONE = "phone";
		public static final String LOCATION = "location";
		
		public static boolean isLoggedIn()
		{
			if(ParseUser.getCurrentUser()==null)
				return false;
			return true;
		}
	}
	
	public static class Event
	{
		public static final String TABLE_NAME = "events";
		public static final String NAME = "name";
		public static final String LOCATION = "location";
		public static final String RADIUS = "radius";
		public static final String STARTING_TIME = "starting_time";
		public static final String ENDING_TIME = "ending_time";
		public static final String DESCRIPTION = "description";
		public static final String CREATOR = "creator";
		
		
		public static void createEvent(ParseUser user, String name, Date startingTime, Date endingTime, double longitude, double latitude, int radius,String description, SaveCallback callBack)
		{
			ParseObject event = new ParseObject(TABLE_NAME);
			event.put(NAME, name);
			event.put(LOCATION, new ParseGeoPoint(latitude, longitude));
			event.put(RADIUS, radius);
			event.put(STARTING_TIME,startingTime);
			event.put(ENDING_TIME,endingTime);
			event.put(CREATOR, user);
			event.put(DESCRIPTION, description);
			event.saveInBackground(callBack);
		}
		
		public static void searchEvent(String searchString, int resultNumber,int skip,FindCallback callBack)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_NAME);
			//TODO: now the search is case sensitive, change it.
			query.whereContains(NAME, searchString);
			query.setLimit(resultNumber);
			query.setSkip(skip);
			query.findInBackground(callBack);
		}
	}
	

	
	
}
