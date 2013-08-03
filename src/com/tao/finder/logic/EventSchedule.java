package com.tao.finder.logic;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * A data structure used to store the schedule of events
 * in the phone storage.
 * 
 * In the current implementation, data is converted to a string 
 * and then stored in the SharedPreferences.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 *
 */
public class EventSchedule {
	
	//The key used by the SharedPreferences to store data.
	public static final String EVENT_SCHEDULE_SHAREDPREFERENCES_KEY = "starting_time_sharedpreferences_key";
	//Delimiter used to separate different events in the string representation of this data structure
	private static final String DELIMS = "'";
	
	private HashMap<String, EventTime> events;
	
	private EventSchedule()
	{
		events = new HashMap<String, EventSchedule.EventTime>();
	}
	
	/**
	 * Save the data to the SharedPreferences.
	 * 
	 * @param c
	 */
	public void save(Context c)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		sp.edit().putString(EVENT_SCHEDULE_SHAREDPREFERENCES_KEY, toString()).commit();
	}
	
	/**
	 * Load existing instance from the SharedPreferences
	 * or create a new one if nothing exists.
	 * @param c
	 * @return a EventSchedule instance
	 */
	public static EventSchedule getInstance(Context c)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		String s = sp.getString(EVENT_SCHEDULE_SHAREDPREFERENCES_KEY, "");
		if(s.equals(""))//If no existing one is found.
			return new EventSchedule();
		
		//Parse the string representation.
		StringTokenizer st = new StringTokenizer(s);
		
		EventSchedule schedule = new EventSchedule();
		
		long currentTime = Calendar.getInstance().getTimeInMillis();
		while(st.hasMoreTokens())
		{
			String next = st.nextToken(DELIMS);
			EventTime et = new EventTime();
			String name = EventTime.parseEventTimeFromString(next, et);//Parse instance from string
			if(et.getEnding()>currentTime)//We only care about the events that haven't ended.
				schedule.add(name,et);
		}
		return schedule;
	}
	
	/**
	 * Add a new event to the schedule.
	 * 
	 * @param id the objectID of the event, as used by Parse.com
	 * @param et the EventTime instance associated with this event
	 */
	public void add(String id, EventTime et)
	{
		events.put(id, et);
	}
	
	/**
	 * Remove an event from the schedule.
	 * It's safe to call this method even 
	 * when the event is already removed.
	 * 
	 * @param id the objectID of the event, as used by Parse.com
	 */
	public void remove(String id)
	{
		events.remove(id);
	}
	
	/**
	 * Determine whether the updater should be started based on the 
	 * event schedule. 
	 * This method is to be called by an LocationUpdateScheduler instance
	 * 
	 * @param currentEventID the objectId of the event that invoked the LocationUpdateScheduler instance
	 * @param isStarting true if the LocationUpdateScheduler is invoked at the beginning of the event.
	 * @return true if the updater be running.
	 */
	public boolean shouldBeUpdating(String currentEventID,boolean isStarting)
	{
		long currentTime = Calendar.getInstance().getTimeInMillis();
		int count = 0;
		//If the scheduler is triggered to at the beginning of an event.
		if(isStarting)
			return true;
		for(Entry<String,EventTime> e:events.entrySet())
		{
			//Consider the current event separately.
			if(currentEventID.equals(e.getKey()))
				continue;
			EventTime et = e.getValue();
			if(et.starting < currentTime)
				count++;
			if(et.ending < currentTime)
				count--;
		}
		if(count > 0)
			return true;
		return false;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Entry<String, EventTime> e:events.entrySet())
		{
			sb.append(e.getValue().toString(e.getKey())+DELIMS);
		}
		return sb.toString();
	}
	
	/**
	 * A data structure used to store the start and ending time
	 * of individual events.
	 * It is used in the EventSchedule.
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 *
	 */
	public static class EventTime
	{
		private long starting;
		private long ending;
		
		public EventTime()
		{
			this((long)0,(long)0);
		}
		
		public EventTime(Long starting, Long ending)
		{
			setTime(starting, ending);
		}
		
		public void setTime(Long starting, Long ending)
		{
			this.starting = starting;
			this.ending = ending;
		}
		
		public Long getStarting()
		{
			return starting;
		}
		
		public Long getEnding()
		{
			return ending;
		}
		
		public String toString(String id)
		{
			return starting+" "+ending+" "+id;
		}
		
		public static String parseEventTimeFromString(String s,EventTime e)
		{
			StringTokenizer st = new StringTokenizer(s);
			Long starting = Long.parseLong(st.nextToken());
			Long ending = Long.parseLong(st.nextToken());
			String id = st.nextToken();
			e.setTime(starting, ending);
			return id;
		}
	}
}
