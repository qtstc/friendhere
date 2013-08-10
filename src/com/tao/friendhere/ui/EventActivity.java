package com.tao.friendhere.ui;

import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.tao.friendhere.R;
import com.tao.friendhere.logic.BackgroundLocationUpdater;
import com.tao.friendhere.logic.ParseContract;
import com.tao.friendhere.logic.SchedulerManager;
import com.tao.friendhere.logic.SuggestionProvider;
import com.tao.friendhere.logic.Utility;
import com.tao.friendhere.ui.SearchListFragment.OnSearchListener;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

/** 
 * This activity allows the user to view details of an event, search users who
 * are in the same event and check in/out
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class EventActivity extends LocationAwareActivity implements
		ActionBar.TabListener, OnSearchListener {

	public final static String TAG = "EventActivity";

	private boolean fragmentLoaded;

	private ParseObject event;
	private ParseObject checkin;
	private SchedulerManager manager;

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
		fragmentLoaded = false;
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_event);

		setProgressBarIndeterminateVisibility(true);
		manager = new SchedulerManager(this);
		// Load the data from the server.
		String objectId = getIntent().getStringExtra(
				SearchListFragment.OBJECT_ID);
		// If the intent is sent from EventListActivity with an object id.
		if (objectId != null) {
			ParseContract.Event.getEventById(objectId,
					new GetCallback<ParseObject>() {

						@Override
						public void done(final ParseObject object,
								ParseException e) {
							if (e != null) {
								Toast.makeText(
										EventActivity.this,
										R.string.connection_error_toast_message,
										Toast.LENGTH_SHORT).show();
								finish();
								return;
							}
							event = object;
							if (ParseUser.getCurrentUser() == null) {
								checkin = null;
							}
							else
							{
							ParseContract.Checkin.getCheckin(
									ParseUser.getCurrentUser(), event,
									new FindCallback<ParseObject>() {

										@Override
										public void done(
												List<ParseObject> objects,
												ParseException e) {
											if (e != null) {
												Toast.makeText(
														EventActivity.this,
														R.string.connection_error_toast_message,
														Toast.LENGTH_SHORT)
														.show();
												finish();
												return;
											}

											if (objects.size() == 0)
												checkin = null;
											else {
												checkin = objects.get(0);
											}
										}
									});
							}
							// Start initialize the GUI here,
							// after loading all the data.
							setTitle(event
									.getString(ParseContract.Event.NAME));
							initializeTabs();
							fragmentLoaded = true;
							invalidateOptionsMenu();
							setProgressBarIndeterminateVisibility(false);
						}
					});
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		super.onConnected(arg0);
		if (ParseUser.getCurrentUser() == null)// If not logged in, return.
			return;
		if (checkin == null)// If not checkedin
			mLocationClient.removeLocationUpdates(getLocationUpdateIntent());
		else
			Log.e(TAG, "Error: checkin is not null in onConnecred.");
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	/**
	 * Create and return a pending intent for the BackgroundLocationUpdater
	 * service.
	 * 
	 * @return the service created.
	 */
	private PendingIntent getLocationUpdateIntent() {
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
				getSupportFragmentManager(), event);

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
		if (!fragmentLoaded)
			return true;

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event, menu);

		// Change the text displayed depending on the settings
		MenuItem checkinItem = menu.findItem(R.id.action_checkin);

		if (checkin == null)
			checkinItem.setTitle(getString(R.string.action_check_in));
		else
			checkinItem.setTitle(getString(R.string.action_check_out));

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
			if (!isLoggedin())// Return if the user is not logged in.
				return true;
			setProgressBarIndeterminateVisibility(true);
			item.setEnabled(false);
			// If the user is not checked in, check him in and start the
			// locationUpdater.
			if (checkin == null) {
				checkin = ParseContract.Checkin.checkIn(
						ParseUser.getCurrentUser(), event, new SaveCallback() {

							@Override
							public void done(ParseException e) {
								setProgressBarIndeterminateVisibility(false);
								item.setEnabled(true);
								if (e != null) {
									checkin = null;
									Toast.makeText(
											EventActivity.this,
											R.string.connection_error_toast_message,
											Toast.LENGTH_SHORT).show();
									return;
								}

								item.setTitle(getString(R.string.action_check_out));
								// Add a scheduler for this event.
								// If the event is already started, the
								// scheduler will be invoked immediately to
								// start the location updater.
								manager.addScheduler(
										event.getObjectId(),
										event.getDate(ParseContract.Event.STARTING_TIME),
										event.getDate(ParseContract.Event.ENDING_TIME));
							}
						});
				return true;
			}
			// Else if the user is checked in, check him out and terminate the
			// location updater.

			// First clear the result of search list.
			PersonSearchFragment frag = (PersonSearchFragment) getSupportFragmentManager()
					.findFragmentByTag(
							Utility.getFragmentTag(R.id.pager,
									SectionsPagerAdapter.PERSON_SEARCH_PAGE));
			frag.clearResults();
			ParseContract.Checkin.checkOut(checkin, new DeleteCallback() {

				@Override
				public void done(ParseException e) {
					setProgressBarIndeterminateVisibility(false);
					item.setEnabled(true);
					if (e != null) {
						Toast.makeText(EventActivity.this,
								R.string.connection_error_toast_message,
								Toast.LENGTH_SHORT).show();
						return;
					}

					item.setTitle(getString(R.string.action_check_in));
					checkin = null;

					boolean shouldBeUpdating = manager.removeScheduler(event
							.getObjectId());// First remove the scheduler.
					if (shouldBeUpdating)
						return;
					if (!mLocationClient.isConnected()
							&& !mLocationClient.isConnecting())
						mLocationClient.connect();
					else if (mLocationClient.isConnected())
						mLocationClient
								.removeLocationUpdates(getLocationUpdateIntent());
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

		public static final int EVENT_INFO_PAGE = 0;
		public static final int PERSON_SEARCH_PAGE = 1;

		ParseObject event;

		public SectionsPagerAdapter(FragmentManager fm, ParseObject event) {
			super(fm);
			this.event = event;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment = null;
			switch (position) {
			// TODO:Set tab 0 as the EventInfoFragment.
			case EVENT_INFO_PAGE:
				fragment = EventInfoFragment.newInstance(event);
				break;
			// Set tab 1 as the PersonSearchFragment.
			case PERSON_SEARCH_PAGE:
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
			case EVENT_INFO_PAGE:
				return getString(R.string.info).toUpperCase(l);
			case PERSON_SEARCH_PAGE:
				return getString(R.string.people).toUpperCase(l);
			}
			return null;
		}
	}

	public static class EventInfoFragment extends Fragment {

		private static final String STARTING_TIME_KEY = "starting_time_key";
		private static final String ENDING_TIME_KEY = "ending_time_key";
		private static final String DESCRIPTION_KEY = "description_key";
		private static final String CENTER_LOCATION_KEY = "center_location_key";
		private static final String RADIUS_KEY = "radius_key";

		public static EventInfoFragment newInstance(ParseObject event) {
			EventInfoFragment frag = new EventInfoFragment();
			Bundle args = new Bundle();
			args.putString(STARTING_TIME_KEY, Utility.dateToString(event
					.getDate(ParseContract.Event.STARTING_TIME)));
			args.putString(ENDING_TIME_KEY, Utility.dateToString(event
					.getDate(ParseContract.Event.ENDING_TIME)));
			args.putString(DESCRIPTION_KEY,
					event.getString(ParseContract.Event.DESCRIPTION));
			args.putParcelable(CENTER_LOCATION_KEY, Utility.toLatLng(event
					.getParseGeoPoint(ParseContract.Event.LOCATION)));
			args.putInt(RADIUS_KEY, event.getInt(ParseContract.Event.RADIUS));
			frag.setArguments(args);

			return frag;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_event_info,
					container, false);

			Bundle args = getArguments();
			// Initialize view
			SupportMapFragment frag = (SupportMapFragment) getFragmentManager()
					.findFragmentById(R.id.info_map);
			GoogleMap map = frag.getMap();

			// Change the display settings of the map.
			UiSettings settings = map.getUiSettings();
			settings.setCompassEnabled(true);
			map.setMyLocationEnabled(true);
			// settings.setMyLocationButtonEnabled(true);//Not necessary?

			LatLng centerPoint = (LatLng) args
					.getParcelable(CENTER_LOCATION_KEY);

			// Zoom to the location of the user
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint,
					LocationAwareActivity.DEFAULT_ZOOM_LEVEL));

			// Create the circle that represents the event area.
			CircleOptions circleOptions = new CircleOptions()
					.center(centerPoint)
					.radius(args.getInt(RADIUS_KEY))
					// In meters
					.strokeWidth((float) 4)
					.strokeColor(LocationAwareActivity.EVENT_AREA_STROKE_COLOR)
					.fillColor(LocationAwareActivity.EVENT_AREA_FILL_COLOR);
			map.addCircle(circleOptions);

			TextView description = (TextView) rootView
					.findViewById(R.id.info_description_textview);
			description.setText(args.getString(DESCRIPTION_KEY));
			TextView startingTime = (TextView) rootView
					.findViewById(R.id.info_startingtime_textview);
			TextView endingTime = (TextView) rootView
					.findViewById(R.id.info_endingtime_textview);
			startingTime.setText(getString(R.string.start_at) + " "
					+ args.getString(STARTING_TIME_KEY));
			endingTime.setText(getString(R.string.end_at) + " "
					+ args.getString(ENDING_TIME_KEY));

			return rootView;
		}
	}

	/**
	 * Handles intents used to invoke this activity. If the intent contains a
	 * search action, this method starts the search.
	 * 
	 * @param intent
	 *            the intent sent to this activity.
	 */
	private void handleIntent(Intent intent) {
		if (!isLoggedin())// Return if the user is using search && the user is
							// not logged in.
			return;
		else if (!isCheckedIn())// If the user is logged in but did not check
								// in, notify him with a toast
			return;

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchString = intent.getStringExtra(SearchManager.QUERY)
					.trim();
			// use the query to search data
			PersonSearchFragment frag = (PersonSearchFragment) getSupportFragmentManager()
					.findFragmentByTag(
							Utility.getFragmentTag(R.id.pager,
									SectionsPagerAdapter.PERSON_SEARCH_PAGE));
			frag.newSearch(searchString, event.getObjectId());
			// Adds the current search to the search history.
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
			suggestions.saveRecentQuery(searchString, null);

			// Switch to the search page.
			mViewPager.setCurrentItem(1);
		}
	}

	/**
	 * Check whether the user is logged in. If not, a dialog requesting the user
	 * to log in will be shown.
	 * 
	 * @return true if the user is logged in, false otherwise.
	 */
	private boolean isLoggedin() {
		if (ParseUser.getCurrentUser() != null)
			return true;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.request_login_dialog_message);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(EventActivity.this,
						SettingsActivity.class);
				startActivity(i);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
		return false;
	}

	/**
	 * Check whether the user is checked in. Display a toast message if he is
	 * not.
	 * 
	 * @return true if the user is checked in.
	 */
	private boolean isCheckedIn() {
		if (checkin != null)
			return true;
		Toast.makeText(this, R.string.request_checkin_toast_message,
				Toast.LENGTH_SHORT).show();
		return false;
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
