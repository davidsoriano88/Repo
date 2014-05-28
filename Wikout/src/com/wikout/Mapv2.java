package com.wikout;

import utils.Util;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


public class Mapv2 extends ActionBarActivity {
	
	Util util = new Util();
	private GoogleMap map;
	LocationManager locationManager;
	Location location;
	double longitudeSW, latitudeSW, longitudeNE, latitudeNE;
	LocationClient mLocationClient;
    Context context;  

  
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	 super.onCreate(savedInstanceState);
	 supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	 setContentView(R.layout.fragment_mapv2);
	 context=this;

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
				
				Projection proj = map.getProjection();
				Point coord = proj.toScreenLocation(point);
				
				
		util.showToast(Mapv2.this, "Click\n" + "Lat: " + point.latitude + "\n" + "Lng: "
						+ point.longitude + "\n" + "X: " + coord.x
						+ " - Y: " + coord.y);
				
		
		///establecemos el paso a la siguiente pantalla y le pasamos valores:
		Intent commerceList = new Intent(context, CommerceList.class);
		commerceList.putExtra("pointlat", point.latitude);
		commerceList.putExtra("pointlon", point.longitude);
		/*insert.putExtra("latiMain", point.latitude);
		insert.putExtra("longiMain", point.longitude);
		enter = 1;
		insert.putExtra("enter", enter);*/
		startActivity(commerceList);
		finish();
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
			util.showToast(context, "location null");
		}
		
	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case android.R.id.home: 
        		this.finish();
        		return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu2, menu);
		return super.onCreateOptionsMenu(menu);
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
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("latpos", String.valueOf(lat)); 
			editor.putString("longpos", String.valueOf(lon));
			editor.commit();
			
			locationManager.removeUpdates(listener);	
		}
	};
}
