package walktrought;

import utils.Util;
import utils.MyLocation.LocationResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.wikout.LoginActivity;
import com.wikout.Map;
import com.wikout.R;

public class LastFragment extends Fragment implements LocationListener {

	// Context context = getParentFragment().getActivity();
	Util util;

	Button btnFinish;
	CheckBox cbxDontShow;

	public LocationResult locationResult = null;
	Location location;
	private LocationManager locationManager;

	double lat = 0, log = 0, latitude = 0, longitude = 0;

	private String provider;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.last_frag, container, false);

		btnFinish = (Button) v.findViewById(R.id.btnFinish);

		cbxDontShow = (CheckBox) v.findViewById(R.id.cbxDontShow);

		cbxDontShow.setTypeface(Typeface.createFromAsset(getActivity()
				.getAssets(), "fonts/Roboto-Regular.ttf"));
		btnFinish.setTypeface(Typeface.createFromAsset(getActivity()
				.getAssets(), "fonts/Roboto-Regular.ttf"));

		// Get the location manager
		getLocationMethod();

		btnFinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getActivity();

				if (cbxDontShow.isChecked() == true) {

					Util.setPreferenceBoolean(getActivity()
							.getApplicationContext(), "notour", true);

				}
				if (lat != 0) {
					Util.setPreferenceDouble(getActivity()
							.getApplicationContext(), "latpos", lat);
					Util.setPreferenceDouble(getActivity()
							.getApplicationContext(), "longpos", log);

				} else {

					Util.setPreferenceDouble(getActivity()
							.getApplicationContext(), "latpos", 0);
					Util.setPreferenceDouble(getActivity()
							.getApplicationContext(), "longpos", 0);
				}
				if (Util.getPreferenceBoolean(getActivity()
						.getApplicationContext(), "login") == true) {
					Intent mainIntent = new Intent().setClass(context,
							Map.class);
					mainIntent.putExtra("latitudSplash", lat);// location.getLatitude());
					mainIntent.putExtra("longitudSplash", log);// location.getLongitude());
					// System.out.println("latitudSplash "+
					// location.getLatitude() +
					// "\n" + "longitudSplash "+ location.getLongitude());

					startActivity(mainIntent);
					getActivity().finish();
				} else {
					Intent mainIntent = new Intent().setClass(context,
							LoginActivity.class);
					mainIntent.putExtra("latitudSplash", lat);// location.getLatitude());
					mainIntent.putExtra("longitudSplash", log);// location.getLongitude());
					// System.out.println("latitudSplash "+
					// location.getLatitude() +
					// "\n" + "longitudSplash "+ location.getLongitude());

					startActivity(mainIntent);
					getActivity().finish();
				}
			}
		});

		return v;
	}

	private void getLocationMethod() {
		// COJO EL PROVEEDOR DE LOCALIZACIONES (GPS Y EL PROVEEDOR DE LA RED)
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);
		// Initialize the location fields
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			System.out.println(location.getLatitude() + " "
					+ location.getLongitude());
			onLocationChanged(location);
		} else {
			System.out.println("Location not available");
			System.out.println("Location not available");
		}
	}

	/* Request updates at startup */
	@Override
	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 10, 1, this);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		lat = location.getLatitude();
		log = location.getLongitude();

		System.out.println(String.valueOf(lat));
		System.out.println(String.valueOf(log));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		System.out.println("Enabled new provider " + provider);

	}

	@Override
	public void onProviderDisabled(String provider) {
		System.out.println("Disabled provider " + provider);
	}

}
