package com.tao.finder.logic;

import com.google.android.gms.location.LocationClient;
import com.parse.Parse;
import com.parse.ParseUser;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

public class BackgroundLocationUpdater extends IntentService {

	public BackgroundLocationUpdater(){
		this("Background Updater");
	}
	
	public BackgroundLocationUpdater(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	 protected void onHandleIntent(Intent intent) {
	   
	   Location location = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);
	   if(location !=null){
		Parse.initialize(this, ParseContract.APPLICATION_ID,ParseContract.CLIENT_KEY);
		ParseUser user = ParseUser.getCurrentUser();
		if(user != null)
			Log.e("Background Receiver",user.getUsername());
		else
			Log.e("Background Receiver","not logged in");
	    Log.e("Background Receiver", "onHandleIntent " + location.getLatitude() + "," + location.getLongitude());
	   }
	 }

}
