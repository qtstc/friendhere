package com.tao.finder.ui;

import java.util.Locale;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.tao.finder.R;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.Utility;
import com.tao.finder.ui.NewEventActivity.NewEventFormFragment;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PersonActivity extends FragmentActivity implements
		ActionBar.TabListener {

	private ParseUser user;
	
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
		setContentView(R.layout.activity_person);

		ParseContract.User.getPersonById(getIntent().getStringExtra(SearchListFragment.OBJECT_ID), new GetCallback<ParseUser>() {
			
			@Override
			public void done(ParseUser object, ParseException e) {
				user = object;
				initializeTabs(user);
			}
		});
	}

	private void initializeTabs(ParseUser user)
	{
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(),user);

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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.person, menu);
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

		ParseUser user;
		
		public SectionsPagerAdapter(FragmentManager fm, ParseUser user) {
			super(fm);
			this.user = user;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment = null;

			switch (position) {
			case 0:
				fragment = TrackingMapFragment.newInstance(user) ;
//				if (!(mLocationClient.isConnected() || mLocationClient
//						.isConnecting()))
//					mLocationClient.connect();
				
//				SupportMapFragment mapFrag = (SupportMapFragment)fragment;
//				GoogleMap mMap = mapFrag.getMap();
//				if(mMap != null)
//				{
//					UiSettings settings = mMap.getUiSettings();
//				}
//				else
//					Log.e("It's null!","what?");
				break;
			case 1:
				fragment = PersonInfoFragment.newInstance(user);
				break;
			default:
			}

			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.location).toUpperCase(l);
			case 1:
				return getString(R.string.contact).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A fragment that contains the information of the 
	 * person.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 *
	 */
	public static class PersonInfoFragment extends Fragment {

		private static final String EMAIL_KEY = "email_key";
		private static final String PHONE_KEY = "phone_key";
		
		public static PersonInfoFragment newInstance(ParseUser user)
		{
			PersonInfoFragment frag = new PersonInfoFragment();
			Bundle args = new Bundle();
		    args.putString(EMAIL_KEY, user.getEmail());
		    args.putString(PHONE_KEY, user.getString(ParseContract.User.PHONE));
		    frag.setArguments(args);
		    return frag;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_person_info,
					container, false);
			
			Bundle args = getArguments();
			TextView email = (TextView)rootView.findViewById(R.id.person_info_email_textview);
			TextView phone = (TextView)rootView.findViewById(R.id.person_info_phone_textview);
			email.setText(args.getString(EMAIL_KEY));
			phone.setText(args.getString(PHONE_KEY));
			return rootView;
		}
	}
	
	public static class TrackingMapFragment extends SupportMapFragment{
		
		private static final String USER_OBJECT_ID_KEY = "user_object_id_key";
		
		public static TrackingMapFragment newInstance(ParseUser user)
		{
			TrackingMapFragment frag = new TrackingMapFragment();
			Bundle args = new Bundle();
		    args.putString(USER_OBJECT_ID_KEY, user.getObjectId());
		    frag.setArguments(args);
		    return frag;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = super.onCreateView(inflater, container, savedInstanceState);
			
			GoogleMap mMap = getMap();
			mMap.setMyLocationEnabled(true);
			return v;
		}
	}
}
