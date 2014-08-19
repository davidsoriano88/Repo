package com.wikout;

import utils.Util;
import model.FontUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;


public class Mapv2 extends ActionBarActivity {
	
	Util util = new Util();
	private GoogleMap map;
	LocationManager locationManager;
	Location location;
	double longitudeSW, latitudeSW, longitudeNE, latitudeNE, selectedLat, selectedLon;
	LocationClient mLocationClient;
    Context context;  
    boolean enableOk = false;
	protected SharedPreferences prefers;
	protected double userlat, userlon;
    public static ActionBarActivity fa;
    
    
    private static final int MENUITEM = Menu.FIRST;
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	 super.onCreate(savedInstanceState);
	 supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	 setContentView(R.layout.fragment_mapv2);
	 context=this;
	 FontUtils.setRobotoFont(context, ((Activity) context).getWindow().getDecorView());
	 fa = this;
     util.projectData(context);
     initUI();  
     
 	    
 	
	 }

	public void initUI(){
		
		setSupportProgressBarIndeterminateVisibility(true);
	
		 
	     //load slide menu items
		
	    map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map1)).getMap();
	
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    getSupportActionBar().setTitle("Seleccione un punto del mapa");
	    
		map.setMyLocationEnabled(true);

		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				map.clear();
				Projection proj = map.getProjection();
				Point coord = proj.toScreenLocation(point);

				map.addMarker(new MarkerOptions()
						.position(new LatLng(point.latitude,
										point.longitude))
						.draggable(true)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pin)));
				enableOk=true;
				supportInvalidateOptionsMenu();
				selectedLat = point.latitude;
				selectedLon = point.longitude;
				
		    	/*Intent mapLocation=new Intent();
		          // put the message in Intent
		          mapLocation.putExtra("pointlat", point.latitude);
		          mapLocation.putExtra("pointlon", point.longitude);
		          util.log("latitud del mapv2"+String.valueOf(point.latitude));*/
		          // Set The Result in Intent
		        //  setResult(RESULT_OK,mapLocation);
		          // finish The activity 
		 
		       //  finish();

			}
		});

		
		viewPort();	
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition position) {

				LatLngBounds curScreen = map.getProjection().getVisibleRegion().latLngBounds;
				latitudeNE = curScreen.northeast.latitude;
				latitudeSW = curScreen.southwest.latitude;
				longitudeNE = curScreen.northeast.longitude;
				longitudeSW = curScreen.southwest.longitude;
				util.log(String.valueOf(latitudeNE));
				util.log("screen has been recharged");
				 
			}
		});	
		setSupportProgressBarIndeterminateVisibility(false);
	}
	
	
	
	
	
	//contains info about the viewposition, clientposition...:
	public void viewPort(){
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		
		locationManager.requestLocationUpdates(
				locationManager.getBestProvider(criteria, false), 0, 0,
				listener);
		
		location = locationManager
				.getLastKnownLocation(locationManager.getBestProvider(criteria,
						false));
		
		if (location != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					location.getLatitude(), location.getLongitude()), 15.0F));
		
				
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location
							.getLongitude())) // Sets the center of the map to
												// location user
					.zoom(15.0F) // Sets the zoom
					.build(); //Creates a CameraPosition from the builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			
			util.log("location"+String.valueOf(location.getLatitude()));
		}else
		{
			//util.showToast(context, "location null");
		}
		
	}
	

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {

			this.finish();
			CommerceList.actionbarAct.finish();
			return true;
		} else if (id == R.id.btnOkMap) {
			if (util.isNetworkAvailable(context) == true) {
				Intent mapLocation = new Intent();
				// put the message in Intent
				mapLocation.putExtra("pointlat", selectedLat);
				mapLocation.putExtra("pointlon", selectedLon);
				// util.log("latitud del mapv2"+String.valueOf(point.latitude));*/
				// Set The Result in Intent
				setResult(RESULT_OK, mapLocation);
				// finish The activity

				finish();

			} else {
				util.showInfoDialog(context, "Lo sentimos",
						"Es necesaria conexión a internet");
			}

			return true;

			//
		}else{
		return super.onOptionsItemSelected(item);}
	}

	/*
	 	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
       case R.id.refresh:
			new LoadDataTask().execute();
			return true;
        	case android.R.id.home: 
			finish();
        		return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }*/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		
		MenuInflater inflater = getMenuInflater();
		
		
		inflater.inflate(R.menu.menu2, menu);
		menu.findItem(R.id.btnOkMap).setEnabled(enableOk)
									.setVisible(enableOk);
		return super.onCreateOptionsMenu(menu);
	}
	


	
	@Override
	public void supportInvalidateOptionsMenu() {
		
		super.supportInvalidateOptionsMenu();
	}

	/*
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu2, menu);
		
		return super.onCreateOptionsMenu(menu);
	}*/
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    switch(keycode) {
	        case KeyEvent.KEYCODE_BACK:
	        	System.out.println("entra aqui");
	        	CommerceList.actionbarAct.finish();
	        	finish();
	        	
	            return true;
	    }
	    return false;
	}

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
		public void onLocationChanged(Location locationn) {
		       System.out.println("Location = " + location);
		       userlat = location.getLatitude();
				 userlon = location.getLongitude();
				 if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
						 prefers = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = prefers.edit();
						editor.putFloat("latpos", (float) userlat);
						editor.putFloat("longpos", (float) userlon);
						editor.commit();
				}else{
					prefers = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefers.edit();
					editor.putFloat("latpos", (float) userlat);
					editor.putFloat("longpos", (float) userlon);
					editor.commit();
				}
		}
	};
}
