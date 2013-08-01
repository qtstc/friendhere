package com.tao.finder.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.location.Location;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Defines the interface for communication between the app and the Parse server.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class ParseContract {
	// Used for initialize Parse client.
	public final static String APPLICATION_ID = "t2zKB0ekNi8wWLoAmfc2usjmV03kAAygt4tzI0Dx";
	public final static String CLIENT_KEY = "ks4ddRM4qaCOGl4ZPLN0xxFD3AiaZ6Vj2elbFwmP";

	/**
	 * User table as in the Parse server.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	public static class User {
		public final static String NAME = "name";
		public final static String PHONE = "phone";
		public final static String LOCATION = "location";

		/**
		 * Check whether a ParseUser is currently logged in.
		 * 
		 * @return true if a user is logged in.
		 */
		public static boolean isLoggedIn() {
			if (ParseUser.getCurrentUser() == null)
				return false;
			return true;
		}

		/**
		 * Updates the location of a ParseUser.
		 * 
		 * @param user
		 *            the user.
		 * @param l
		 *            the location
		 * @throws ParseException 
		 */
		public static void updateLocation(ParseUser user, Location l) throws ParseException {
			user.put(LOCATION, toGeoPoint(l));
			user.save();
		}

		/**
		 * Converts Location to ParseGeoPoint.
		 * 
		 * @param l
		 *            the Location instance
		 * @return the ParseGeoPoint instance
		 */
		public static ParseGeoPoint toGeoPoint(Location l) {
			return new ParseGeoPoint(l.getLatitude(), l.getLongitude());
		}
		
		/**
		 * Get a ParseUser instance from the server based on its object id
		 * @param objectId the object id of the ParseUser instance.
		 * @param callback the callback to be executed afterwards.
		 */
		public static void getPersonById(String objectId,GetCallback<ParseUser> callback)
		{
			ParseUser.getQuery().getInBackground(objectId, callback);
		}

		/**
		 * Search for a person with a string on his name.
		 * 
		 * @param search
		 *            String the search string.
		 * @param eventId
		 *            the event in which to the person is checked in.
		 * @param resultNumber
		 *            the number of result desired.
		 * @param skip
		 *            the number of results that are already searched(and
		 *            returned)
		 * @param callback
		 *            the callback invoked at the end of the search.
		 */
		public static void searchPerson(final String searchString,
				final String eventId, final int resultNumber, final int skip,
				final FindCallback<ParseUser> callback) {
			Event.getEventById(eventId, new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject object, ParseException e) {
					ParseQuery<ParseUser> query1 = ParseUser.getQuery();
					query1.whereContains(NAME, searchString);
					ParseQuery<ParseObject> query2 = ParseQuery
							.getQuery(Checkin.TABLE_NAME);
					query2.setLimit(resultNumber);
					query2.setSkip(skip);
					query2.include(Checkin.USER);
					query2.whereEqualTo(Checkin.EVENT, object)
							.whereMatchesQuery(Checkin.USER, query1)
							.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> objects,
										ParseException e) {
									ArrayList<ParseUser> users = new ArrayList<ParseUser>();
									for (int i = 0; i < objects.size(); i++)
										users.add(objects.get(i).getParseUser(
												Checkin.USER));
									ParseObject.fetchAllIfNeededInBackground(
											users, callback);
								}
							});
				}
			});
		}
	}

	/**
	 * evnts table as in the Parse server.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	public static class Event {
		public final static String TABLE_NAME = "events";
		public final static String NAME = "name";
		public final static String LOCATION = "location";
		public final static String RADIUS = "radius";
		public final static String STARTING_TIME = "starting_time";
		public final static String ENDING_TIME = "ending_time";
		public final static String DESCRIPTION = "description";
		public final static String CREATOR = "creator";

		/**
		 * Create a new event.
		 * 
		 * @param user
		 *            the creator.
		 * @param name
		 *            the name of the event
		 * @param startingTime
		 *            the starting time
		 * @param endingTime
		 *            the ending time
		 * @param longitude
		 *            the longitude of the center of the event.
		 * @param latitude
		 *            the latitude of the center of the event.
		 * @param radius
		 *            radius of the event, indicating the size.
		 * @param description
		 *            the description on the event
		 * @param callback
		 *            callback to be invoked at the end of creation.
		 */
		public static void createEvent(ParseUser user, String name,
				Date startingTime, Date endingTime, double longitude,
				double latitude, int radius, String description,
				SaveCallback callback) {
			ParseObject event = new ParseObject(TABLE_NAME);
			event.put(NAME, name);
			event.put(LOCATION, new ParseGeoPoint(latitude, longitude));
			event.put(RADIUS, radius);
			event.put(STARTING_TIME, startingTime);
			event.put(ENDING_TIME, endingTime);
			event.put(CREATOR, user);
			event.put(DESCRIPTION, description);
			event.saveInBackground(callback);
		}

		/**
		 * Search for a event that contains the given string in its name
		 * 
		 * @param searchString
		 *            the string to be searched.
		 * @param resultNumber
		 *            the number of result to be returned.
		 * @param skip
		 *            the number of result already returned(to be skipped)
		 * @param callback
		 *            the callback to be invoked at the end of the search.
		 */
		public static void searchEvent(String searchString, int resultNumber,
				int skip, FindCallback<ParseObject> callback) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_NAME);
			// TODO: now the search is case sensitive, change it.
			query.setLimit(resultNumber);
			query.setSkip(skip);
			query.whereContains(NAME, searchString)
			.whereGreaterThan(ENDING_TIME, Calendar.getInstance().getTime())
			.findInBackground(callback);
		}

		/**
		 * Retrieve an event using its id.
		 * 
		 * @param objectId
		 *            the id of the event.
		 * @param callback
		 *            the callback to be invoked when the event is returned.
		 */
		public static void getEventById(String objectId,
				GetCallback<ParseObject> callback) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_NAME);
			query.getInBackground(objectId, callback);
		}
	}

	/**
	 * check_in table as in the ParseServer
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	public static class Checkin {
		public final static String TABLE_NAME = "check_in";
		public final static String USER = "user";
		public final static String EVENT = "event";

		/**
		 * Check a user in.
		 * 
		 * @param user
		 *            the user
		 * @param event
		 *            the event to be checked in
		 * @param callback
		 *            the callback to be invoked after checking in.
		 * @return the check_in object created.
		 */
		public static ParseObject checkIn(ParseUser user, ParseObject event,
				SaveCallback callback) {
			ParseObject checkin = new ParseObject(TABLE_NAME);
			checkin.put(USER, user);
			checkin.put(EVENT, event);
			checkin.saveInBackground(callback);
			return checkin;
		}

		/**
		 * Check a user out.
		 * 
		 * @param checkin
		 *            the check_in object
		 * @param callback
		 *            the callback to be invoked after checking out.
		 */
		public static void checkOut(ParseObject checkin, DeleteCallback callback) {
			checkin.deleteInBackground(callback);
		}

		/**
		 * Get the check_in object from user and event.
		 * 
		 * @param user
		 *            the user
		 * @param event
		 *            the event
		 * @param callback
		 *            the callback to be invoked when the object is found.
		 */
		public static void getCheckin(ParseUser user, ParseObject event,
				FindCallback<ParseObject> callback) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_NAME);
			query.whereEqualTo(USER, user).whereEqualTo(EVENT, event)
					.findInBackground(callback);
		}
	}

}
