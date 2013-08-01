package com.tao.finder.ui;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.tao.finder.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Base class for activities that uses the Google Play Location service.
 * This class contains the methods that deals with connection failure
 * and a few constants that are used for acquiring location.
 * 
 * The child classes need to deal with the setting up/terminating of connection.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 *
 */
public abstract class LocationAwareActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

	public static final String TAG = "LocationAwareActivity";
	
	public static final int DEFAULT_ZOOM_LEVEL = 15;// The default zoom level of
	// the map.
	public static final int EVENT_AREA_STROKE_COLOR = Color.GRAY;
	public static final int EVENT_AREA_FILL_COLOR = Color.argb(100, 100, 100,100);
	
	// Stores the current instantiation of the location client in this object
	protected LocationClient mLocationClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocationClient = new LocationClient(this, this, this);
	}
	
	@Override 
	public void onResume()
	{
		super.onResume();
		if(!servicesConnected(this))
			finish();//If not connected to Google Play service, close the activity.
	}
	
	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in LocationUpdateRemover and LocationUpdateRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Log.d(TAG, "onActivityResult");
		// Choose what to do based on the request code
		switch (requestCode) {
		// TODO:Inform user with dialog about the failure.
		// If the request code matches the code sent in onConnectionFailed
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:
				// Log the result
				Log.d(TAG, "requestCode:" + getString(R.string.resolved));
				break;
			// If any other result was returned by Google Play services
			default:
				Toast.makeText(this, R.string.location_service_connection_error_toast_message, Toast.LENGTH_SHORT).show();
				finish();
				// Log the result
				Log.d(TAG, "requestCode:" + getString(R.string.no_resolution));
				break;
			}

			// If any other request code was received
		default:
			Toast.makeText(this, R.string.location_service_connection_error_toast_message, Toast.LENGTH_SHORT).show();
			finish();
			// Report that this Activity received an unknown requestCode
			Log.d(TAG,
					"requestCode"
							+ getString(R.string.unknown_activity_request_code,
									requestCode));
			break;
		}
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			Log.d(TAG, "onConnectionFailed:has resolution");
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */

			} catch (IntentSender.SendIntentException e) {
				Toast.makeText(this, R.string.location_service_connection_error_toast_message, Toast.LENGTH_SHORT).show();
				finish();//Close the activity.
				// Log the error
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, R.string.location_service_connection_error_toast_message, Toast.LENGTH_SHORT).show();
			finish();
			// If no resolution is available, display a dialog to the user with
			// the error;
			Log.d(TAG, "onConnectionFailed:no resolution");
		}
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		Log.d(TAG, "Connected location client");
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "Disconnected location client");
	}
	
	
	/**
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	public static boolean servicesConnected(Context c) {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(c);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			Toast.makeText(c, R.string.google_play_service_connection_error_toast_message, Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	
	/*
	 * Constants for location update parameters
	 */
	// Milliseconds per second
	public static final int MILLISECONDS_PER_SECOND = 1000;

	//Constants used for periodical location updates.
	
	// The update interval
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// A fast interval ceiling
	public static final int FAST_CEILING_IN_SECONDS = 1;
	// Update interval in milliseconds
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// A fast ceiling of update intervals, used when the app is visible
	public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* FAST_CEILING_IN_SECONDS;

	
	// Create an empty string for initializing strings
	public static final String EMPTY_STRING = new String();
}
