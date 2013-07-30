package com.tao.finder.ui;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.tao.finder.R;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.SuggestionProvider;
import com.tao.finder.ui.SearchListFragment.OnSearchListener;

import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.Window;
import android.widget.SearchView;

/**
 * This Activity display a list of events for the user to search/select. It also
 * contains navigation to other activities which allow users to view event, add
 * event, change settings etc.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class EventListActivity extends FragmentActivity implements
		OnSearchListener {

	public final static String TAG = "EventListActivity";
	private final static String EVENT_SEARCH_FRAGMENT_TAG = "event_search_fragment_tag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// First intialize Parse and Facebook.
		Parse.initialize(this, ParseContract.APPLICATION_ID,
				ParseContract.CLIENT_KEY);
		ParseFacebookUtils.initialize(this.getString(R.string.app_id));

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_event_list);
		setProgressBarIndeterminate(true);

		initializeList();
		handleIntent(getIntent());
		getKeyHash();
	}

	/**
	 * This method adds the EventSearchFragment to the activity.
	 */
	public void initializeList() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		EventSearchFragment fragment = new EventSearchFragment();
		fragmentTransaction.add(R.id.event_list_layout, fragment,
				EVENT_SEARCH_FRAGMENT_TAG);
		fragmentTransaction.commit();
	}

	/**
	 * Method to be used to get the keyhash used by Facebook. It needs to be put
	 * in onCreate(). The keyhash changes as the ssh used for packaging
	 * appliation changes. It needs to be registered on Facebook developer
	 * console. Only used for debuging.
	 */
	public void getKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"com.tao.finder", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d(TAG,
						"KeyHash:"
								+ Base64.encodeToString(md.digest(),
										Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "NameNotFoundException" + e.toString());
		} catch (NoSuchAlgorithmException e1) {
			Log.e(TAG, "NoSuchAlgorithmException" + e1.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_list, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.action_event_search).getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new_event:// Start NewEventActivity.
			startActivity(new Intent(this, NewEventActivity.class));
			break;
		case R.id.action_settings:// Start LoginAcvity.
			startActivity(new Intent(this, SettingsActivity.class));
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
			String searchString = intent.getStringExtra(SearchManager.QUERY)
					.trim();
			// use the query to search data
			EventSearchFragment frag = (EventSearchFragment) getSupportFragmentManager()
					.findFragmentByTag(EVENT_SEARCH_FRAGMENT_TAG);
			frag.newSearch(searchString);
			// Adds the current search to the search history.
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
			suggestions.saveRecentQuery(searchString, null);
		}
	}

	/**
	 * Clears the search history.
	 */
	@SuppressWarnings("unused")
	private void clearSearchHistory() {
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
				SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
		suggestions.clearHistory();
	}

	@Override
	public void onSearchStarted() {
		setProgressBarIndeterminateVisibility(true);
	}

	@Override
	public void onSearchEnded() {
		setProgressBarIndeterminateVisibility(false);
	}

}
