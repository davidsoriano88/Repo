package com.wikout;

import java.util.ArrayList;
import java.util.List;

import model.FontUtils;
import utils.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;

public class LoginActivity extends Activity implements OnClickListener,
		PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener,
		PlusClient.OnAccessRevokedListener {

	private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;

	private static final int REQUEST_CODE_SIGN_IN = 1;
	private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

	private PlusClient mPlusClient;
	private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
	Context context = this;
	Util util;
	private TextView textInstructionsOrLink;
	private Button btnFacebook, btnSinLogin, btnGoogleLogout;

	private ConnectionResult mConnectionResult;
	private SignInButton btnGoogle;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	public double latitudeSplash = 0, longitudeSplash = 0;

	// Profile pic image size in pixels
	private static final int PROFILE_PIC_SIZE = 400;

	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;
	
	
	private SharedPreferences prefers;

	List<String> permissions = new ArrayList<String>();


	// Esta es la clave: 2jmj7l5rSw0yVb/vlWAYkK/YBwk=

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		// Set portrait orientation

		mPlusClient = new PlusClient.Builder(this, this, this).setScopes(
				Scopes.PROFILE).build();

		btnFacebook = (Button) findViewById(R.id.btnLoginFacebook);
		btnGoogle = (SignInButton) findViewById(R.id.btnLoginGoogle);
		btnGoogleLogout = (Button) findViewById(R.id.sign_out_button);
		textInstructionsOrLink = (TextView) findViewById(R.id.instructionsOrLink);
		// btnGooglePlus = (Button) findViewById(R.id.btnLoginGoogle);
		btnSinLogin = (Button) findViewById(R.id.btnSinLogin);
		Bundle splash = getIntent().getExtras();
		latitudeSplash = splash.getDouble("latitudSplash");
		longitudeSplash = splash.getDouble("longitudSplash");
		FontUtils.setRobotoFont(context, ((Activity) context).getWindow()
				.getDecorView());

		// Anadir ActionClics

		btnSinLogin.setOnClickListener(this);
		btnFacebook.setOnClickListener(this);
		btnGoogle.setOnClickListener(this);
		btnGoogleLogout.setOnClickListener(this);

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));
			}
		}
		permissions.add("email");
		updateView();
	}

	private static Session openActiveSession(Activity activity,
			boolean allowLoginUI, Session.StatusCallback callback,
			List<String> permissions) {
		Session.OpenRequest openRequest = new Session.OpenRequest(activity)
				.setPermissions(permissions).setCallback(callback);
		Session session = new Session.Builder(activity).build();
		if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState())
				|| allowLoginUI) {
			Session.setActiveSession(session);
			session.openForRead(openRequest);
			return session;
		}
		return null;
	}

	@Override
	public void onStart() {
		super.onStart();
		/* Facebook */Session.getActiveSession().addCallback(statusCallback);
		mPlusClient.connect();
	}

	@Override
	public void onStop() {
		mPlusClient.disconnect();
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SIGN_IN
				|| requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
			if (resultCode == RESULT_OK && !mPlusClient.isConnected()
					&& !mPlusClient.isConnecting()) {
				// This time, connect should succeed.
				mPlusClient.connect();
			}
		}
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void updateView() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			textInstructionsOrLink.setText(URL_PREFIX_FRIENDS
					+ session.getAccessToken());
			btnFacebook.setText(R.string.logout);
			btnFacebook.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogout();
				}
			});
		} else {
			textInstructionsOrLink.setText(R.string.instructions);
			btnFacebook.setText(R.string.login);
			btnFacebook.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogin();
				}
			});
		}
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(statusCallback));
			// start Facebook session
			openActiveSession(this, true, new Session.StatusCallback() {
				@Override
				public void call(Session session, SessionState state,
						Exception exception) {
					if (session.isOpened()) {
						// make request to the /me API
						Log.e("sessionopened", "true");
						Request.newMeRequest(session,
								new Request.GraphUserCallback() {
									@Override
									public void onCompleted(GraphUser user,
											Response response) {
										if (user != null) {
											String firstName = user
													.getFirstName();
											String lastName = user
													.getLastName();
											String id = user.getId();
											String email = user.getProperty(
													"email").toString();

											Log.e("facebookid", id);
											Log.e("firstName", firstName);
											Log.e("lastName", lastName);
											Log.e("email", email);
											textInstructionsOrLink.setText(id
													+ ", " + email + ", "
													+ firstName + ", "
													+ lastName);
											System.out
													.println(textInstructionsOrLink
															.getText()
															.toString());

										}
									}
								}).executeAsync();
					}
				}
			}, permissions);
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}

	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateView();
		}
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	public void onAccessRevoked(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		
		// Get user's information
				getProfileInformation();

				// Update the UI after signin
		updateButtons(true /* isSignedIn */);
	}
	private void getProfileInformation() {
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(mGoogleApiClient);
				String personName = currentPerson.getDisplayName();
				String personPhotoUrl = currentPerson.getImage().getUrl();
				String personGooglePlusProfile = currentPerson.getUrl();
				String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

				

				textInstructionsOrLink.setText("Name: " + personName + ", plusProfile: "
						+ personGooglePlusProfile + ", email: " + email
						+ ", Image: " + personPhotoUrl);

				// by default the profile url gives 50x50 px image only
				// we can replace the value with whatever dimension we want by
				// replacing sz=X
				personPhotoUrl = personPhotoUrl.substring(0,
						personPhotoUrl.length() - 2)
						+ PROFILE_PIC_SIZE;

				//new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

			} else {
				Toast.makeText(getApplicationContext(),
						"Person information is null", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnected() {
		textInstructionsOrLink.setText(R.string.loading_status);
		mPlusClient.connect();
		updateButtons(false /* isSignedIn */);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		mConnectionResult = result;
		textInstructionsOrLink.setText("Reinténtelo de nuevo");
		updateButtons(false /* isSignedIn */);
	}

	private void updateButtons(boolean isSignedIn) {
		if (isSignedIn) {
			btnGoogle.setVisibility(View.GONE);
			btnGoogleLogout.setVisibility(0);
			btnGoogleLogout.setEnabled(true);
		} else {
			if (mConnectionResult == null) {
				// Disable the sign-in button until onConnectionFailed is called
				// with result.
				btnGoogle.setVisibility(View.GONE);
				btnGoogleLogout.setVisibility(0);
				textInstructionsOrLink
						.setText(getString(R.string.loading_status));
			} else {
				// Enable the sign-in button since a connection result is
				// available.
				btnGoogle.setVisibility(0);
				btnGoogleLogout.setVisibility(View.GONE);
				textInstructionsOrLink
						.setText(getString(R.string.signed_out_status));
			}

			btnGoogleLogout.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnLoginGoogle:
			mPlusClient.disconnect();
			mPlusClient = new PlusClient.Builder(LoginActivity.this,
					LoginActivity.this, LoginActivity.this).setScopes(
					Scopes.PROFILE).build();
			mPlusClient.connect();
			int available = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(this);
			if (available != ConnectionResult.SUCCESS) {
				showDialog(DIALOG_GET_GOOGLE_PLAY_SERVICES);
				return;
			}

			try {
				textInstructionsOrLink
						.setText(getString(R.string.signing_in_status));
				mConnectionResult.startResolutionForResult(this,
						REQUEST_CODE_SIGN_IN);
			} catch (IntentSender.SendIntentException e) {
				// Fetch a new result to start.
				mPlusClient.connect();
			}
			break;
		case R.id.sign_out_button:
			if (mPlusClient.isConnected()) {
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				mPlusClient.connect();
			}
			break;
		// case R.id.btnLoginFacebook:break;
		case R.id.btnSinLogin:
			if (isNetworkAvailable() == true) {
				Intent mainIntent = new Intent().setClass(LoginActivity.this,
						Map.class);

				mainIntent.putExtra("latitudSplash", latitudeSplash); // location.getLatitude());
				mainIntent.putExtra("longitudSplash", longitudeSplash);// location.getLongitude());
				// System.out.println("latitudSplash "+
				// location.getLatitude() +
				// "\n" + "longitudSplash "+ location.getLongitude());
				startActivity(mainIntent);
				finish();
			} else {
				util.showInfoDialog(context, "Lo sentimos",
						"Es necesaria conexión a internet");
			}
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id != DIALOG_GET_GOOGLE_PLAY_SERVICES) {
			return super.onCreateDialog(id);
		}

		int available = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (available == ConnectionResult.SUCCESS) {
			return null;
		}
		if (GooglePlayServicesUtil.isUserRecoverableError(available)) {
			return GooglePlayServicesUtil.getErrorDialog(available, this,
					REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
		}
		return new AlertDialog.Builder(this)
				.setMessage("No lo tienes instalado").setCancelable(true)
				.create();
	}
}
