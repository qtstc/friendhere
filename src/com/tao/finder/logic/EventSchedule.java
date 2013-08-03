package com.tao.finder.logic;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EventSchedule {
	
	public static final String EVENT_SCHEDULE_SHAREDPREFERENCES_KEY = "starting_time_sharedpreferences_key";
	private static final String DELIMS = "'";
	
	private HashMap<String, EventTime> events;
	
	private EventSchedule()
	{
		events = new HashMap<String, EventSchedule.EventTime>();
	}
	
	public void save(Context c)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		sp.edit().putString(EVENT_SCHEDULE_SHAREDPREFERENCES_KEY, toString()).commit();
	}
	
	public static EventSchedule getInstance(Context c)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		String s = sp.getString(EVENT_SCHEDULE_SHAREDPREFERENCES_KEY, "");
		if(s.equals(""))
			return new EventSchedule();
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
	
	public void add(String id, EventTime et)
	{
		events.put(id, et);
	}
	
	public void remove(String id)
	{
		events.remove(id);
	}
	
	/**
	 * Determine whether the updater should be started depending
	 * on the current time.
	 * @return true if the updater should be started.
	 */
	public boolean shouldStartUpdater(String currentEventID,boolean isStarting)
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
