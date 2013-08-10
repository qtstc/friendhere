package com.tao.friendhere.logic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.tao.friendhere.R;
import com.tao.friendhere.ui.SettingsActivity;

/**
 * Common methods.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class Utility {

	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"MM-dd-yyyy HH:mm", Locale.getDefault());

	public static String truncString(String s, int length) {
		if (s.length() < length)
			return s;
		s = s.substring(0, length - 3) + "...";
		return s;
	}

	/**
	 * Get the tag used to identify different Fragments. This is necessary
	 * because identifying fragment by Id will give NullPointerException.
	 * 
	 * @param pos
	 *            the position of the tab.
	 * @return the Tag of the fragment.
	 */
	public static String getFragmentTag(int id, int pos) {
		return "android:switcher:" + id + ":" + pos;
	}

	/**
	 * Converts a location instance to a Latlng instance.
	 * 
	 * @param l
	 *            the location instance.
	 * @return the LatLng instance.
	 */
	public static LatLng toLatLng(Location l) {
		return new LatLng(l.getLatitude(), l.getLongitude());
	}

	/**
	 * Converts a ParseGeoPoint instance to a LatLng instance.
	 * 
	 * @param p
	 *            the ParseGeoPoint instance
	 * @return the LatLng instance
	 */
	public static LatLng toLatLng(ParseGeoPoint p) {
		return new LatLng(p.getLatitude(), p.getLongitude());
	}

	/**
	 * Get the distance in meter between two points.
	 * Source:http://stackoverflow.
	 * com/questions/8832071/how-can-i-get-the-distance
	 * -between-two-point-by-latlng
	 * 
	 * @param p1
	 * @param p2
	 * @return distance in meter
	 */
	public static float distance(LatLng p1, LatLng p2) {
		double earthRadius = 3958.75;
		double latDiff = Math.toRadians(p2.latitude - p1.latitude);
		double lngDiff = Math.toRadians(p2.longitude - p1.longitude);
		double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
				+ Math.cos(Math.toRadians(p1.latitude))
				* Math.cos(Math.toRadians(p2.latitude)) * Math.sin(lngDiff / 2)
				* Math.sin(lngDiff / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = earthRadius * c;

		int meterConversion = 1609;

		return (float) distance * meterConversion;
	}

	/**
	 * Return the string representation of a Date instance.
	 * 
	 * @param the
	 *            date
	 * @return string as in MM/dd/YYYY HH:mm
	 */
	public static String dateToString(Date d) {
		return DATE_FORMAT.format(d);
	}
	
	/**
	 * Check whether the user is logged in. If not, a dialog requesting the user
	 * to log in will be shown.
	 * 
	 * @return true if the user is logged in, false otherwise.
	 */
	public static boolean isLoggedin(final Context c) {
		if (ParseUser.getCurrentUser() != null)
			return true;

		AlertDialog.Builder builder = new AlertDialog.Builder(c);

		builder.setMessage(R.string.request_login_dialog_message);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(c,
						SettingsActivity.class);
				c.startActivity(i);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
		return false;
	}
}
