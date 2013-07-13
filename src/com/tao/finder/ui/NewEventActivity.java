package com.tao.finder.ui;

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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.Spinner;
import android.widget.TimePicker;

public class NewEventActivity extends LocationAwareActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	private Marker centerMarker;// The map marker used to indicate the center of
								// the event.
	private Marker radiusMarker;// The map marker used to indicate the radius of
								// the event.

	public final static double DEFAULT_EVENT_RADIUS = 0.001;// The default
															// radius of the
															// event as
															// expressed in
															// longitude/latitude
	public final static int DEFAULT_ZOOM_LEVEL = 15;// The default zoom level of
													// the map.

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_new_event);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
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
		// Get the map shown in the fragment.
		final GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentByTag(Utility.getFragmentTag(R.id.pager, 0)))
				.getMap();
		// Change the display settings of the map.
		UiSettings settings = mMap.getUiSettings();
		settings.setCompassEnabled(true);

		// Get the current location of the user,
		// use it as the default event location,
		// and give the location a default radius.
		Location l = mLocationClient.getLastLocation();
		LatLng centerPoint = Utility.toLatLng(l);
		LatLng radiusPoint = new LatLng(l.getLatitude() + DEFAULT_EVENT_RADIUS,
				l.getLongitude());

		// Zoom to the location of the user
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint,
				DEFAULT_ZOOM_LEVEL));

		// Create the circle that represents the event area.
		CircleOptions circleOptions = new CircleOptions().center(centerPoint)
				.radius(Utility.distance(centerPoint, radiusPoint)) // In meters
				.strokeWidth((float) 4).strokeColor(Color.WHITE);
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
			setProgressBarIndeterminateVisibility(true);
			ParseContract.Event.createEvent(ParseUser.getCurrentUser(),
					"TestEvent2", new Date(1000), new Date(10000), 12, 13, 10,
					"Just for testing", new SaveCallback() {
						@Override
						public void done(ParseException e) {
							// TODO Auto-generated method stub
							setProgressBarIndeterminateVisibility(false);
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

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			// Fragment fragment = new DummySectionFragment();

			Fragment fragment = null;

			switch (position) {
			case 0:
				fragment = new SupportMapFragment();
				if (!(mLocationClient.isConnected() || mLocationClient
						.isConnecting()))
					mLocationClient.connect();
				break;
			case 1:
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
			// Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return "Hoho";
			case 1:
				return "Haha";
			default:
			}
			return null;
		}
	}

	public static class NewEventFormFragment extends Fragment {

		public NewEventFormFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_new_event_form,
					container, false);
			Button startingTime = (Button) rootView.findViewById(R.id.startingTimeSpinner);
			Button endingTime = (Button) rootView.findViewById(R.id.endingTimeSpinner);
			startingTime.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
						 Dialog dialog = new Dialog(getActivity());

						 dialog.setContentView(R.layout.date_time_picker);   
						 DatePicker datePicker = (DatePicker)dialog.findViewById(R.id.datePicker);
						 TimePicker timePicker = (TimePicker)dialog.findViewById(R.id.timePicker);
						 dialog.show();
						 dialog.setOnDismissListener(new OnDismissListener() {
							
							@Override
							public void onDismiss(DialogInterface dialog) {
								//startingTime.
							}
						});
				}
			});
			return rootView;
		}
	}
}
