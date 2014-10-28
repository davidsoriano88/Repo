package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.ObjectCallback;
import io.backbeam.Query;

import java.util.ArrayList;
import java.util.List;

import model.FontUtils;
import model.NavDrawerItem;
import utils.ArrayAdapterWithIcon;
import utils.Place;
import utils.PlacesService;
import utils.Util;
import adapter.NavDrawerListAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map extends ActionBarActivity implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener,
		OnMyLocationButtonClickListener,
		com.google.android.gms.location.LocationListener {

	// COMPONENTES
	ImageView filterView, ivIconSearch;
	TextView tvFilterText;
	ImageButton ibFilter;
	
	EditText etSearch;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;
	ListView lvDrawer;

	private GoogleMap map;
	private Marker markB, markG;
	private LocationManager locationManager;
	private LocationClient mLocationClient;
	private Location location;
	private Handler handler = new Handler();

	protected AsyncTask<Void, Void, ArrayList<Place>> asyncPlaces;
	protected AsyncTask<Void, Integer, Boolean> asyncBackbeam;
	protected long snap = System.currentTimeMillis();

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	private Context context;

	Util util = new Util();

	String title, finalId, option, filter, searchResult;
	double longitudeSW, latitudeSW, longitudeNE, latitudeNE, latitudeSplash,
			longitudeSplash, userlat, userlon;
	protected int mDpi = 0;
	// LocationClient locationClient;

	ArrayList<String> placeName = new ArrayList<String>(),
			idcommerce = new ArrayList<String>(),
			idcommerceonmap = new ArrayList<String>(),
			idMarker = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Componentes de pantalla
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.fragment_main);
		context = this;
		FontUtils.setRobotoFont(context, ((Activity) context).getWindow()
				.getDecorView());
		mDpi = getResources().getDisplayMetrics().densityDpi;

		// Recojo Bundle
		Bundle splash = getIntent().getExtras();
		latitudeSplash = splash.getDouble("latitudSplash");
		userlat = splash.getDouble("latitudSplash");

		longitudeSplash = splash.getDouble("longitudSplash");
		userlon = splash.getDouble("longitudSplash");
		System.out.println("Coordenadas en MAPS recibidas del intent: LAT "
				+ userlat + " LON " + userlon);

		Util.setPreferenceDouble(context, "latpos",userlat);
		Util.setPreferenceDouble(context, "longpos",userlon);
		System.out.println("parametros que paso a insertoffer:\n" + "LAT: "
				+ userlat + " LON: " + userlon);
		System.out.println("PREFS: " +Util.getPreferenceDouble(context, "latpos"));
		
		// Datos Backbeam
		util.projectData(context);

		initUI();

	}

	public void initUI() {

		// ProgressBar
		setSupportProgressBarIndeterminateVisibility(true);

		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			// Try to obtain the map from the SupportMapFragment.
			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			map.setMyLocationEnabled(true);
			// Check if we were successful in obtaining the map.
			if (map != null) {
				map.setMyLocationEnabled(true);
				map.setOnMyLocationButtonClickListener(this);
				map.getUiSettings().setMyLocationButtonEnabled(true);

			}
		}
		map.setMyLocationEnabled(true);

		// Identifico componentes de pantalla
		// map = ((SupportMapFragment)
		// getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		lvDrawer = (ListView) findViewById(R.id.left_drawer);
		filterView = (ImageView) findViewById(R.id.filterText);
		tvFilterText = (TextView) findViewById(R.id.tvFilterText);
		ibFilter = (ImageButton) findViewById(R.id.filterButton);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		String[] navMenuTitles = getResources().getStringArray(R.array.options);
		ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();

		setUpLocationClientIfNeeded();
		mLocationClient.connect();

		// map.setMyLocationEnabled(true);
		filterVisible(false);

		String fontPathLight = "fonts/Roboto-Light.ttf";
		// get the font face
		Typeface tf = Typeface.createFromAsset(getAssets(), fontPathLight);
		LayoutInflater inflater = LayoutInflater.from(this);

		// incializamos el header del ListView:
		View search = inflater.inflate(R.layout.search_drawer, null);
		etSearch = (EditText) search.findViewById(R.id.search1);
		etSearch.setTypeface(tf);
		ivIconSearch = (ImageView) search.findViewById(R.id.ivIconSearch);
		lvDrawer.addHeaderView(search);

		// asignamos los items con su icono:
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0],
				R.drawable.filter_icon));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1],
				R.drawable.info_icon));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2],
				R.drawable.logout_icon));
		lvDrawer.setAdapter(new NavDrawerListAdapter(context, navDrawerItems));

		// asignamos la funcionalidad drawerToggle:
		drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		drawerLayout, /* DrawerLayout object */
		R.drawable.ic_navigation_drawe, /* nav drawer image to replace 'Up'caret */
		R.string.oferta, /* "open drawer" description for accessibility */
		R.string.hello_world /* "close drawer" description for accessibility */
		);
		// prueba.setDrawerIndicatorEnabled(true);
		drawerLayout.setDrawerListener(drawerToggle);

		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// establecemos las opciones del menu deslizable:
		lvDrawer.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				drawerOpener();
				switch (pos) {
				case 0:
					break;
				case 1:
					final String[] filterItems = new String[] { "Ocio",
							"Servicios", "Compras", "Otros" };
					final Integer[] filterIcons = new Integer[] {
							R.drawable.pinazul, R.drawable.pinmorado,
							R.drawable.pinrosa, R.drawable.pinverde };
					// filterVisible(true);

					AlertDialog.Builder builder = new AlertDialog.Builder(
							context);
					ListAdapter adapter = new ArrayAdapterWithIcon(context,
							filterItems, filterIcons);
					builder.setTitle("Filtrar por: ");
					builder.setAdapter(adapter,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int item) {
									// Do something with the selection
									getSupportActionBar()
											.setTitle("Resultados");
									etSearch.setText("");
									imm.hideSoftInputFromWindow(
											etSearch.getWindowToken(), 0);
									switch (item) {
									case 0:
										filter = "ocio";
										tvFilterText.setText("Filtrado por: "
												+ filter);
										new MyData().execute();
										break;
									case 1:
										filter = "servicios";
										tvFilterText.setText("Filtrado por: "
												+ filter);
										new MyData().execute();
										break;
									case 2:
										filter = "compras";
										tvFilterText.setText("Filtrado por: "
												+ filter);
										new MyData().execute();
										break;
									case 3:
										filter = "otros";
										tvFilterText.setText("Filtrado por: "
												+ filter);
										new MyData().execute();
										break;
									}
									filterVisible(true);

								}

							});

					AlertDialog alert = builder.create();
					alert.show();
					searchResult = null;
					break;
				case 2:
					util.showInfoDialog(context, "Wikout",
							"Aplicacion desarrollada por Uptimiza. 2014");
					break;

				case 3:
					if(Util.getPreferenceBoolean(context, "login")==true){
					AlertDialog.Builder dialogLocation = new AlertDialog.Builder(context);
					dialogLocation.setTitle("Salir");
					dialogLocation.setMessage("Antes de salir... �Deseas cerrar sesi�n?");
					dialogLocation.setCancelable(true);
					dialogLocation.setPositiveButton("S�",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogo1, int id) {
									Util.setPreferenceBoolean(context, "login", false);
									//Util.setPreferenceString(context, "email", "");
									android.os.Process.killProcess(android.os.Process.myPid());
									}

							});
					dialogLocation.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogo1, int id) {
								
									android.os.Process.killProcess(android.os.Process.myPid());
								}
							});
					dialogLocation.show();
					}else{
						android.os.Process.killProcess(android.os.Process.myPid());
					}
					break;
				}

			}
		});

		etSearch.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					filter = null;
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					searchResult = etSearch.getText().toString();
					imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					tvFilterText.setText("Buscando por: " + etSearch.getText());
					getSupportActionBar().setTitle("Resultados");
					filterVisible(true);
					new MyData().execute();
					drawerOpener();
					etSearch.setText("");
					return true;
				}
				return false;
			}
		});
		ivIconSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
					// Perform action on key press
					filter = null;
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					searchResult = etSearch.getText().toString();
					imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					tvFilterText.setText("Buscando por: " + etSearch.getText());
					getSupportActionBar().setTitle("Resultados");
					filterVisible(true);
					new MyData().execute();
					drawerOpener();
					etSearch.setText("");

			}
		});
		ibFilter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				filter = null;
				searchResult = null;
				filterVisible(false);
				new MyData().execute();
				getSupportActionBar().setTitle("Wikout");
			}

		});

		// viewPort();

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_next) {
			if (util.isNetworkAvailable(context) == true) {

				Intent insert = new Intent(context, InsertOffer.class);
				startActivity(insert);
				return true;

			} else {
				util.showInfoDialog(context, "Lo sentimos",
						"Es necesaria conexi�n a internet");
				return false;
			}
		} else if (id == android.R.id.home) {
			drawerOpener();
			return true;
		} else {

			return super.onOptionsItemSelected(item);
		}
	}

	// configuracion visibilidad del filtro:
	public void filterVisible(Boolean visible) {
		if (visible == false) {
			ibFilter.setVisibility(4);
			tvFilterText.setVisibility(4);
			filterView.setVisibility(4);

		} else {
			ibFilter.setVisibility(0);
			tvFilterText.setVisibility(0);
			filterView.setVisibility(0);
		}
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			drawerOpener();
			return true;
		}
		return super.onKeyDown(keycode, e);
	}

	// comprueba si el menu est� abierto o cerrado y lo abre o cierra en
	// consecuencia:
	public void drawerOpener() {
		if (drawerLayout.isDrawerOpen(lvDrawer)) {
			drawerLayout.closeDrawer(lvDrawer);
		} else {
			drawerLayout.openDrawer(lvDrawer);
		}
	}

	// contains info about the viewposition, clientposition...:
	public void viewPort() {

		location = map.getMyLocation();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		locationManager.requestLocationUpdates(
				locationManager.getBestProvider(criteria, false), 100, 0,
				listener);

		location = locationManager.getLastKnownLocation(locationManager
				.getBestProvider(criteria, false));
		/*
		 * LocationManager locationManager = (LocationManager)
		 * getSystemService(LOCATION_SERVICE); String bestProvider =
		 * locationManager.getBestProvider(criteria, false); Location location =
		 * locationManager.getLastKnownLocation(bestProvider);
		 */

		System.out.println("mLocationClient.getLastLocation().getLatitude(): "
				+ mLocationClient.getLastLocation().getLatitude());

		if (location == null) {
			if (longitudeSplash == 0 && latitudeSplash == 0) {
				util.showInfoDialog(context, "Lo sentimos",
						"No podemos detectar tu posici�n");
				location = new Location(LocationManager.NETWORK_PROVIDER);
				location.setLatitude(41.6561);
				location.setLongitude(-0.8773);
			} else {
				System.out.println("latitude en map: "
						+ String.valueOf(latitudeSplash));
				location = new Location(LocationManager.NETWORK_PROVIDER);
				location.setLatitude(latitudeSplash);
				location.setLongitude(longitudeSplash);
			}
		}
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(location.getLatitude(), location.getLongitude()),
				15.0F));

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), location
						.getLongitude())) // Sets the center of the map to
											// location user
				.zoom(15.0F) // Sets the zoom
				.build(); // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		map.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition position) {

				snap = System.currentTimeMillis();
				handler.removeCallbacks(runnable, null);
				handler.postDelayed(runnable, 1000);

			}

		});

		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getApplicationContext(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	/**
	 * Button to get current Location. This demonstrates how to get the current
	 * Location as required without needing to register a LocationListener.
	 */
	public void showMyLocation(View view) {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			String msg = "Location = " + mLocationClient.getLastLocation();
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		System.out.println("Location = " + location);
		userlat = location.getLatitude();
		userlon = location.getLongitude();
	
		
		Util.setPreferenceDouble(context, "latpos",userlat);
		Util.setPreferenceDouble(context, "longpos",userlon);
		System.out.println("parametros que paso a insertoffer:\n" + "LAT: "
				+ userlat + " LON: " + userlon);
		System.out.println("PREFS: " + Util.getPreferenceDouble(context, "latpos"));

	}

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
		if (userlat != 0.0) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					userlat, userlon), 15.0F));
		} else {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLocationClient.getLastLocation().getLatitude(),
					mLocationClient.getLastLocation().getLongitude()), 15.0F));

		}
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(mLocationClient.getLastLocation()
						.getLatitude(), mLocationClient.getLastLocation()
						.getLongitude())) // Sets the center of the map to
											// location user
				.zoom(15.0F) // Sets the zoom
				.build(); // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		map.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition position) {

				snap = System.currentTimeMillis();
				handler.removeCallbacks(runnable, null);
				handler.postDelayed(runnable, 500);

			}

		});
		setSupportProgressBarIndeterminateVisibility(false);
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
		// Do nothing
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
	}

	@Override
	public boolean onMyLocationButtonClick() {
		System.out.println("Location = " + location);

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			// Define the criteria how to select the locatioin provider -> use
			// default
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, false);
			Location location = locationManager.getLastKnownLocation(provider);

			// Initialize the location fields
			if (location != null) {
				System.out.println("Provider " + provider
						+ " has been selected.");
				System.out.println(location.getLatitude() + " "
						+ location.getLongitude());
				onLocationChanged(location);
			} else {
				System.out.println("Location not available");
				System.out.println("Location not available");
			}
			userlat = location.getLatitude();
			userlon = location.getLongitude();
			System.out.println(userlat + " " + userlon);
			
			Util.setPreferenceDouble(context, "latpos",userlat);
			Util.setPreferenceDouble(context, "longpos",userlon);
		}
		// Return false so that we don't consume the event and the default
		// behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			/* do what you need to do */

			LatLngBounds curScreen = map.getProjection().getVisibleRegion().latLngBounds;
			latitudeNE = curScreen.northeast.latitude;
			latitudeSW = curScreen.southwest.latitude;
			longitudeNE = curScreen.northeast.longitude;
			longitudeSW = curScreen.southwest.longitude;
			util.log("screen has been recharged");

			// start both asyncTask:
			if (util.isNetworkAvailable(context) == true) {
				asyncBackbeam = new MyData().execute();
				 //asyncPlaces = new GetPlaces("").execute();

			} else {
				util.showInfoDialog(context, "Lo sentimos",
						"Es necesaria conexi�n a internet");
			}
		}

	};

	private LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {

			System.out.println("Location = " + location);
			userlat = location.getLatitude();
			userlon = location.getLongitude();
			
			Util.setPreferenceDouble(context, "latpos",userlat);
			Util.setPreferenceDouble(context, "longpos",userlon);
			locationManager.removeUpdates(listener);

		}
	};

	// gets data from google places:
	private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

		private String places;

		public GetPlaces(String places) {
			System.out.println("recorremos getplaces");

			this.places = places;
		}

		@Override
		protected void onPostExecute(ArrayList<Place> result) {
			super.onPostExecute(result);
			System.out.println("recorremos post execute places");

			for (int i = 0; i < result.size(); i++) {
				markG = map.addMarker(new MarkerOptions()
						.title(result.get(i).getName())
						.position(
								new LatLng(result.get(i).getLatitude(), result
										.get(i).getLongitude()))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pinplaces))
						.snippet(result.get(i).getVicinity()));
			}
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			System.out.println("recorremos pre-execute places");
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... arg0) {
			PlacesService service = new PlacesService(
					"AIzaSyCyZF9Cxz6bhgzuGLt7OvD3f_gPqsfvJSI");
			ArrayList<Place> findPlaces = service.findPlaces(userlat, userlon, places);
			//ArrayList<Place> findPlaces = service.search(latitudeSplash, longitudeSplash, 300, null);
			//System.out.println("placesprueba1: " + location.getLatitude()+ location.getLongitude());
			System.out.println("DENTRO DE GETPLACES");
			for (int i = 0; i < findPlaces.size(); i++) {

				Place placeDetail = findPlaces.get(i);
			System.out.println("places : " + placeDetail.getName());
			}
			return findPlaces;
		}
	}

	// gets data from Backbeam:
	private class MyData extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {

			util.log("recorremos post execute mydata");
			util.log("filtro: " + filter + " busqueda: " + searchResult);
			if (filter == null && searchResult == null) {
				// standardQuery();

				final Intent info = new Intent(context, OfferList.class);

				Query query = new Query("commerce");
				query.bounding("placelocation", latitudeSW, longitudeSW,
						latitudeNE, longitudeNE, 40, new FetchCallback() {

							@Override
							public void success(List<BackbeamObject> commerces,
									int totalCount, boolean fromCache) {
								// MARCADORES BACKBEAM
								map.clear();
								// Nombre Comercio
								placeName.clear();
								// Id Comercio
								idcommerce.clear();
								// Id Marcador
								idMarker.clear();
								// ID comercios encuadrados
								idcommerceonmap.clear();
								util.log("map clear mydata");
								
								for (final BackbeamObject object : commerces) {

									
									
									if (object.getNumber("actualoffers")
											.intValue() > 0) {
										util.log("1" + object.getId());
										placeName.add(object
												.getString("placename"));
										idcommerce.add(object.getId());

										switch (object.getString("category")) {
										case ("ocio"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinazul,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));

											/*
											 * .icon(BitmapDescriptorFactory
											 * .fromResource
											 * (R.drawable.pinazul)));
											 */
											break;
										case ("servicios"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinmorado,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));
											break;
										case ("compras"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinrosa,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));
											break;
										case ("otros"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinverde,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));
											break;
										default:
											break;
										}
										idMarker.add(markB.getId());
										util.log("2" + object.getId());
										map.setOnMarkerClickListener(new OnMarkerClickListener() {
											@Override
											public boolean onMarkerClick(
													Marker marker) {
												marker.showInfoWindow();
												for (int i = 0; i < placeName
														.size(); i++) {
													if (marker.getTitle()
															.equals(placeName
																	.get(i))) {
														finalId = idcommerce
																.get(i);
														break;
													}
												}
												util.log("marcador mydata pulsado, id marcador:"
														+ finalId
														+ ","
														+ marker.getTitle()
														+ marker.getPosition()
														+ "");
												return true;
											}
										});
										util.log("3" + object.getId());
										map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
											@Override
											public void onInfoWindowClick(
													Marker marker) {
												util.log("4" + object.getId());
												for (int i = 0; i < idMarker
														.size(); i++) {
													if (idMarker
															.get(i)
															.contains(
																	marker.getId())) {
														util.log("titulo marcador mydata pulsado, id marcador:"
																+ finalId
																+ ","
																+ marker.getTitle());
														info.putExtra("id",
																finalId);

														if (mLocationClient != null
																&& mLocationClient
																		.isConnected()) {
															info.putExtra(
																	"location",
																	mLocationClient
																			.getLastLocation()
																			.toString());
															info.putExtra(
																	"userlatitude",
																	mLocationClient
																			.getLastLocation()
																			.getLatitude());
															info.putExtra(
																	"userlongitude",
																	mLocationClient
																			.getLastLocation()
																			.getLongitude());
														}
														if (util.isNetworkAvailable(context) == true) {
															startActivity(info);
														} else {
															util.showInfoDialog(
																	context,
																	"Lo sentimos",
																	"Es necesaria conexi�n a internet");
														}

													}
												}
											}
										});

									}
								}
								setSupportProgressBarIndeterminateVisibility(false);
							}
						});

			} else if (searchResult == null) {
				// filterQuery(filter);
				final Intent info = new Intent(context, OfferList.class);

				Query query = new Query("commerce");
				query.bounding("placelocation", latitudeSW, longitudeSW,
						latitudeNE, longitudeNE, 40, new FetchCallback() {

							@Override
							public void success(List<BackbeamObject> objects,
									int totalCount, boolean fromCache) {

								map.clear();
								// Nombre Comercio
								placeName.clear();
								// Id Comercio
								idcommerce.clear();
								// Id Marcador
								idMarker.clear();
								// ID comercios encuadrados
								idcommerceonmap.clear();

								util.log("map clear mydata");
								for (final BackbeamObject object : objects) {
									if (object.getString("category").equals(
											filter) == true
											&& object.getNumber("actualoffers")
													.intValue() > 0) {
										util.log("1" + object.getId());
										placeName.add(object
												.getString("placename"));
										idcommerce.add(object.getId());

										switch (object.getString("category")) {
										case ("ocio"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinazul,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));
											break;
										case ("servicios"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinmorado,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));
											break;
										case ("compras"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinrosa,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));
											break;
										case ("otros"):
											markB = map
													.addMarker(new MarkerOptions()
															.position(
																	new LatLng(
																			object.getLocation(
																					"placelocation")
																					.getLatitude(),
																			object.getLocation(
																					"placelocation")
																					.getLongitude()))
															.draggable(false)
															.snippet(
																	object.getNumber(
																			"actualoffers")
																			.toString()
																			+ " ofertas; "
																			+ object.getNumber(
																					"numbubble")
																					.toString()
																			+ " "
																			+ getResources()
																					.getString(
																							R.string.heartofferlist))
															.title(object
																	.getString("placename"))
															.icon(BitmapDescriptorFactory
																	.fromBitmap(util.ajusteBitmap(mDpi ,util
																			.writeTextOnDrawable(
																					context,
																					R.drawable.pinverde,
																					object.getNumber(
																							"actualoffers")
																							.toString())))));
											break;
										default:
											break;
										}
										idMarker.add(markB.getId());
										util.log("2" + object.getId());
										map.setOnMarkerClickListener(new OnMarkerClickListener() {
											@Override
											public boolean onMarkerClick(
													Marker marker) {
												marker.showInfoWindow();
												for (int i = 0; i < placeName
														.size(); i++) {
													if (marker.getTitle()
															.equals(placeName
																	.get(i))) {
														finalId = idcommerce
																.get(i);
														break;
													}
												}
												util.log("marcador mydata pulsado, id marcador:"
														+ finalId
														+ ","
														+ marker.getTitle()
														+ marker.getPosition()
														+ "");
												return true;
											}
										});
										util.log("3" + object.getId());
										map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
											@Override
											public void onInfoWindowClick(
													Marker marker) {
												util.log("4" + object.getId());
												for (int i = 0; i < idMarker
														.size(); i++) {
													if (idMarker
															.get(i)
															.contains(
																	marker.getId())) {
														util.log("titulo marcador mydata pulsado, id marcador:"
																+ finalId
																+ ","
																+ marker.getTitle());
														info.putExtra("id",
																finalId);
														if (mLocationClient != null
																&& mLocationClient
																		.isConnected()) {
															info.putExtra(
																	"location",
																	mLocationClient
																			.getLastLocation()
																			.toString());
															info.putExtra(
																	"userlatitude",
																	mLocationClient
																			.getLastLocation()
																			.getLatitude());
															info.putExtra(
																	"userlongitude",
																	mLocationClient
																			.getLastLocation()
																			.getLongitude());
														}
														if (util.isNetworkAvailable(context) == true) {
															startActivity(info);
														} else {
															util.showInfoDialog(
																	context,
																	"Lo sentimos",
																	"Es necesaria conexi�n a internet");
														}

													}
												}
											}
										});
										setSupportProgressBarIndeterminateVisibility(false);
									}
								}

							}

						});
			} else {
				// commercesOnMap(searchResult);

				final Intent info = new Intent(context, OfferList.class);

				util.log("fff ha entrado en la funcion busqueda(1)");
				Query queryCommerce = new Query("commerce");
				queryCommerce.setQuery("where placename like ?", searchResult)
						.fetch(100, 0, new FetchCallback() {
							@Override
							public void success(List<BackbeamObject> objects,
									int totalCount, boolean fromCache) {
								map.clear();
								// Nombre Comercio
								placeName.clear();
								// Id Comercio
								idcommerce.clear();
								// Id Marcador
								idMarker.clear();
								// ID comercios encuadrados
								idcommerceonmap.clear();
								System.out.println("Numero de comercios con '"
										+ searchResult + "': " + totalCount);
								for (BackbeamObject commerce : objects) {
									if (commerce.getLocation("placelocation")
											.getLatitude() < latitudeNE
											&& commerce.getLocation(
													"placelocation")
													.getLatitude() > latitudeSW
											&& commerce.getLocation(
													"placelocation")
													.getLongitude() < longitudeNE
											&& commerce.getLocation(
													"placelocation")
													.getLongitude() > longitudeSW) {
										// METO LOS IDCOMMERCE EN UN ARRAY
										idcommerce.add(commerce.getId());
										util.log("fff ha entrado en el success busqueda(2)");
									}
								}
							}

						});
				Query queryOffer = new Query("offer");
				queryOffer.setQuery("where description like ? join commerce",
						searchResult).fetch(100, 0, new FetchCallback() {
					@Override
					public void success(List<BackbeamObject> objects,
							int totalCount, boolean fromCache) {
						util.log("fff ha entrado en el success oferta(3)");
						System.out.println("Numero de ofertas con '"
								+ searchResult + "': " + totalCount);
						for (BackbeamObject offer : objects) {
							BackbeamObject commerce = offer
									.getObject("commerce");
							if (commerce.getLocation("placelocation")
									.getLatitude() < latitudeNE
									&& commerce.getLocation("placelocation")
											.getLatitude() > latitudeSW
									&& commerce.getLocation("placelocation")
											.getLongitude() < longitudeNE
									&& commerce.getLocation("placelocation")
											.getLongitude() > longitudeSW) {
								// COMPRUEBO SI EL ID YA ESTA DENTRO DEL ARRAY
								if (!idcommerce.contains(commerce.getId())) {
									idcommerce.add(commerce.getId());
									util.log("fff comprueba bien (4)");
								}
							}
						}

						// CREAR MARCADOR EN EL MAPA (cambiar for por el de
						// abajo)
						for (String commerce : idcommerce) {
							Backbeam.read("commerce", commerce,
									new ObjectCallback() {
										@Override
										public void success(
												final BackbeamObject commerceMark) {
											// CREAR MARCADOR
											util.log("fff creamos marcadores (5)");

											util.log("1 "
													+ commerceMark.getId());

											switch (commerceMark
													.getString("category")) {
											case ("ocio"):
												markB = map
														.addMarker(new MarkerOptions()
																.position(
																		new LatLng(
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLatitude(),
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLongitude()))
																.draggable(
																		false)
																.title(commerceMark
																		.getString("placename"))
																.icon(BitmapDescriptorFactory
																		.fromBitmap(util.ajusteBitmap(mDpi ,util
																				.writeTextOnDrawable(
																						context,
																						R.drawable.pinazul,
																						commerceMark
																								.getNumber(
																										"actualoffers")
																								.toString())))));
												break;
											case ("servicios"):
												markB = map
														.addMarker(new MarkerOptions()
																.position(
																		new LatLng(
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLatitude(),
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLongitude()))
																.draggable(
																		false)
																.title(commerceMark
																		.getString("placename"))
																.icon(BitmapDescriptorFactory
																		.fromBitmap(util.ajusteBitmap(mDpi ,util
																				.writeTextOnDrawable(
																						context,
																						R.drawable.pinmorado,
																						commerceMark
																								.getNumber(
																										"actualoffers")
																								.toString())))));
												break;
											case ("compras"):
												markB = map
														.addMarker(new MarkerOptions()
																.position(
																		new LatLng(
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLatitude(),
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLongitude()))
																.draggable(
																		false)
																.title(commerceMark
																		.getString("placename"))
																.icon(BitmapDescriptorFactory
																		.fromBitmap(util
																				.writeTextOnDrawable(
																						context,
																						R.drawable.pinrosa,
																						commerceMark
																								.getNumber(
																										"actualoffers")
																								.toString()))));
												break;
											case ("otros"):
												markB = map
														.addMarker(new MarkerOptions()
																.position(
																		new LatLng(
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLatitude(),
																				commerceMark
																						.getLocation(
																								"placelocation")
																						.getLongitude()))
																.draggable(
																		false)
																.title(commerceMark
																		.getString("placename"))
																.icon(BitmapDescriptorFactory
																		.fromBitmap(util.ajusteBitmap(mDpi ,util
																				.writeTextOnDrawable(
																						context,
																						R.drawable.pinverde,
																						commerceMark
																								.getNumber(
																										"actualoffers")
																								.toString())))));
												break;
											default:
												break;
											}
											placeName.add(commerceMark
													.getString("placename"));
											idcommerceonmap.add(commerceMark
													.getId());
											idMarker.add(markB.getId());
											util.log("2 "
													+ commerceMark.getId()
													+ "   "
													+ commerceMark
															.getString("placename"));
											map.setOnMarkerClickListener(new OnMarkerClickListener() {
												@Override
												public boolean onMarkerClick(
														Marker marker) {
													marker.showInfoWindow();
													for (int i = 0; i < placeName
															.size(); i++) {
														if (marker
																.getTitle()
																.equals(placeName
																		.get(i))) {
															finalId = idcommerceonmap
																	.get(i);
															break;
														}
													}
													util.log("marcador mydata pulsado, id marcador:"
															+ finalId
															+ ","
															+ marker.getTitle()
															+ marker.getPosition()
															+ "");
													return true;
												}
											});
											util.log("3 "
													+ commerceMark.getId()
													+ "   "
													+ commerceMark
															.getString("placename"));
											map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
												@Override
												public void onInfoWindowClick(
														Marker marker) {

													for (int i = 0; i < placeName
															.size(); i++) {
														if (marker
																.getTitle()
																.equals(placeName
																		.get(i))) {
															finalId = idcommerceonmap
																	.get(i);
															util.log("titulo marcador mydata pulsado, id marcador:"
																	+ marker.getId()
																	+ ","
																	+ marker.getTitle());
															info.putExtra("id",
																	finalId);
															if (mLocationClient != null
																	&& mLocationClient
																			.isConnected()) {
																info.putExtra(
																		"location",
																		mLocationClient
																				.getLastLocation()
																				.toString());
																info.putExtra(
																		"userlatitude",
																		mLocationClient
																				.getLastLocation()
																				.getLatitude());
																info.putExtra(
																		"userlongitude",
																		mLocationClient
																				.getLastLocation()
																				.getLongitude());
															}
															if (util.isNetworkAvailable(context) == true) {
																startActivity(info);
															} else {
																util.showInfoDialog(
																		context,
																		"Lo sentimos",
																		"Es necesaria conexi�n a internet");
															}

														}
													}
												}
											});

										}
									});
						}
						setSupportProgressBarIndeterminateVisibility(false);

					}

				});

			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			/*
			 * util.log("recorremos pre execute");
			 * util.showProgressDialog(context);
			 * util.log("mostramos dialog mydata");
			 */
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			util.log("doInBackgroundRecorrido mydata");

			return true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onRestart() {
		super.onResume();
		// new MyData().execute();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
