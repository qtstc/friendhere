package com.tao.finder.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;
import com.tao.finder.ui.SearchListFragment.OnSearchListener;

/**
 * A custom SupportMapFragment class.
 * It contains a OnCreateListener interface which is called
 * right after the map in the SupportMapFragment is created.
 * 
 * This class should be used to replace the SupportMapFragment
 * when SupportMapFragment is added to an activity dynamically.
 * In this case, calling getMap() directly from a SupportMapFragment
 * is likely to return null because the GoogleMap is not yet created.
 * Instead, accessing the GoogleMap in the callback will be safe.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 *
 */
public class CustomMapFragment extends SupportMapFragment {

	private OnCreatedListener onCreatedListener;

	public static final int DEFAULT_ZOOM_LEVEL = 15;// The default zoom level of
	// the map.
	public static final int EVENT_AREA_STROKE_COLOR = Color.GRAY;
	public static final int EVENT_AREA_FILL_COLOR = Color.argb(100, 100, 100,100);

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Require the activity using this class to implement the
		// OnCreatedListener interface.
		try {
			onCreatedListener = (OnCreatedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onCreatedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		onCreatedListener.onMapCreated();
		return v;
	}

	/**
	 * To be implemented by activities that uses this fragment.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 *
	 */
	public interface OnCreatedListener {
		/**
		 * Put any map initialization code here.
		 * Calling getMap() on the fragment from this method will not return null.
		 */
		public void onMapCreated();
	}
}
