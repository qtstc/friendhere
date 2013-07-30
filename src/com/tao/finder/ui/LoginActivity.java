package com.tao.finder.ui;

import java.util.Arrays;

import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFacebookUtils.Permissions;
import com.parse.ParseUser;
import com.tao.finder.R;
import com.tao.finder.R.layout;
import com.tao.finder.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);

		Button loginButton = (Button) findViewById(R.id.auth_button);
		if (ParseUser.getCurrentUser() == null) {
			loginButton.setText(getString(R.string.login));
			loginButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					setProgressBarIndeterminateVisibility(true);
					ParseFacebookUtils.logIn(Arrays.asList(
							Permissions.User.EMAIL, Permissions.User.BIRTHDAY),
							LoginActivity.this, new LogInCallback() {
								@Override
								public void done(ParseUser user,
										ParseException err) {
									if (err != null)
										Log.e("Exce", " " + err.toString());
									if (user == null) {
										Log.e("MyApp",
												"Uh oh. The user cancelled the Facebook login.");
										return;
									} else if (user.isNew()) {
										Log.e("MyApp",
												"User signed up and logged in through Facebook!");
									} else {
										Log.e("MyApp",
												"User logged in through Facebook!");
									}
									setProgressBarIndeterminateVisibility(false);
									Request.executeMeRequestAsync(
											ParseFacebookUtils.getSession(),
											new GraphUserCallback() {

												@Override
												public void onCompleted(
														GraphUser user,
														Response response) {
													if (user != null) {

														Toast.makeText(
																LoginActivity.this,
																user.getName()
																		+ " "
																		+ user.getBirthday(),
																Toast.LENGTH_LONG)
																.show();
													}
												}
											});
								}
							});
				}
			});
		} else {
			loginButton.setText(getString(R.string.logout));
			loginButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ParseFacebookUtils.getSession()
							.closeAndClearTokenInformation();
					ParseUser.logOut();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}


}
