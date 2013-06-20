package com.tao.finder.ui;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.tao.finder.R;
import com.tao.finder.logic.EventAdapter;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.SuggestionProvider;

import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class EventListActivity extends Activity {

	private PullToRefreshListView eventList;
	private EventAdapter eventAdapter;
	private int resultLimit;
	private int resultSkip;
	private String searchString;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Parse.initialize(this, ParseContract.APPLICATION_ID,ParseContract.CLIENT_KEY);
		ParseFacebookUtils.initialize(this.getString(R.string.app_id));
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_event_list);
		//setProgressBarIndeterminateVisibility(true);
		
		initializeList();
		handleIntent(getIntent());
	}
	
	public void initializeList()
	{
		resultSkip = 0;
		resultLimit = 1;
		searchString = "";
		eventAdapter = null;
		eventList = (PullToRefreshListView)EventListActivity.this.findViewById(R.id.event_list);
		eventList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				if(resultSkip == 0)
				{
					eventList.onRefreshComplete();
					return;
				}
				ParseContract.Event.searchEvent(searchString, resultLimit, resultSkip, new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> objects, ParseException e) {
						resultSkip += objects.size();
						eventAdapter.addEvents(objects);
						eventList.onRefreshComplete();
					}
				});
			}
		});
		
		eventList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				ParseObject event = (ParseObject)parent.getAdapter().getItem(position);
				Intent eventIntent = new Intent(EventListActivity.this,EventActivity.class);
				eventIntent.putExtra(EventActivity.OBJECT_ID, event.getObjectId());
				startActivity(eventIntent);
			}
		});
	}

	/**
	 * Method to be used to get the keyhash used by Facebook.
	 * It needs to be put in onCreate().
	 */
	public void getKeyHash()
	{
		try {
		    PackageInfo info = getPackageManager().getPackageInfo(
		          "com.tao.finder", PackageManager.GET_SIGNATURES);
		    for (Signature signature : info.signatures) 
		        {
		           MessageDigest md = MessageDigest.getInstance("SHA");
		           md.update(signature.toByteArray());
		           Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
		        }
		    } catch (NameNotFoundException e) {Log.e("ee",e.toString());
		} catch (NoSuchAlgorithmException e1) {Log.e("ee",e1.toString());}
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
			startActivity(new Intent(this,LoginActivity.class));
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
			searchString = intent.getStringExtra(SearchManager.QUERY).trim();
			// use the query to search data
			setProgressBarIndeterminateVisibility(true);
			ParseContract.Event.searchEvent(searchString, resultLimit, resultSkip, new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> objects, ParseException e) {
//					String text = ""+objects.size();
//					if(objects.size() >0)
//					{
//						text += objects.get(0).getString(ParseContract.Event.DESCRIPTION);
//					}
//					Toast.makeText(EventListActivity.this, text, Toast.LENGTH_LONG).show();
					resultSkip += objects.size();
					eventAdapter = new EventAdapter(EventListActivity.this,objects);
					eventList.setAdapter(eventAdapter);
					setProgressBarIndeterminateVisibility(false);
				}
			});
			
			//Adds the current search to the search history.
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		               SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
			suggestions.saveRecentQuery(searchString, null);
		}
	}
	
	/**
	 * Clears the search history.
	 */
	private void clearSearchHistory()
	{
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
		suggestions.clearHistory();
	}

}
