package com.tao.finder.logic;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationUpdateScheduler extends BroadcastReceiver implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

	
	Context c;
	
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
	public void onReceive(Context arg0, Intent arg1) {
		Log.e("Broadcast recerver triggered","haha");
		c = arg0;
		mLocationClient = new LocationClient(c, this, this);
		initializeLocationRequest();
		//mLocationClient.connect();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		Log.e("Got string!",sp.getString("ss", "default value"));
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.e("Connected to location service!","yea!");
		//mLocationClient.requestLocationUpdates(
				//mLocationRequest, getLocationUpdateIntent());
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
