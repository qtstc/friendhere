package com.tao.finder.ui;

import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.tao.finder.R;
import com.tao.finder.logic.BackgroundLocationUpdater;
import com.tao.finder.logic.LocationUtils;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.SuggestionProvider;
import com.tao.finder.logic.Utility;
import com.tao.finder.ui.SearchListFragment.OnSearchListener;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SearchView;

/**
 * This activity allows the user to view details of an event, search users who
 * are in the same event and check in/out.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class EventActivity extends LocationAwareActivity implements
		ActionBar.TabListener, OnSearchListener {

	public final static String TAG = "EventActivity";

	// A request to connect to Location Services
	private LocationRequest mLocationRequest;

	public static final String OBJECT_ID = "object_id";
	ParseObject event;
	ParseObject checkin;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeLocationRequest();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_event);
		handleIntent(getIntent());
	}

	/**
	 * Initialize the location location request instance
	 * used for periodical updates.
	 */
	private void initializeLocationRequest() {
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		// Set the update interval
		mLocationRequest
				.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest
				.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	/**
	 * Either start or stop background location tracking.
	 */
	private void changeLocationUpdate() {
		// If onConnect() is not called yet, return.
		if (mLocationClient == null)
			return;
		if (checkin == null) {
			mLocationClient.removeLocationUpdates(getPendingIntent());
			Log.d(TAG, "Removed PendingIntent");
		} else {
			mLocationClient.requestLocationUpdates(mLocationRequest,
					getPendingIntent());
			Log.d(TAG, "Added PendingIntent");
		}
	}

	/**
	 * Create and return a pending intent for the BackgroundLocationUpdater
	 * service.
	 * 
	 * @return the service created.
	 */
	private PendingIntent getPendingIntent() {
		Intent i = new Intent(getApplicationContext(),
				BackgroundLocationUpdater.class);
		return PendingIntent.getService(getApplicationContext(), 1, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Initialize the tabs used in the page. Mostly auto-generated.
	 */
	public void initializeTabs() {
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the two
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event, menu);

		// Change the text displayed depending on the settings
		MenuItem item = menu.findItem(R.id.action_checkin);
		if (checkin == null)
			item.setTitle(getString(R.string.action_check_in));
		else
			item.setTitle(getString(R.string.action_check_out));

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.action_person_search).getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_checkin:
			// TODO:check parseuser
			setProgressBarIndeterminateVisibility(true);
			// If the user is not checked in, check him in and start the
			// locationUpdater.
			if (checkin == null) {
				checkin = ParseContract.Checkin.checkIn(
						ParseUser.getCurrentUser(), event, new SaveCallback() {

							@Override
							public void done(ParseException e) {
								// TODO if exception caught, make checkin null.
								item.setTitle(getString(R.string.action_check_out));
								setProgressBarIndeterminateVisibility(false);
								mLocationClient.requestLocationUpdates(
										mLocationRequest, getPendingIntent());
							}
						});
				return true;
			}
			// Else if the user is checked in, check him out and terminate the
			// location updater.
			ParseContract.Checkin.checkOut(checkin, new DeleteCallback() {

				@Override
				public void done(ParseException e) {
					// TODO change the title back otherwise.
					item.setTitle(getString(R.string.action_check_in));
					setProgressBarIndeterminateVisibility(false);
					checkin = null;
					mLocationClient.removeLocationUpdates(getPendingIntent());
				}
			});
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages. Mostly auto-generated code.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.

			Fragment fragment = null;
			switch (position) {
			// TODO:Set tab 0 as the EventInfoFragment.
			case 0:
				fragment = new DummySectionFragment();
				Bundle args = new Bundle();
				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
						position + 1);
				fragment.setArguments(args);
				break;
			// Set tab 1 as the PersonSearchFragment.
			case 1:
				fragment = new PersonSearchFragment();
				break;
			default:
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.event_info).toUpperCase(l);
			case 1:
				return getString(R.string.event_people).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return null;
		}
	}

	/**
	 * Handles intents used to invoke this activity. If the intent contains an
	 * object_id for event, load the event info from the server. If the intent
	 * contains a search action, this method starts the search.
	 * 
	 * @param intent
	 *            the intent sent to this activity.
	 */
	private void handleIntent(Intent intent) {
		String objectId = intent.getStringExtra(OBJECT_ID);
		// If the intent is sent from EventListActivity with an object id.
		if (objectId != null) {
			ParseContract.Event.getEventFromId(objectId,
					new GetCallback<ParseObject>() {

						@Override
						public void done(ParseObject object, ParseException e) {
							event = object;
							initializeTabs();
							setTitle(event.getString(ParseContract.Event.NAME));
						}
					});
			if (ParseUser.getCurrentUser() == null) {
				checkin = null;
				return;
			}
			ParseContract.Checkin.getCheckin(ParseUser.getCurrentUser(), event,
					new FindCallback<ParseObject>() {

						@Override
						public void done(List<ParseObject> objects,
								ParseException e) {
							if (objects.size() == 0)
								checkin = null;
							else {
								invalidateOptionsMenu();
								checkin = objects.get(0);
							}
							// Only connect after the checkin is initialized
							// because
							// we need to start/stop background location tracker
							// in onConnecte().
							// based on the status of checkin.
							mLocationClient.connect();
						}
					});
			return;
		}
		// initializeTabs();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchString = intent.getStringExtra(SearchManager.QUERY)
					.trim();
			// use the query to search data
			PersonSearchFragment frag = (PersonSearchFragment) getSupportFragmentManager()
					.findFragmentByTag(Utility.getFragmentTag(R.id.pager,1));
			frag.newSearch(searchString, event.getObjectId());
			// Adds the current search to the search history.
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
			suggestions.saveRecentQuery(searchString, null);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
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
