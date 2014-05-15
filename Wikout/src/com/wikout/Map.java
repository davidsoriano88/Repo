package com.wikout;

import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.Query;

import java.util.ArrayList;
import java.util.List;

import utils.ItemObject;
import utils.Place;
import utils.PlacesService;
import utils.Util;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class Map extends ActionBarActivity {
	
	//
	private GoogleMap map;
	Marker marker, mark;
	String title, finalId, option;
	LocationRequest request;
	LocationManager locationManager;
	LatLng myLocation;
	Location location;
	public Intent insert;
	double longitudeSW, latitudeSW, longitudeNE, latitudeNE;
	int enter;
	ActionBar actionBar;
	private String[] places;
	LocationClient mLocationClient;
	Util util = new Util();

	
	private String[] titulos;
	private DrawerLayout navDrawerLayout;
	private ListView NavList;
    private ArrayList<ItemObject> NavItms;
    private TypedArray NavIcons;	
    Context context;
    

	
	
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.fragment_main);
	    context=this;
	    String[] values = getResources().getStringArray(R.array.options);
        navDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView optionList = (ListView) findViewById(R.id.left_drawer);
        optionList.setAdapter(new ArrayAdapter<String>(this, R.layout.item_drawer, values));
        optionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				switch(pos){
				case 0: util.showToast(getApplicationContext(), "buscar"); break;
					
				case 1: util.showToast(getApplicationContext(), "Filtrar"); break;
					
				case 2: util.showInfoDialog(context, "Wikout", "Aplicación desarrollado por Uptimiza. 2014"); break;
					
				case 3: android.os.Process.killProcess(android.os.Process.myPid()); break;
				}
				
			}});
	   initUI();
	    util.projectData(context);
	   
	  }

	public void initUI(){
		insert = new Intent(getApplicationContext(), InsertActivity.class);
		places = getResources().getStringArray(R.array.places);
		
		
		
		
		navDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		
		map.setMyLocationEnabled(true);
	

		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				Projection proj = map.getProjection();
				Point coord = proj.toScreenLocation(point);
				
				
				util.showToast(Map.this, "Click\n" + "Lat: " + point.latitude + "\n" + "Lng: "
						+ point.longitude + "\n" + "X: " + coord.x
						+ " - Y: " + coord.y);
				
				
				insert.putExtra("latiMain", point.latitude);
				insert.putExtra("longiMain", point.longitude);
				enter = 1;
				insert.putExtra("enter", enter);
				startActivity(insert);
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
				//start both asyncTask:
				MyData data = (MyData) new MyData(Map.this).execute();
				GetPlaces getplaces= (GetPlaces) new GetPlaces(Map.this,"").execute();
			}
		});	
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_search:
        	startActivity(insert);
			enter = 0;
			insert.putExtra("enter", enter);
                        return true;
       
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
		}
	@SuppressWarnings("deprecation")
	public void InfoVersion(){
		AlertDialog info = new AlertDialog.Builder(this).create();
		info.setTitle("Wikout");
		info.setMessage("Wikout, desarrollado por Uptimiza.");
		info.setButton("OK", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
		      // TODO Add your code for the button here.
		   }
		});
		// Set the Icon for the Dialog
		info.show();
	}
	
	//contains info about the viewposition, clientposition...:
	public void viewPort(){
		Intent myPos= new Intent(getApplicationContext(),PlacesService.class);
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
					location.getLatitude(), location.getLongitude()), 13.0F));
		
				
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location
							.getLongitude())) // Sets the center of the map to
												// location user
					.zoom(17.0F) // Sets the zoom
					.build(); // Creates a CameraPosition from the builder
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			
			util.log(String.valueOf(location.getLatitude()));
		}
		myPos.putExtra("latitude", location.getLatitude());
		myPos.putExtra("longitude", location.getLongitude());
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
		public void onLocationChanged(Location location) {
			insert = new Intent(getApplicationContext(), InsertActivity.class);
			util.log( "location update : " + location);
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			insert.putExtra("latpos", lat);
			insert.putExtra("longpos", lon);
			
			locationManager.removeUpdates(listener);
		
		}
	};
	//gets data from google places:
	private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

		
		private Context context;
		private String places;
		
		public GetPlaces(Context context, String places) {
			util.log("recorremos getplaces");
		this.context = context;
		this.places = places;
		}

		@Override
		protected void onPostExecute(ArrayList<Place> result) {
		super.onPostExecute(result);
		util.log("recorremos post execute places");
		
		for (int i = 0; i <result.size(); i++) {
		mark=map.addMarker(new MarkerOptions()
		.title(result.get(i).getName())
		.position(
		new LatLng(result.get(i).getLatitude(), result
		.get(i).getLongitude()))
		.icon(BitmapDescriptorFactory
		.fromResource(R.drawable.pinplaces))
		.snippet(result.get(i).getVicinity()));

		}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			util.log("recorremos pre execute places");
			util.showDialog(context);
			
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... arg0) {
			PlacesService service = new PlacesService(
					"AIzaSyCo0GZPsPX3hZvSi8q31AUlzufu6SUymXU");
			ArrayList<Place> findPlaces = service.findPlaces(
					location.getLatitude(), location.getLongitude(), places);

			for (int i = 0; i < findPlaces.size(); i++) {

				Place placeDetail = findPlaces.get(i);
				util.log("places : " + placeDetail.getName());
			}
			return findPlaces;

		}

	}
	//gets data from database:
	private class MyData extends AsyncTask<Void, Integer, Boolean> {

		//private ProgressDialog dialog;
		private Context context;

		public MyData(Context context) {
			util.log("recorremos mydata");
			this.context = context;

		}

		protected void onPostExecute(Boolean result) {
			util.log("recorremos post execute mydata");
			final Intent info = new Intent(getApplicationContext(),
					OfferList.class);

			Query query = new Query("commerce");
			query.bounding("placelocation", latitudeSW, longitudeSW,
					latitudeNE, longitudeNE, 40, new FetchCallback() {

						@Override
						public void success(List<BackbeamObject> objects,
								int totalCount, boolean fromCache) {
							
							map.clear();
							//HashMap<String,String> refData= new HashMap<String,String>();
							final ArrayList<String>placeName=new ArrayList<String>();
							final ArrayList<String>idData=new ArrayList<String>();
							
							util.log("map clear mydata");
							for (final BackbeamObject object : objects) {
								util.log("1"+object.getId());
								//String desc = object.getId();
								placeName.add(object.getString("placename"));
								idData.add(object.getId());
								//refData.put(object.getString("placename"),object.getId());
								marker = map.addMarker(new MarkerOptions()
										.position(new LatLng(object.getLocation("placelocation").getLatitude(),
															 object.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(object.getString("placename"))
										.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.pin)));
								
								util.log("2"+object.getId());
								map.setOnMarkerClickListener(new OnMarkerClickListener() {
									public boolean onMarkerClick(Marker marker) {
										marker.showInfoWindow();
										for(int i=0;i<placeName.size();i++){
											if(marker.getTitle().equals(placeName.get(i))){
												finalId=idData.get(i);
												break;
											}
										}
										util.log("marcador mydata pulsado, id marcador:"+finalId+","+marker.getTitle()
											  + marker.getPosition() + "");
										return true;
									}
								});
								util.log("3"+object.getId());
								map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
									@Override
									public void onInfoWindowClick(Marker marker) {
										util.log("4"+object.getId());
										util.showToast(
												Map.this,
												"Marker\n" + marker.getTitle()
														+ "\n" + " pulsed.");
										util.log("titulo marcador mydata pulsado, id marcador:"+finalId+","+marker.getTitle());
										info.putExtra("id", finalId);
										startActivity(info);
									}
								});

							}
						}
					});
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			util.log("recorremos pre execute");
			util.showDialog(context);
			util.log("mostramos dialog mydata");
			

		}

		@Override
		protected Boolean doInBackground(Void... params) {
		
			util.log("doInBackgroundRecorrido mydata");

			return true;

		}

	}

}

	
