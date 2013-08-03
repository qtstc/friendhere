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

/**
 * Instance of this class manages the schedulers that are used to schedule
 * location updates.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class SchedulerManager {
	// The AlarmManager instance used to schedule schedulers.
	private AlarmManager manager;
	// The event schedule
	private EventSchedule schedule;

	private Context c;

	// Strings used in the URI included in the pending intent.
	public static final String DELIMS = "'";
	public static final String STARTING_SUFFIX = "s";
	public static final String ENDING_SUFFIX = "e";

	public SchedulerManager(Context c) {
		this.c = c;
		manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
		schedule = EventSchedule.getInstance(c);
	}

	/**
	 * Add new schedulers. Call this when the user checked in at a event. It
	 * created two schedulers, the first one will be invoked at the starting
	 * time of the new event, whereas the second one will be invoked at the
	 * ending time of the new event.
	 * 
	 * @param eventObjectID
	 *            the objectID of the event. As used by Parse.com
	 * @param startingDate
	 *            the starting time of the event. If before current time, the
	 *            first scheduler will be invoked immediately.
	 * @param endingDate
	 *            the ending time of the event. If before current time, no
	 *            scheduler will be invoked.
	 */
	public void addScheduler(String eventObjectID, Date startingDate,
			Date endingDate) {
		Long startingTime = startingDate.getTime();
		Long endingTime = endingDate.getTime();
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		// If event already ended. We assume ending date is later than starting
		// date here.
		if (currentTime > endingTime)
			return;

		// First add the event to the schedule
		schedule.add(eventObjectID, new EventTime(startingTime, endingTime));
		schedule.save(c);

		// Schedule the scheduler for the starting time.
		// The scheduler will be triggered if the event already started.
		// http://developer.android.com/reference/android/app/AlarmManager.html#set(int,
		// long, android.app.PendingIntent)
		manager.set(AlarmManager.RTC_WAKEUP, startingTime,
				getSchedulerPendingIntent(eventObjectID, true));
		// Schedule the scheduler for the ending time.
		manager.set(AlarmManager.RTC_WAKEUP, endingTime,
				getSchedulerPendingIntent(eventObjectID, false));
	}

	/**
	 * Remove schedulers. Call this when the user checked out at events.
	 * 
	 * @param eventObjectID
	 *            the objectID of the event. As used by Parse.com.
	 * @return true if the location updater should continue running after the
	 *         removal of the event. false otherwise.
	 */
	public boolean removeScheduler(String eventObjectID) {
		// First determine whether the updater should continue running,
		// before the event is removed from the schedule.
		boolean shouldBeUpdating = schedule.shouldBeUpdating(eventObjectID,
				false);
		// Cancel the two schedulers associated with the event.
		manager.cancel(getSchedulerPendingIntent(eventObjectID, true));
		manager.cancel(getSchedulerPendingIntent(eventObjectID, false));
		// Remove the event from the schedule.
		schedule.remove(eventObjectID);
		schedule.save(c);

		return shouldBeUpdating;
	}

	/**
	 * Get the PendingIntent used to invoke a UpdateScheduler
	 * 
	 * @param eventObjectID
	 *            the objectId of the event. As used by Parse.com
	 * @param isStarting
	 *            true if the scheduler is invoked at the starting time of the
	 *            event.
	 * @return the PendingIntent created.
	 */
	private PendingIntent getSchedulerPendingIntent(String eventObjectID,
			boolean isStarting) {
		Intent i = new Intent(c, LocationUpdateScheduler.class);
		// We have to add a suffix to the URI because we need to differentiate
		// the
		// two schedulers invoked at the start and end of the event.
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
