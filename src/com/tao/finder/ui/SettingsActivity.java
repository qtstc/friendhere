package com.tao.finder.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.tao.finder.R;
import com.tao.finder.R.string;
import com.tao.finder.R.xml;
import com.tao.finder.logic.ParseContract;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * Settings are always presented as a single list.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		//First fetch the ParseUser instance.
		if(ParseUser.getCurrentUser() != null)
			ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser object, ParseException e) {
					setupSimplePreferencesScreen();
				}
			});
		else
			setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		addPreferencesFromResource(R.xml.pref_account);

		// Add 'profile' preferences.
		addPreferencesFromResource(R.xml.pref_profile);

		findPreferenceById(R.string.pref_key_login)
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						updateSettingsGUI();
						return true;
					}
				});

		findPreferenceById(R.string.pref_key_display_name)
				.setOnPreferenceChangeListener(
						new OnParseStringPreferenceChangeListener() {

							@Override
							public void updateParseUser(String newValue) {
								ParseUser.getCurrentUser().put(
										ParseContract.User.NAME, newValue);
							}
						});
		findPreferenceById(R.string.pref_key_email)
				.setOnPreferenceChangeListener(
						new OnParseStringPreferenceChangeListener() {

							@Override
							public void updateParseUser(String newValue) {
								ParseUser.getCurrentUser().setEmail(newValue);
							}
						});
		findPreferenceById(R.string.pref_key_phone)
				.setOnPreferenceChangeListener(
						new OnParseStringPreferenceChangeListener() {

							@Override
							public void updateParseUser(String newValue) {
								// TODO Auto-generated method stub
								ParseUser.getCurrentUser().put(
										ParseContract.User.PHONE, newValue);
							}
						});
		updateSettingsGUI();
	}

	/**
	 * Update the settings preference GUI. It enables/disables the preference
	 * items depending on the login state of the user. It also updates the GUI
	 * with the user information.
	 * 
	 * Only calls this method after fetching the current user information from
	 * the server.
	 */
	private void updateSettingsGUI() {
		ParseUser user = ParseUser.getCurrentUser();
		boolean loggedIn = (user != null);

		// Get the different preferences.
		Preference displayName = findPreferenceById(R.string.pref_key_display_name);
		Preference email = findPreferenceById(R.string.pref_key_email);
		Preference phone = findPreferenceById(R.string.pref_key_phone);
		Preference login = findPreferenceById(R.string.pref_key_login);

		// First enable/disable form
		displayName.setEnabled(loggedIn);
		email.setEnabled(loggedIn);
		phone.setEnabled(loggedIn);

		if (loggedIn) {
			//Update the GUI.
			displayName.setSummary(user.getString(ParseContract.User.NAME));
			email.setSummary(user.getEmail());
			phone.setSummary(user.getString(ParseContract.User.PHONE));
			//Save the values to SharedPreference
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editor.putString(displayName.getKey(), user.getString(ParseContract.User.NAME));
			editor.putString(email.getKey(), user.getEmail());
			editor.putString(phone.getKey(), user.getString(ParseContract.User.PHONE));
			editor.commit();
			
			login.setTitle(getString(R.string.logout));
		} else {
			login.setTitle(getString(R.string.login));
		}
	}

	/**
	 * Convenience method for finding a preference by the string id of its key.
	 * 
	 * @param keyStringId
	 *            the id of the string which is used as the key of the
	 *            preference
	 * @return the preference
	 */
	@SuppressWarnings("deprecation")
	private Preference findPreferenceById(int keyStringId) {
		return findPreference(getString(keyStringId));
	}
	

	/**
	 * An OnPreferenceChangeListener which has the mechanism of saving the
	 * changed information to Parse.com.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	public static abstract class OnParseStringPreferenceChangeListener
			implements Preference.OnPreferenceChangeListener {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String stringValue = newValue.toString();
			preference.setSummary(stringValue);
			updateParseUser(stringValue);
			ParseUser.getCurrentUser().saveEventually();
			return true;
		}

		/**
		 * Update a specific key-value pair in the current Parse user. Do not
		 * perform save operation in this method.
		 * 
		 * @param newValue
		 *            the new value to be saved.
		 */
		public abstract void updateParseUser(String newValue);
	}
	
}
