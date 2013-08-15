package com.tao.friendhere.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.util.Arrays;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Request.GraphUserCallback;
import com.facebook.model.GraphUser;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ParseFacebookUtils.Permissions;
import com.tao.friendhere.R;
import com.tao.friendhere.logic.ParseContract;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * Settings are always presented as a single list.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class SettingsActivity extends PreferenceActivity {

	public static final String TAG = "SettingsActivity";
	
	//A flag that indicates the login is on going,
	//allow us to ignore additional requests sent by the user.
	public boolean isWorking;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		isWorking = false;
		setProgressBarIndeterminateVisibility(true);
		// First fetch the ParseUser instance.
		if (ParseUser.getCurrentUser() != null)
		{
			isWorking = true;
			ParseUser.getCurrentUser().fetchInBackground(
					new GetCallback<ParseUser>() {

						@Override
						public void done(ParseUser object, ParseException e) {
							setupSimplePreferencesScreen();
							setProgressBarIndeterminateVisibility(false);
							isWorking = false;
						}
					});
		}
		else {
			setupSimplePreferencesScreen();
			setProgressBarIndeterminateVisibility(false);
		}
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
						if(isWorking)
							return true;
						
						if (ParseUser.getCurrentUser() == null) {
							setProgressBarIndeterminateVisibility(true);
							ParseFacebookUtils.logIn(
									Arrays.asList(Permissions.User.EMAIL),
									SettingsActivity.this, new LogInCallback() {
										@Override
										public void done(final ParseUser user,
												ParseException err) {
											if (err != null)
											{
												//Notify the user when error occurs
												Toast.makeText(SettingsActivity.this, R.string.connection_error_toast_message, Toast.LENGTH_SHORT).show();
												setProgressBarIndeterminateVisibility(false);
												return;
											}
											if (user == null) {
												//User did not grant access to Facebook. Logging in is canceled.
												setProgressBarIndeterminateVisibility(false);
												return;
											} else {
												if (user.isNew()) {
													Log.d(TAG,
															"User signed up and logged in through Facebook!");
												} else {
													Log.d(TAG,
															"User logged in through Facebook!");
												}

												Request.executeMeRequestAsync(
														ParseFacebookUtils
																.getSession(),
														new GraphUserCallback() {

															@Override
															public void onCompleted(
																	GraphUser gUser,
																	Response response) {
																if(gUser == null)
																{
																	//If loading user data from fb failed,cancel login and remove the database entry
																	ParseFacebookUtils.getSession()
																	.closeAndClearTokenInformation();
																	ParseUser.logOut();
																	user.deleteEventually();
																	Toast.makeText(SettingsActivity.this, R.string.connection_error_toast_message, Toast.LENGTH_SHORT).show();
																}
																if (gUser != null) {
																	user.put(
																			ParseContract.User.NAME,
																			gUser.getName());
																	// Key as
																	// listed in
																	// https://developers.facebook.com/docs/reference/api/user/
																	Object email = gUser
																			.getProperty("email");
																	//If the user has a valid email, add it to the user profile
																	if(email != null)
																		user.setEmail((String)email);
																	user.saveInBackground(new SaveCallback() {
																		@Override
																		public void done(
																				ParseException e) {
																			if(e != null)
																				user.saveEventually();
																			setProgressBarIndeterminateVisibility(false);
																			updateSettingsGUI();
																		}
																	});
																}
															}
														});

											}
										}
									});
						} else {
							ParseFacebookUtils.getSession()
									.closeAndClearTokenInformation();
							ParseUser.logOut();
							updateSettingsGUI();
						}
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

		if(displayName == null)
			Log.e(TAG,"Hell, it is null!");
		// First enable/disable form
		displayName.setEnabled(loggedIn);
		email.setEnabled(loggedIn);
		phone.setEnabled(loggedIn);

		if (loggedIn) {
			// Update the GUI.
			if(user.containsKey(ParseContract.User.NAME))
				displayName.setSummary(user.getString(ParseContract.User.NAME));
			if(user.getEmail() != null)
				email.setSummary(user.getEmail());
			if(user.containsKey(ParseContract.User.PHONE))
				phone.setSummary(user.getString(ParseContract.User.PHONE));
			login.setTitle(getString(R.string.logout));
		} else {
			login.setTitle(getString(R.string.login));
			displayName.setSummary("");
			email.setSummary("");
			phone.setSummary("");
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}
}
