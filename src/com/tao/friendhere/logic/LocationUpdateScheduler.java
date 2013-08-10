package com.tao.friendhere.logic;

import java.util.StringTokenizer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Instance of this class takes care of the scheduling of location updates. This
 * BroadcastReceiver is to be scheduled by the SchedulerManager. For each event
 * the user checked in, two scheduler will be created. The first one will be
 * invoked at the starting time of the event and the second one will be invoked
 * at the ending time of the event. Each scheduler checks the schedule of the
 * other events which the user is attending and either start or stop the
 * background location updater.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class LocationUpdateScheduler extends BroadcastReceiver implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	public static final String TAG = "LocationUpdateScheduler";

	private Context c;
	private String eventObjectID;// The id of the event which invoked the
									// scheduler.
	private boolean isStarting;// whether the scheduler is invoked at the start
								// of the event
	// A request to connect to Location Service
	private LocationRequest mLocationRequest;
	// Used by the google play location service
	private LocationClient mLocationClient;

	// The interval at which the location is updated, in millis second.
	public static final long UPDATE_INTERVAL = 5000;
	// The fastest interval at which the location can be updated
	public static final long FASTEST_UPDATE_INTERVAL = 1000;

	/**
	 * Initialize the location location request instance used for periodical
	 * updates.
	 */
	private void initializeLocationRequest() {
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		// Set the update interval
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
	}

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// Parse the URI which contains the information of the event that
		// invoked the scheduler
		String data = intent.getData().getSchemeSpecificPart();
		StringTokenizer st = new StringTokenizer(data);
		eventObjectID = st.nextToken(SchedulerManager.DELIMS);
		String suffix = st.nextToken(SchedulerManager.DELIMS);
		if (suffix.equals(SchedulerManager.STARTING_SUFFIX))
			isStarting = true;
		else
			isStarting = false;

		Log.e("LocationUpdateScheduler", data);

		c = arg0;
		mLocationClient = new LocationClient(c, this, this);
		initializeLocationRequest();
		mLocationClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.e(TAG, "Failed to connect to location service");
		// Not doing anything for now.
		// Not supposed to notify the user because the application can be
		// running in the background.
	}

	@Override
	public void onConnected(Bundle arg0) {
		// First get the schedule of all events.
		EventSchedule schedule = EventSchedule.getInstance(c);
		if (schedule.shouldBeUpdating(eventObjectID, isStarting)) {
			Log.e("Connected to location service, requested update",
					eventObjectID);
			mLocationClient.requestLocationUpdates(mLocationRequest,
					getLocationUpdatePendingIntent());
		} else {
			Log.e("Connected to location service,canceled update",
					eventObjectID);
			mLocationClient
					.removeLocationUpdates(getLocationUpdatePendingIntent());
		}
		mLocationClient.disconnect();
	}

	/**
	 * Get the PendingIntent used to invoke the location updater.
	 * 
	 * @return the PendingIntent created.
	 */
	private PendingIntent getLocationUpdatePendingIntent() {
		Intent i = new Intent(c, BackgroundLocationUpdater.class);
		return PendingIntent.getService(c, 1, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onDisconnected() {
		// Not doing anything.
	}

}
