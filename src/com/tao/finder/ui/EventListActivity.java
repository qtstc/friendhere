package com.tao.finder.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.tao.finder.R;
import com.tao.finder.logic.EventSuggestionProvider;
import com.tao.finder.logic.Events;
import com.tao.finder.logic.ParseContract;

import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SearchView;
import android.widget.SimpleAdapter;

public class EventListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleIntent(getIntent());
		
		Parse.initialize(this, ParseContract.APPLICATION_ID,ParseContract.CLIENT_KEY);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_event_list);
		//setProgressBarIndeterminateVisibility(true);
	}

	
	public Events loadEvents()
	{
		PullToRefreshListView eventList = (PullToRefreshListView)findViewById(R.id.event_list);
		eventList.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh(PullToRefreshBase refreshView) {
				// load data here.
			}
		});
		String[] from = new String[]{"event_item_upper_text","event_item_lower_text"};
		int[] to = new int[]{R.id.event_item_upper_text,R.id.event_item_lower_text};
		
		ArrayList<HashMap<String,String>> mapList = new ArrayList<HashMap<String,String>>();
		for(int i = 0;i<10;i++)
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(from[0],i+"upper");
			map.put(from[1],i+"lower");
			mapList.add(map);
		}
		
		SimpleAdapter eventListAdapter = new SimpleAdapter(this, mapList, R.layout.event_list_item, from, to);
		eventList.setAdapter(eventListAdapter);
		
		eventList.onRefreshComplete();
		return null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_list, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_event_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		return true;

	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.action_new_event:
			startActivity(new Intent(this,NewEventActivity.class));
			break;
		case R.id.action_settings:
			startActivity(new Intent(this,SettingsActivity.class));
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}
	


	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	/**
	 * Handles intents used to invoke this activity. If the intent contains a
	 * search action, this method starts the search.
	 * 
	 * @param intent
	 *            the intent sent to this activity.
	 */
	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			// use the query to search your data somehow
			// Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
			
			//Adds the current search to the search history.
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		                EventSuggestionProvider.AUTHORITY, EventSuggestionProvider.MODE);
			suggestions.saveRecentQuery(query, null);
		}
	}
	
	/**
	 * Clears the search history.
	 */
	private void clearSearchHistory()
	{
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                EventSuggestionProvider.AUTHORITY, EventSuggestionProvider.MODE);
		suggestions.clearHistory();
	}

}
