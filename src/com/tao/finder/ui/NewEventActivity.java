package com.tao.finder.ui;

import java.util.Date;
import java.util.LinkedList;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMapOptions;
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

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.TextView;

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
	GoogleMap mMap;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

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

	@Override
	public void onConnected(Bundle arg0) {
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentByTag(Utility.getFragmentTag(R.id.pager, 0)))
				.getMap();
		UiSettings settings = mMap.getUiSettings();
		settings.setCompassEnabled(true);
		
		Location l = mLocationClient.getLastLocation();
		LatLng centerPoint = Utility.toLatLng(l);
		LatLng radiusPoint = new LatLng(l.getLatitude() + 0.001,
				l.getLongitude());
		
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 15));

		CircleOptions circleOptions = new CircleOptions().center(centerPoint)
				.radius(Utility.distance(centerPoint, radiusPoint)) // In meters
				.strokeWidth((float) 2).strokeColor(Color.GREEN);
		final Circle circle = mMap.addCircle(circleOptions);

		PolylineOptions polyLineOptions = new PolylineOptions()
				.add(centerPoint).add(radiusPoint).width((float) 2)
				.color(Color.BLUE).visible(false);
		final Polyline line = mMap.addPolyline(polyLineOptions);

		final Marker centerMarker = mMap.addMarker(new MarkerOptions()
				.position(centerPoint)
				.draggable(true)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

		final Marker radiusMarker = mMap.addMarker(new MarkerOptions()
				.draggable(true)
				.position(radiusPoint)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

		mMap.setOnMarkerDragListener(new OnMarkerDragListener() {

			@Override
			public void onMarkerDragStart(Marker arg0) {
				// TODO Auto-generated method stub
				updateLine();
				circle.setVisible(false);
				line.setVisible(true);
			}

			@Override
			public void onMarkerDragEnd(Marker arg0) {
				circle.setCenter(centerMarker.getPosition());
				circle.setRadius(Utility.distance(centerMarker.getPosition(),
						radiusMarker.getPosition()));
				circle.setVisible(true);
				line.setVisible(false);
				mMap.animateCamera(CameraUpdateFactory.newLatLng(centerMarker.getPosition()));
			}

			@Override
			public void onMarkerDrag(Marker arg0) {
				updateLine();
			}
			
			private void updateLine()
			{
				LinkedList<LatLng> l = new LinkedList<LatLng>();
				l.add(centerMarker.getPosition());
				l.add(radiusMarker.getPosition());
				line.setPoints(l);
			}
		});
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

			if (position == 0) {
				fragment = new SupportMapFragment();
			} else {
				fragment = new DummySectionFragment();
			}

			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);

			if (position == 0
					&& !(mLocationClient.isConnected() || mLocationClient
							.isConnecting()))
				mLocationClient.connect();
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return "Hoho";
			case 1:
				return "Haha";
			case 2:
				return "Hehe";
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
			View rootView = inflater.inflate(R.layout.fragment_new_event_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}
}
