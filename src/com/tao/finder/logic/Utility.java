package com.tao.finder.logic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

/**
 * Common methods.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class Utility {
	
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH:mm",Locale.getDefault());	
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
	 * @param l the location instance.
	 * @return the LatLng instance.
	 */
	public static LatLng toLatLng(Location l)
	{
		return new LatLng(l.getLatitude(), l.getLongitude());
	}
	
	/**
	 * Converts a ParseGeoPoint instance to a LatLng instance.
	 * @param p the ParseGeoPoint instance
	 * @return the LatLng instance
	 */
	public static LatLng toLatLng(ParseGeoPoint p)
	{
		return new LatLng(p.getLatitude(),p.getLongitude());
	}
	

	/**
	 * Get the distance in meter between two points.
	 * Source:http://stackoverflow.com/questions/8832071/how-can-i-get-the-distance-between-two-point-by-latlng
	 * @param p1
	 * @param p2
	 * @return distance in meter
	 */
	public static float distance(LatLng p1, LatLng p2) 
	{
	    double earthRadius = 3958.75;
	    double latDiff = Math.toRadians(p2.latitude-p1.latitude);
	    double lngDiff = Math.toRadians(p2.longitude-p1.longitude);
	    double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
	    Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) *
	    Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double distance = earthRadius * c;

	    int meterConversion = 1609;

	    return (float)distance * meterConversion;
	}
	
	/**
	 * Return the string representation of a Date instance.
	 * @param the date
	 * @return string as in MM/dd/YYYY HH:mm
	 */
	public static String dateToString(Date d)
	{
		return DATE_FORMAT.format(d);
	}
}
