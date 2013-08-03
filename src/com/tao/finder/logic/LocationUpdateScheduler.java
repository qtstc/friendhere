package com.tao.finder.logic;

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

public class LocationUpdateScheduler extends BroadcastReceiver implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

	
	private Context c;
	private String eventObjectID;
	private boolean isStarting;
	
	// A request to connect to Location Services
	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	
	
	/**
	 * Initialize the location location request instance used for periodical
	 * updates.
	 */
	private void initializeLocationRequest() {
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		// Set the update interval
		mLocationRequest.setInterval(5000);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(5000);
	}
	
	@Override
	public void onReceive(Context arg0, Intent intent) {
		//Parse the URI
		String data = intent.getData().getSchemeSpecificPart();
		StringTokenizer st = new StringTokenizer(data);
		eventObjectID = st.nextToken(SchedulerManager.DELIMS);
		String suffix = st.nextToken(SchedulerManager.DELIMS);
		if(suffix.equals(SchedulerManager.STARTING_SUFFIX))
			isStarting = true;
		else 
			isStarting = false;
		
		Log.e("LocationUpdateScheduler",data);
		
		c = arg0;
		mLocationClient = new LocationClient(c, this, this);
		initializeLocationRequest();
		mLocationClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.e("Connected to location service!",eventObjectID);
		EventSchedule schedule = EventSchedule.getInstance(c);
		if(schedule.shouldStartUpdater(eventObjectID,isStarting))
			mLocationClient.requestLocationUpdates(mLocationRequest, getLocationUpdateIntent());
		else
			mLocationClient.removeLocationUpdates(getLocationUpdateIntent());
	}
	
	private PendingIntent getLocationUpdateIntent() {
		Intent i = new Intent(c,
				BackgroundLocationUpdater.class);
		return PendingIntent.getService(c, 1, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}


	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
