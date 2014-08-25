package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.ObjectCallback;
import io.backbeam.Query;

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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;

public class LoginActivity extends Activity implements OnClickListener,
		PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener,
		PlusClient.OnAccessRevokedListener, ConnectionCallbacks,
		OnConnectionFailedListener {

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
	private String email, nombre;
	private int lastUserId = 0;
	// Profile pic image size in pixels

	// Google client to interact with Google API
	// private GoogleApiClient mGoogleApiClient;
	List<String> permissions = new ArrayList<String>();
	
	// Esta es la clave: 2jmj7l5rSw0yVb/vlWAYkK/YBwk=

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		// Set portrait orientation

		mPlusClient = new PlusClient.Builder(this, this, this).setScopes(
				Scopes.PROFILE, Scopes.PLUS_ME, Scopes.PLUS_LOGIN).build();

		btnFacebook = (Button) findViewById(R.id.btnLoginFacebook);
		btnGoogle = (SignInButton) findViewById(R.id.btnLoginGoogle);
		btnGoogleLogout = (Button) findViewById(R.id.sign_out_button);
		textInstructionsOrLink = (TextView) findViewById(R.id.instructionsOrLink);
		textInstructionsOrLink.setVisibility(View.GONE);
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

		System.out.println("Leloo" + "valor de login y procedencia y place: "
				+ Util.getPreferenceBoolean(context, "login") + ", "
				+ splash.getInt("procedencia")
				+ Util.getPreferenceInt(context, "place"));
		if (Util.getPreferenceBoolean(context, "login") == false
				&& splash.getInt("procedencia") > 1) {
			btnSinLogin.setVisibility(View.GONE);
		}
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (Util.getPreferenceBoolean(context, "login") == true) {
			if (session == null) {
				if (savedInstanceState != null) {

					session = Session.restoreSession(this, null,
							statusCallback, savedInstanceState);
				}

				if (session == null) {
					session = new Session(this);
				}
				Session.setActiveSession(session);
				if (session.getState()
						.equals(SessionState.CREATED_TOKEN_LOADED)) {
					session.openForRead(new Session.OpenRequest(this)
							.setCallback(statusCallback));
				}
			}
		} else {
			if (session != null) {

				if (!session.isClosed()) {
					session.closeAndClearTokenInformation();
					// clear your preferences if saved
				}
			} else {

				session = new Session(context);
				Session.setActiveSession(session);

				session.closeAndClearTokenInformation();
				// clear your preferences if saved

			}
		}

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

				// GOOGLE CONNECTED
				// editor.putInt("place", 1);
				Util.setPreferenceBoolean(context, "login", false);

				// util.setuserid(prefs, );
				// falta añadir google login util.setSPstring(

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
		permissions.add("email");
		permissions.add("public_profile");
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
											email = user.getProperty("email").toString();
											nombre = lastName + ", "
													+ firstName;
											Log.e("facebookid", id);
											Log.e("firstName", firstName);
											Log.e("lastName", lastName);
											Log.e("email", email);
											System.out.println("leloo valor place en fb login"
													+ Util.getPreferenceInt(
															context, "place"));
											textInstructionsOrLink.setText(id
													+ ", " + email + ", "
													+ firstName + ", "
													+ lastName);
											System.out
													.println(textInstructionsOrLink
															.getText()
															.toString());
											// FACEBOOK LOGIN
											Util.setPreferenceBoolean(context,
													"login", true);
											Util.setPreferenceString(context,
													"email", email);
											// BUSCAR USER
											isRegistered(email);
											// buscarusuario(email);
											// falta añadir face login
											// util.setSPstring(

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

	}

	private void buscarusuario(String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		String currentPersonName = mPlusClient.getCurrentPerson() != null ? mPlusClient
				.getCurrentPerson().getDisplayName()
				: getString(R.string.unknown_person);
		textInstructionsOrLink.setText(getString(R.string.signed_in_status,
				currentPersonName));
		updateButtons(true /* isSignedIn */);
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

				Util.setPreferenceBoolean(context, "login", false);
				// util.setuserid(prefs, 0);
				// util.setmail(prefs,"");
			}
			break;
		// case R.id.btnLoginFacebook:break;
		case R.id.btnSinLogin:
			if (isNetworkAvailable() == true) {
				// SharedParam de usuario sin login
				Util.setPreferenceBoolean(context, "login", false);
				// util.setuserid(prefs, 0);

				dondeVa(Util.getPreferenceInt(context, "place"));
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

	@Override
	public void onConnectionSuspended(int arg0) {

	}

	public void isLogged() {
		if (Util.getPreferenceBoolean(context, "login") == false) {
			if (Util.getPreferenceString(context, "email") == ""
					|| Util.getPreferenceInt(context, "userid") == 0) {
				login();
			} else {
				Util.setPreferenceBoolean(context, "login", true);

				dondeVa(Util.getPreferenceInt(context, "place"));
			}
		} else {
			dondeVa(Util.getPreferenceInt(context, "place"));
		}
	}

	public void login() {
		Util.setPreferenceBoolean(context, "login", true);
		if (Util.getPreferenceString(context, "email") == "") {
			Util.setPreferenceString(context, "email", email);

		}
	}

	public void registrarBB(final String mail, final String nombre) {

		// Backbeam ProjectData
		Backbeam.setProject("pruebaapp");
		Backbeam.setEnvironment("dev");
		Backbeam.setContext(context);
		// Create the API keys in the control panel of your project
		Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
		Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");

		Query qUserId = new Query("users");
		qUserId.setQuery("sort by userid desc");
		qUserId.fetch(1, 0, new FetchCallback() {
			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
				for (BackbeamObject object : objects) {
					// Do something with this object
					lastUserId = (object.getNumber("userid").intValue() + 1);
					System.out.println("Leloo"
							+ "Numero ultimo user registrado: "
							+ object.getNumber("userid").intValue());
					System.out.println("Leloo" + "Numero nuevo user: "
							+ lastUserId);
				}

				final BackbeamObject user = new BackbeamObject("users");
				// inserto los valores de "offer"
				// util.log(idfile);
				System.out.println("Leloo" + "Numero email, user, name: "
						+ mail + ", " + nombre + ", " + lastUserId);
				user.setString("email", mail);
				user.setString("name", nombre);
				user.setNumber("userid", lastUserId);

				user.save(new ObjectCallback() {
					@Override
					public void success(BackbeamObject userReg) {
						System.out.println("Leloo" + "Email: "
								+ userReg.getString("email") + "\nNombre: "
								+ userReg.getString("name") + "\nUserId: "
								+ userReg.getNumber("userid"));

						Util.setPreferenceBoolean(context, "login", true);
						Util.setPreferenceInt(context, "userid", userReg
								.getNumber("userid").intValue());
						Util.setPreferenceString(context, "email",
								userReg.getString("email"));
						// falta añadir google login util.setSPstring(

						dondeVa(Util.getPreferenceInt(context, "place"));
					}

				});

			}
		});

	}

	public void isRegistered(String mail) {
		Query q = new Query("users");
		q.setQuery("where email = ?", mail);
		q.fetch(100, 0, new FetchCallback() {

			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
				if (totalCount == 1) {
					Util.setPreferenceInt(context, "userid", objects.get(0)
							.getNumber("userid").intValue());
					System.out.println("Leloo"
							+ "dentro del if de isRegistered");
					Util.setPreferenceBoolean(context, "login", true);

					dondeVa(Util.getPreferenceInt(context, "place"));
					System.out.println("Leloo" + "donde va: "
							+ Util.getPreferenceInt(context, "place"));
				} else {
					System.out.println("Leloo"
							+ "dentro del else de isregistered");
					registrarBB(email, nombre);

				}
			}
		});
		/*
		 * System.out.println("Leloo"+"estamos en isregistered"); if
		 * (prefs.getInt("userid", 0) != 0) {
		 * System.out.println("Leloo"+"dentro del if de isRegistered");
		 * editor.putBoolean("login", true);
		 * 
		 * editor.commit(); dondeVa(prefs.getInt("place", 0)); } else {
		 * System.out.println("Leloo"+"dentro del else de isregistered");
		 * registrarBB(email, nombre); }
		 */
	}

	public void logout() {

		Util.setPreferenceBoolean(context, "login", false);

	}

	public void dondeVa(int value) {

		System.out.println("Leloo" + "estoy en donde va");

		switch (value) {
		case 1: // IR A MAPA
			Intent imap = new Intent();
			imap.setClass(LoginActivity.this, Map.class);

			imap.putExtra("latitudSplash", latitudeSplash); // location.getLatitude());
			imap.putExtra("longitudSplash", longitudeSplash);// location.getLongitude());
			// System.out.println("Leloo"+"latitudSplash "+
			// location.getLatitude() +
			// "\n" + "longitudSplash "+ location.getLongitude());
			startActivity(imap);
			finish();

			break;
		case 2: // INSERTAR REPORT
			Intent ireport = new Intent();
			setResult(RESULT_OK, ireport);
			finish();
			break;
		case 3: // INSERTAR OFERTA
			Intent ioffer = new Intent();
			setResult(RESULT_OK, ioffer);
			finish();
			break;
		default:

		}
	}
}
