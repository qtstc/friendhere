package com.tao.finder.logic;

import java.util.Calendar;
import java.util.Date;

import com.tao.finder.R;
import com.tao.finder.logic.EventSchedule.EventTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class SchedulerManager {
	private AlarmManager manager;
	private EventSchedule schedule;
	private Context c;

	public static final String DELIMS = "'";
	public static final String STARTING_SUFFIX = "s";
	public static final String ENDING_SUFFIX = "e";

	public SchedulerManager(Context c) {
		this.c = c;
		manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
		schedule = EventSchedule.getInstance(c);
	}

	public void addScheduler(String eventObjectID, Date startingDate,
			Date endingDate) {
		Long startingTime = startingDate.getTime();
		Long endingTime = endingDate.getTime();
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		if (currentTime > endingTime)// If event already ended. We assume ending
										// date is later than starting date
										// here.
			return;
		
		// First add the event to the Schedule
		schedule.add(eventObjectID, new EventTime(startingTime, endingTime));
		schedule.save(c);
		// Schedule the scheduler for the starting time.
		// The scheduler will be triggered if the event already started.
		// http://developer.android.com/reference/android/app/AlarmManager.html#set(int,
		// long, android.app.PendingIntent)
		manager.set(AlarmManager.RTC_WAKEUP, startingTime,
				getPendingIntent(eventObjectID, true));
		// Schedule the scheduler for the ending time.
		manager.set(AlarmManager.RTC_WAKEUP, endingTime,
				getPendingIntent(eventObjectID, false));
	}

	public void removeScheduler(String eventObjectID) {
		manager.cancel(getPendingIntent(eventObjectID, true));
		manager.cancel(getPendingIntent(eventObjectID, false));
		schedule.remove(eventObjectID);
		schedule.save(c);
	}

	private PendingIntent getPendingIntent(String eventObjectID,
			boolean isStarting) {
		Intent i = new Intent(c, LocationUpdateScheduler.class);
		String suffix = DELIMS + ENDING_SUFFIX;
		if (isStarting)
			suffix = DELIMS + STARTING_SUFFIX;
		i.setData(Uri.parse(c
				.getString(R.string.location_update_scheduler_scheme)
				+ eventObjectID + suffix));
		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}
}
