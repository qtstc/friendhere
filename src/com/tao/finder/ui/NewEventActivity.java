package com.tao.finder.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.tao.finder.R;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.Utility;
import com.tao.finder.ui.PersonActivity.SectionsPagerAdapter;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class NewEventActivity extends LocationAwareActivity implements
ActionBar.TabListener{

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	public static final double DEFAULT_EVENT_RADIUS = 0.001;// The default
															// radius of the
															// event as
															// expressed in
															// longitude/latitude
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_new_event);
		
		if (!(mLocationClient.isConnected() || mLocationClient
				.isConnecting()))
			mLocationClient.connect();
	}
	/**
	 * Initialize the pager used to allow the user to create a new event.
	 * @param currentLocation the current location of the user, used to initialize the map.
	 */
	private void initializeTabs(LatLng currentLocation) {
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(),currentLocation);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
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
	}
	

	/*
	 * Initialize the map instance after connected to the Google Play service.
	 * We put the initialization here because setting up the map requires the
	 * current location of the user, which can only be acquired after connected
	 * to the service.
	 * 
	 * @see
	 * com.tao.finder.ui.LocationAwareActivity#onConnected(android.os.Bundle)
	 */
	@Override
	public void onConnected(Bundle arg0) {
		Location l = mLocationClient.getLastLocation();
		LatLng centerPoint = Utility.toLatLng(l);
		initializeTabs(centerPoint);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create_event:
			NewEventMapFragment mapFrag = (NewEventMapFragment) getSupportFragmentManager()
			.findFragmentByTag(Utility.getFragmentTag(R.id.pager, SectionsPagerAdapter.NEW_EVENT_MAP_PAGE));
			NewEventFormFragment formFrag = (NewEventFormFragment) getSupportFragmentManager()
					.findFragmentByTag(Utility.getFragmentTag(R.id.pager, SectionsPagerAdapter.NEW_EVENT_FORM_PAGE));

			// Validate user input.
			String errorMessage = "";
			Date start = formFrag.getStartingTime();
			Date end = formFrag.getEndingTime();
			if (start.after(end))
				errorMessage += "\n" + getText(R.string.starting_time_too_late);
			if (end.before(Calendar.getInstance().getTime()))
				errorMessage += "\n" + getText(R.string.ending_time_too_early);

			String name = formFrag.getName().trim();
			if (name.equals(""))
				errorMessage += "\n" + getText(R.string.empty_name);
			String description = formFrag.getDescription().trim();

			errorMessage = errorMessage.trim();
			if (!errorMessage.isEmpty()) {
				Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
				return true;
			}
			setProgressBarIndeterminateVisibility(true);
			LatLng centerPosition = mapFrag.getCenterPosition();
			ParseContract.Event.createEvent(ParseUser.getCurrentUser(), name,
					start, end, centerPosition.longitude,
					centerPosition.latitude, mapFrag.getRadius(), description,
					new SaveCallback() {
						@Override
						public void done(ParseException e) {
							// TODO Auto-generated method stub
							if (e != null)
								e.printStackTrace();
							setProgressBarIndeterminateVisibility(false);
							finish();
						}
					});
			break;
		default:

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_event, menu);
		return true;
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
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public static final int NEW_EVENT_MAP_PAGE = 0;
		public static final int NEW_EVENT_FORM_PAGE = 1;
		
		private LatLng userLocation;
		
		/**
		 * Constructor
		 * @param fm
		 * @param userLocation the current location of the user, to be passed to the NewEventMapFragment to initialize the map.
		 */
		public SectionsPagerAdapter(FragmentManager fm, LatLng userLocation) {
			super(fm);
			this.userLocation = userLocation;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment = null;

			switch (position) {
			case NEW_EVENT_MAP_PAGE:
				fragment = NewEventMapFragment.newInstance(userLocation);
				break;
			case NEW_EVENT_FORM_PAGE:
				fragment = new NewEventFormFragment();
				break;
			default:
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case NEW_EVENT_MAP_PAGE:
				return getText(R.string.location);
			case NEW_EVENT_FORM_PAGE:
				return getText(R.string.detail);
			default:
			}
			return null;
		}
	}

	/**
	 * A MapFragment that allows the user to choose the center and radius of an event.
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 *
	 */
	public static class NewEventMapFragment extends SupportMapFragment{
		
		private static final String USER_LOCATION_KEY = "user_location_key";
		private Marker centerMarker;// The map marker used to indicate the center of the event.
		private Marker radiusMarker;// The map marker used to indicate the radius of the event.
		
		/**
		 * Get a new instance
		 * @param latlng the current location of the user.
		 * @return 
		 */
		public static NewEventMapFragment newInstance(LatLng latlng)
		{
			 NewEventMapFragment frag = new NewEventMapFragment();

			    Bundle args = new Bundle();
			    args.putParcelable(USER_LOCATION_KEY, latlng);
			    frag.setArguments(args);
			    return frag;
		}

		/**
		 * Get the center position of the event.
		 * @return 
		 */
		public LatLng getCenterPosition()
		{
			return centerMarker.getPosition();
		}
		
		/**
		 * Get the radius of the event in meters.
		 * @return
		 */
		public int  getRadius()
		{
			return (int) Utility
			.distance(centerMarker.getPosition(),
					radiusMarker.getPosition());
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = super.onCreateView(inflater, container, savedInstanceState);
			
			//We initialize the map here.
			final GoogleMap mMap = getMap();
			
			// Change the display settings of the map.
			UiSettings settings = mMap.getUiSettings();
			settings.setCompassEnabled(true);

			LatLng centerPoint = getArguments().getParcelable(USER_LOCATION_KEY);
			LatLng radiusPoint = new LatLng(centerPoint.latitude + DEFAULT_EVENT_RADIUS,
					centerPoint.longitude);

			// Zoom to the location of the user
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint,
					LocationAwareActivity.DEFAULT_ZOOM_LEVEL));
			
			// Create the circle that represents the event area.
			CircleOptions circleOptions = new CircleOptions().center(centerPoint)
					.radius(Utility.distance(centerPoint, radiusPoint)) // In meters
					.strokeWidth((float) 4).strokeColor(LocationAwareActivity.EVENT_AREA_STROKE_COLOR)
					.fillColor(LocationAwareActivity.EVENT_AREA_FILL_COLOR);
			final Circle circle = mMap.addCircle(circleOptions);

			// Create the polyline that indicates the radius when the user is moving
			// the markers around
			PolylineOptions polyLineOptions = new PolylineOptions()
					.add(centerPoint).add(radiusPoint).width((float) 4)
					.color(Color.WHITE).visible(false);
			final Polyline line = mMap.addPolyline(polyLineOptions);

			// Create the marker that represents the center
			centerMarker = mMap.addMarker(new MarkerOptions()
					.position(centerPoint)
					.draggable(true)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

			// Create the marker that represents the radius
			radiusMarker = mMap.addMarker(new MarkerOptions()
					.draggable(true)
					.position(radiusPoint)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

			mMap.setOnMarkerDragListener(new OnMarkerDragListener() {

				@Override
				public void onMarkerDragStart(Marker arg0) {
					// Hide circle show marker.
					updateLine();
					circle.setVisible(false);
					line.setVisible(true);
				}

				@Override
				public void onMarkerDragEnd(Marker arg0) {
					// Update circle, show circle, hide marker
					circle.setCenter(centerMarker.getPosition());
					circle.setRadius(Utility.distance(centerMarker.getPosition(),
							radiusMarker.getPosition()));
					circle.setVisible(true);
					line.setVisible(false);
					mMap.animateCamera(CameraUpdateFactory.newLatLng(centerMarker
							.getPosition()));
				}

				@Override
				public void onMarkerDrag(Marker arg0) {
					updateLine();
				}

				/**
				 * Update the PolyLine that helps the user visualize the radius when
				 * moving markers. It changes the two end points to be the location
				 * of the two markers.
				 */
				private void updateLine() {
					LinkedList<LatLng> l = new LinkedList<LatLng>();
					l.add(centerMarker.getPosition());
					l.add(radiusMarker.getPosition());
					line.setPoints(l);
				}
			});
			
			return v;
		}
		
	}
	
	/**
	 * Fragment that allows the user to type in the information related to a new
	 * event.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	public static class NewEventFormFragment extends Fragment {

		private Calendar starting;
		private Calendar ending;
		private EditText nameEditText;
		private EditText descriptionEditText;

		public Date getStartingTime() {
			return starting.getTime();
		}

		public Date getEndingTime() {
			return ending.getTime();
		}

		public String getName() {
			return nameEditText.getText().toString();
		}

		public String getDescription() {
			return descriptionEditText.getText().toString();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_new_event_form,
					container, false);

			nameEditText = (EditText) rootView
					.findViewById(R.id.eventNameEditText);
			descriptionEditText = (EditText) rootView
					.findViewById(R.id.eventDescriptionEditText);

			Button startingTime = (Button) rootView
					.findViewById(R.id.startingTimeSpinner);
			Button endingTime = (Button) rootView
					.findViewById(R.id.endingTimeSpinner);

			starting = Calendar.getInstance();
			ending = Calendar.getInstance();
			ending.add(Calendar.HOUR, 1);

			startingTime.setText(Utility.dateToString(starting.getTime()));
			endingTime.setText(Utility.dateToString(ending.getTime()));
			startingTime.setOnClickListener(new DateTimeOnClickListener(
					starting));
			endingTime.setOnClickListener(new DateTimeOnClickListener(ending));
			return rootView;
		}
	}

	/**
	 * OnClick listener for the button that shows a date time picker dialog.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	public static class DateTimeOnClickListener implements OnClickListener {
		private Calendar c;

		/**
		 * 
		 * @param c
		 *            the Calendar instance to be updated
		 */
		public DateTimeOnClickListener(Calendar c) {
			this.c = c;
		}

		@Override
		public void onClick(final View v) {
			Context mContext = v.getContext();

			// Build a custom view that contains both a date picker and a time
			// picker
			Builder builder = new Builder(mContext);
			View dialogView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.date_time_picker, null);
			final DatePicker datePicker = (DatePicker) dialogView
					.findViewById(R.id.datePicker);
			final TimePicker timePicker = (TimePicker) dialogView
					.findViewById(R.id.timePicker);
			datePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
			datePicker.setCalendarViewShown(false);

			// Set the buttons.
			builder.setView(dialogView);
			builder.setCancelable(false);
			builder.setPositiveButton(mContext.getText(R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							c.set(Calendar.YEAR, datePicker.getYear());
							c.set(Calendar.MONTH, datePicker.getMonth());
							c.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
							c.set(Calendar.HOUR_OF_DAY,
									timePicker.getCurrentHour());
							c.set(Calendar.MINUTE,
									timePicker.getCurrentMinute());
							Button b = (Button) v;
							b.setText(Utility.dateToString(c.getTime()));
						}
					});
			builder.setNegativeButton(mContext.getText(R.string.cancel), null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}

	}
}
