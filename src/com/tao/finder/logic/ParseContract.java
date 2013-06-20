package com.tao.finder.logic;

import java.util.Date;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ParseContract {
	public final static String APPLICATION_ID = "t2zKB0ekNi8wWLoAmfc2usjmV03kAAygt4tzI0Dx";
	public final static String CLIENT_KEY = "ks4ddRM4qaCOGl4ZPLN0xxFD3AiaZ6Vj2elbFwmP";
	
	public static class User
	{
		public final static String NAME = "name";
		public final static String PHONE = "phone";
		public final static String LOCATION = "location";
		
		public static boolean isLoggedIn()
		{
			if(ParseUser.getCurrentUser()==null)
				return false;
			return true;
		}
		
		public static void searchPerson(final String searchString,final String eventId, final int resultNumber,final int skip,final FindCallback<ParseUser> callback)
		{
			Event.getEventFromId(eventId, new GetCallback<ParseObject>() {
				
				@Override
				public void done(ParseObject object, ParseException e) {
					ParseQuery<ParseUser> query1 = ParseUser.getQuery();
					ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Checkin.TABLE_NAME)
							.whereEqualTo(Checkin.EVENT, eventId);
					query1.setLimit(resultNumber);
					query1.setSkip(skip);
					//TODO: now the search is case sensitive, change it.
					query1.whereContains(NAME, searchString).whereMatchesKeyInQuery(NAME, Checkin.USER, query2)
					.findInBackground(callback);
				}
			});
		}
	}
	
	public static class Event
	{
		public final static String TABLE_NAME = "events";
		public final static String NAME = "name";
		public final static String LOCATION = "location";
		public final static String RADIUS = "radius";
		public final static String STARTING_TIME = "starting_time";
		public final static String ENDING_TIME = "ending_time";
		public final static String DESCRIPTION = "description";
		public final static String CREATOR = "creator";
		
		
		public static void createEvent(ParseUser user, String name, Date startingTime, Date endingTime, double longitude, double latitude, int radius,String description, SaveCallback callback)
		{
			ParseObject event = new ParseObject(TABLE_NAME);
			event.put(NAME, name);
			event.put(LOCATION, new ParseGeoPoint(latitude, longitude));
			event.put(RADIUS, radius);
			event.put(STARTING_TIME,startingTime);
			event.put(ENDING_TIME,endingTime);
			event.put(CREATOR, user);
			event.put(DESCRIPTION, description);
			event.saveInBackground(callback);
		}
		
		public static void searchEvent(String searchString, int resultNumber,int skip,FindCallback<ParseObject> callback)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_NAME);
			//TODO: now the search is case sensitive, change it.
			query.setLimit(resultNumber);
			query.setSkip(skip);
			query.whereContains(NAME, searchString).findInBackground(callback);
		}
		
		public static void getEventFromId(String objectId, GetCallback<ParseObject> callback)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_NAME);
			query.getInBackground(objectId, callback);
		}
	}
	
	public static class Checkin
	{
		public final static String TABLE_NAME = "check_in";
		public final static String USER ="user";
		public final static String EVENT = "event";
		
		public static ParseObject checkIn(ParseUser user, ParseObject event,SaveCallback callback )
		{
			ParseObject checkin = new ParseObject(TABLE_NAME);
			checkin.put(USER,user);
			checkin.put(EVENT,event);
			checkin.saveInBackground(callback);
			return checkin;
		}
		
		public static void checkOut(ParseObject checkin,DeleteCallback callback)
		{
			checkin.deleteInBackground(callback);
		}
		
		public static void getCheckin(ParseUser user, ParseObject event,FindCallback<ParseObject> callback)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_NAME);
			query.whereEqualTo(USER, user).whereEqualTo(EVENT, event).findInBackground(callback);
		}
	}
	
	
}
