package com.tao.finder;

import com.parse.Parse;
import com.parse.ParseObject;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class EventListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this, "t2zKB0ekNi8wWLoAmfc2usjmV03kAAygt4tzI0Dx", "ks4ddRM4qaCOGl4ZPLN0xxFD3AiaZ6Vj2elbFwmP"); 
		setContentView(R.layout.activity_event_list);
		ParseObject testObject = new ParseObject("TestObject");
		testObject.put("foo", "bar");
		testObject.saveInBackground();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_list, menu);
		return true;
	}

}
