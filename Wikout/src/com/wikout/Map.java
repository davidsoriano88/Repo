package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.ObjectCallback;
import io.backbeam.Query;

import java.util.ArrayList;
import java.util.List;

import model.NavDrawerItem;
import utils.Place;
import utils.PlacesService;
import utils.Util;
import adapter.NavDrawerListAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class Map extends ActionBarActivity {
	
	Util util = new Util();
	private GoogleMap map;
	Marker markerBB, mark;
	String title, finalId, option,filter, searchResult;
	LocationManager locationManager;
	Location location;
	double longitudeSW, latitudeSW, longitudeNE, latitudeNE;
	int enter;
	LocationClient mLocationClient;
    Context context;  
    ImageView filterView; 
    TextView tvFilterText;
    ImageButton filterButton;
    EditText etSearch;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ArrayList<String>placeName=new ArrayList<String>(),idcommerce=new ArrayList<String>(),idcommerceonmap=new ArrayList<String>(),idMarker=new ArrayList<String>();
	protected AsyncTask<Void, Void, ArrayList<Place>> asyncPlaces;
	protected AsyncTask<Void, Integer, Boolean> asyncBackbeam;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	 super.onCreate(savedInstanceState);
	 supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	 setContentView(R.layout.fragment_main);
	 context=this;

     util.projectData(context);
     initUI();  
 	
	 }

	public void initUI(){
		
		setSupportProgressBarIndeterminateVisibility(true);
	
		 
	     //load slide menu items
		String[] navMenuTitles = getResources().getStringArray(R.array.options);     
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

	     ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();
	     LayoutInflater inflater = LayoutInflater.from(this);
	    
	    View search = inflater.inflate(R.layout.search_drawer, null);
	    etSearch=(EditText)search.findViewById(R.id.search1);
	    
	     mDrawerList.addHeaderView(search);
	    // navDrawerItems.add(new NavDrawerItem();
	     navDrawerItems.add(new NavDrawerItem(navMenuTitles[0]));
	     // Find People
	     navDrawerItems.add(new NavDrawerItem(navMenuTitles[1]));
	     // Photos
	     navDrawerItems.add(new NavDrawerItem(navMenuTitles[2]));
	     // Communities, Will add a counter here


	     // setting the nav drawer list adapter
	      
	     mDrawerList.setAdapter(new NavDrawerListAdapter(context,
	             navDrawerItems));
		//Inicializamos las variables
	  
	    map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
	
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    
	    filterView = (ImageView) findViewById(R.id.filterText);
	    tvFilterText=(TextView)findViewById(R.id.tvFilterText);
	    filterButton = (ImageButton) findViewById(R.id.filterButton);
	    filterVisible(false);
	    //tvFilterText.setTextColor(Color.WHITE);
	    //establecemos las opciones del menu deslizable:
	    mDrawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long arg3) {
				drawerOpener();
					switch(pos){
					case 0: break;
					case 1: 	
							final CharSequence[] items = { "Ocio",
							"Servicios", "Compras", "Otros"};

							AlertDialog.Builder builder = new AlertDialog.Builder(
							context);
							builder.setTitle("Filtrar por: ");
							builder.setItems(items, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int item) {
							// Do something with the selection
							switch (item) {
							case 0:
								util.showToast(context, "Ocio");filter="ocio"; break;
							case 1:
								util.showToast(context, "Servicios");filter="servicios"; break;
								
							case 2:
								util.showToast(context, "Compras");filter="compras"; break;
							case 3:
								util.showToast(context, "Otros");filter="otros"; break;		
							}
							tvFilterText.setText("Filtrado por: " + filter);
							getSupportActionBar().setTitle("Resultados");
							filterVisible(true);
							new MyData().execute();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
					searchResult=null;
						break;
					case 2: util.showInfoDialog(context, "Wikout", "Aplicacion desarrollada por Uptimiza. 2014"); break;
						
					case 3: android.os.Process.killProcess(android.os.Process.myPid());break;
					}
					
				}});
	    
	    etSearch.setOnKeyListener(new OnKeyListener() {
	        @Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
	            // If the event is a key-down event on the "enter" button
	            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
	                (keyCode == KeyEvent.KEYCODE_ENTER)) {
	              // Perform action on key press
	            	filter=null;
	            	searchResult=etSearch.getText().toString();
	            	InputMethodManager imm = (InputMethodManager)getSystemService(
	            		      Context.INPUT_METHOD_SERVICE);
	            		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
	              util.showToast(context,searchResult);
	              tvFilterText.setText("Buscando por: " + etSearch.getText());
					getSupportActionBar().setTitle("Resultados");
					filterVisible(true);
					new MyData().execute();
	              drawerOpener();
	              return true;
	            }
	            return false;
	        }
	    });
	    
		map.setMyLocationEnabled(true);
		filterButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				filter=null;
				searchResult=null;
				filterVisible(false);
				new MyData().execute();
				getSupportActionBar().setTitle("Wikout");
			}

		});
		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				util.showToast(context,"e");
			/*	Projection proj = map.getProjection();
				Point coord = proj.toScreenLocation(point);
				
				
		util.showToast(Map.this, "Click\n" + "Lat: " + point.latitude + "\n" + "Lng: "
						+ point.longitude + "\n" + "X: " + coord.x
						+ " - Y: " + coord.y);
				
		
		///establecemos el paso a la siguiente pantalla y le pasamos valores:
		Intent insert = new Intent(context, InsertCommerce.class);
		insert.putExtra("latiMain", point.latitude);
		insert.putExtra("longiMain", point.longitude);
		enter = 1;
		insert.putExtra("enter", enter);
		startActivity(insert);*/
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
				 asyncBackbeam = new MyData().execute();
				 asyncPlaces = new GetPlaces("").execute();
				 
			}
		});	
		setSupportProgressBarIndeterminateVisibility(false);
	}
	
	

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_next:
        	Intent insert = new Intent(context, InsertOffer.class);
			enter = 0;
			insert.putExtra("enter", enter);
			startActivity(insert);
            return true;
        
        	case android.R.id.home: 
        		drawerOpener();
        		return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	//configuracion visibilidad del filtro:
	public void filterVisible(Boolean visible){
		if (visible==false){
		filterButton.setVisibility(4);
	    tvFilterText.setVisibility(4);
	    filterView.setVisibility(4);
		}else{
		filterButton.setVisibility(0);
	    tvFilterText.setVisibility(0);
	    filterView.setVisibility(0);}
	}
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
	    switch(keycode) {
	        case KeyEvent.KEYCODE_MENU:
	        	drawerOpener();
        		return true;
	   
	    }

	    return super.onKeyDown(keycode, e);
	}
	
	//comprueba si el menu está abierto o cerrado y lo abre o cierra en consecuencia:
	public void drawerOpener(){
		if(mDrawerLayout.isDrawerOpen(mDrawerList)){
		mDrawerLayout.closeDrawer(mDrawerList);}
		else{
		mDrawerLayout.openDrawer(mDrawerList);}
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
	
	//gets data from google places:
	private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

		private String places;
		
		public GetPlaces(String places) {
			util.log("recorremos getplaces");
			
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
		//setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			util.log("recorremos pre-execute places");
			//util.showProgressDialog(context);	
			//setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... arg0) {
			PlacesService service = new PlacesService(
					"AIzaSyCo0GZPsPX3hZvSi8q31AUlzufu6SUymXU");
			ArrayList<Place> findPlaces = service.findPlaces(
					location.getLatitude(), location.getLongitude(), places);
			util.log("placesprueba1"+location.getLatitude()+location.getLongitude()+places);
				
			for (int i = 0; i < findPlaces.size(); i++) {

				Place placeDetail = findPlaces.get(i);
				util.log("places : " + placeDetail.getName());
			}
			return findPlaces;
		}
	}

	//gets data from Backbeam:
	private class MyData extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			
			util.log("recorremos post execute mydata");
			util.log("filtro: "+filter+" busqueda: "+searchResult);
			if(filter==null&&searchResult==null){
			standardQuery();
			}else if(searchResult==null){
			filterQuery(filter);}else{ commercesOnMap(searchResult);}
			
			  
			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			/*util.log("recorremos pre execute");
			util.showProgressDialog(context);
			util.log("mostramos dialog mydata");*/
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			util.log("doInBackgroundRecorrido mydata");

			return true;
		}
	}

	
		
	
	public void standardQuery(){

		final Intent info = new Intent(context,
				OfferList.class);
		
		
		Query query = new Query("commerce");
		query.bounding("placelocation", latitudeSW, longitudeSW,
				latitudeNE, longitudeNE, 40, new FetchCallback() {

					@Override
					public void success(List<BackbeamObject> objects,
							int totalCount, boolean fromCache) {
						
						map.clear();
						/*placeName.clear();						
						idcommerce.clear();
						idMarker.clear();
						
						Bitmap.Config conf = Bitmap.Config.ARGB_8888; 
						Bitmap bmp = Bitmap.createBitmap(200, 50, conf); 
						Canvas canvas = new Canvas(bmp);
						Paint paint = new Paint();
						paint.setColor(Color.BLACK);
						
						canvas.drawText("TEXT", 0.1F, 0.1F, paint);*/
						
						
						util.log("map clear mydata");
						for (final BackbeamObject object : objects) {
							util.log("1"+object.getId());
							placeName.add(object.getString("placename"));
							idcommerce.add(object.getId());
							switch(object.getString("category")){
							case("ocio"):
							markerBB = map.addMarker(new MarkerOptions()
									.position(new LatLng(object.getLocation("placelocation").getLatitude(),
														 object.getLocation("placelocation").getLongitude()))
									.draggable(false)
									.title(object.getString("placename"))
									.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinazul, object.getNumber("numbubble").toString()))));
									/*.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.pinazul)));*/
							break;
							case("servicios"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(object.getLocation("placelocation").getLatitude(),
															 object.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(object.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinmorado, object.getNumber("numbubble").toString()))));
							break;
							case("compras"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(object.getLocation("placelocation").getLatitude(),
															 object.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(object.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinrosa, object.getNumber("numbubble").toString()))));
							break;
							case("otros"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(object.getLocation("placelocation").getLatitude(),
															 object.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(object.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinverde, object.getNumber("numbubble").toString()))));
							break;
							default: break;
							}
							idMarker.add(markerBB.getId());
							util.log("2"+object.getId());
							map.setOnMarkerClickListener(new OnMarkerClickListener() {
								@Override
								public boolean onMarkerClick(Marker marker) {
									marker.showInfoWindow();
									for(int i=0;i<placeName.size();i++){
										if(marker.getTitle().equals(placeName.get(i))){
											finalId=idcommerce.get(i);
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
									for(int i=0;i<idMarker.size();i++){
									if(idMarker.get(i).contains(marker.getId())){
									util.log("titulo marcador mydata pulsado, id marcador:"+finalId+","+marker.getTitle());
									info.putExtra("id", finalId);
									startActivity(info);
									}
								}
								}
							});

						}
						setSupportProgressBarIndeterminateVisibility(false);
					}
				});
		
	}
	private void commercesOnMap(final String parameter) {
		final Intent info = new Intent(context,
				OfferList.class);
		map.clear();
		//Nombre Comercio
		placeName.clear();
		//Id Comercio
		idcommerce.clear();
		//Id Marcador
		idMarker.clear();
		//ID comercios encuadrados
		idcommerceonmap.clear();

		util.log("fff ha entrado en la funcion busqueda(1)");
		Query queryCommerce = new Query("commerce");
		queryCommerce.setQuery("where placename like ?", parameter)
				.fetch(100, 0, new FetchCallback() {
					@Override
					public void success(List<BackbeamObject> objects,
							int totalCount, boolean fromCache) {
						System.out.println("Numero de comercios con '"+parameter+"': "+totalCount);
						for (BackbeamObject commerce : objects) {
							if (commerce.getLocation("placelocation").getLatitude() < latitudeNE
								&& commerce.getLocation("placelocation").getLatitude() > latitudeSW
								&& commerce.getLocation("placelocation").getLongitude() < longitudeNE
								&& commerce.getLocation("placelocation").getLongitude() > longitudeSW) {
								// METO LOS IDCOMMERCE EN UN ARRAY
								idcommerce.add(commerce.getId());
								util.log("fff ha entrado en el success busqueda(2)");
							}
						}
					}

				});
		Query queryOffer = new Query("offer");
		queryOffer.setQuery("where description like ? join commerce", parameter)
		.fetch(100, 0, new FetchCallback() {
			@Override
			public void success(List<BackbeamObject> objects,
					int totalCount, boolean fromCache) {
				util.log("fff ha entrado en el success oferta(3)");
				System.out.println("Numero de ofertas con '"+parameter+"': "+totalCount);
				for (BackbeamObject offer : objects) {
					BackbeamObject commerce = offer.getObject("commerce");
					if (commerce.getLocation("placelocation").getLatitude() < latitudeNE
						&& commerce.getLocation("placelocation").getLatitude() > latitudeSW
						&& commerce.getLocation("placelocation").getLongitude() < longitudeNE
						&& commerce.getLocation("placelocation").getLongitude() > longitudeSW) {
								// COMPRUEBO SI EL ID YA ESTA DENTRO DEL ARRAY
								if(!idcommerce.contains(commerce.getId())){
									idcommerce.add(commerce.getId());
									util.log("fff comprueba bien (4)");
								}
							}
						}
				
				//CREAR MARCADOR EN EL MAPA (cambiar for por el de abajo)
				for(String commerce: idcommerce){
					Backbeam.read("commerce", commerce, new ObjectCallback() {
					@Override
					public void success(final BackbeamObject commerceMark) {
						//CREAR MARCADOR
						util.log("fff creamos marcadores (5)");
						
							util.log("1 "+commerceMark.getId());

							
							switch(commerceMark.getString("category")){
							case("ocio"):
							markerBB = map.addMarker(new MarkerOptions()
									.position(new LatLng(commerceMark.getLocation("placelocation").getLatitude(),
											commerceMark.getLocation("placelocation").getLongitude()))
									.draggable(false)
									.title(commerceMark.getString("placename"))
									.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinazul, commerceMark.getNumber("numbubble").toString()))));
							break;
							case("servicios"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(commerceMark.getLocation("placelocation").getLatitude(),
															 commerceMark.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(commerceMark.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinmorado, commerceMark.getNumber("numbubble").toString()))));
							break;
							case("compras"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(commerceMark.getLocation("placelocation").getLatitude(),
															 commerceMark.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(commerceMark.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinrosa, commerceMark.getNumber("numbubble").toString()))));
							break;
							case("otros"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(commerceMark.getLocation("placelocation").getLatitude(),
															 commerceMark.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(commerceMark.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinverde, commerceMark.getNumber("numbubble").toString()))));
							break;
							default: break;
							}
							placeName.add(commerceMark.getString("placename"));
							idcommerceonmap.add(commerceMark.getId());
							idMarker.add(markerBB.getId());
							util.log("2 "+commerceMark.getId()+"   "+ commerceMark.getString("placename"));
							map.setOnMarkerClickListener(new OnMarkerClickListener() {
								@Override
								public boolean onMarkerClick(Marker marker) {
									marker.showInfoWindow();
									for(int i=0;i<placeName.size();i++){
										if(marker.getTitle().equals(placeName.get(i))){
											finalId=idcommerceonmap.get(i);
											break;
										}
									}
									util.log("marcador mydata pulsado, id marcador:"+finalId+","+marker.getTitle()
										  + marker.getPosition() + "");
									return true;
								}
							});
							util.log("3 "+commerceMark.getId() +"   "+ commerceMark.getString("placename"));
							map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
								@Override
								public void onInfoWindowClick(Marker marker) {
									
									for(int i=0;i<placeName.size();i++){
									if(marker.getTitle().equals(placeName.get(i))){
									finalId=idcommerceonmap.get(i);
									util.log("titulo marcador mydata pulsado, id marcador:"+marker.getId()+","+marker.getTitle());
									info.putExtra("id", finalId);
									startActivity(info);
									}
								}
								}
							});

						
					}});
				}setSupportProgressBarIndeterminateVisibility(false);
				
					}

				});


}
	
	public void filterQuery(final String filter){
		final Intent info = new Intent(context,
				OfferList.class);
		placeName.clear();
		idcommerce.clear();
		idMarker.clear();
		Query query = new Query("commerce");
		query.bounding("placelocation", latitudeSW, longitudeSW,
				latitudeNE, longitudeNE, 40, new FetchCallback() {

					@Override
					public void success(List<BackbeamObject> objects,
							int totalCount, boolean fromCache) {
						
						map.clear();
						
						util.log("map clear mydata");
						for (final BackbeamObject object : objects) {
						if(object.getString("category").equals(filter)==true){
							util.log("1"+object.getId());
							placeName.add(object.getString("placename"));
							idcommerce.add(object.getId());
							
							switch(object.getString("category")){
							case("ocio"):
							markerBB = map.addMarker(new MarkerOptions()
									.position(new LatLng(object.getLocation("placelocation").getLatitude(),
														 object.getLocation("placelocation").getLongitude()))
									.draggable(false)
									.title(object.getString("placename"))
									.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinazul, object.getNumber("numbubble").toString()))));
							break;
							case("servicios"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(object.getLocation("placelocation").getLatitude(),
															 object.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(object.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinmorado, object.getNumber("numbubble").toString()))));
							break;
							case("compras"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(object.getLocation("placelocation").getLatitude(),
															 object.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(object.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinrosa, object.getNumber("numbubble").toString()))));
							break;
							case("otros"):
								markerBB = map.addMarker(new MarkerOptions()
										.position(new LatLng(object.getLocation("placelocation").getLatitude(),
															 object.getLocation("placelocation").getLongitude()))
										.draggable(false)
										.title(object.getString("placename"))
										.icon(BitmapDescriptorFactory.fromBitmap(util.writeTextOnDrawable(context,R.drawable.pinverde, object.getNumber("numbubble").toString()))));
							break;
							default: break;
							}
							idMarker.add(markerBB.getId());
							util.log("2"+object.getId());
							map.setOnMarkerClickListener(new OnMarkerClickListener() {
								@Override
								public boolean onMarkerClick(Marker marker) {
									marker.showInfoWindow();
									for(int i=0;i<placeName.size();i++){
										if(marker.getTitle().equals(placeName.get(i))){
											finalId=idcommerce.get(i);
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
									for(int i=0;i<idMarker.size();i++){
									if(idMarker.get(i).contains(marker.getId())){
									util.log("titulo marcador mydata pulsado, id marcador:"+finalId+","+marker.getTitle());
									info.putExtra("id", finalId);
									startActivity(info);
									}
								}
								}
							});
						}
					}setSupportProgressBarIndeterminateVisibility(false);}
					
				});
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	//METODO PARA MOSTRAR NUMLIKES en el PIN 
		protected void commerceNumlike() {
			System.out.println("dentro de coordenadas");
			Query query = new Query("commerce");
			//BUSCO COMERCIOS POR COORDENADAS
			query.bounding("placelocation", latitudeSW, longitudeSW, latitudeNE,longitudeNE, 40, new FetchCallback() {
				@Override
				public void success(List<BackbeamObject> commerces,int totalCount, boolean fromCache) {
					//RECORRO CADA COMERCIO 
					for (BackbeamObject commerce : commerces) {
						//CON ESTO SE OBTIENE EL NUMERO DE LIKES TOTAL DE CADA COMERCIO
						commerce.getNumber("numbubble").intValue();
					}
				}

			});
		}
		@Override
		protected void onRestart() {
		    super.onRestart();
		    new MyData().execute();
		    }
}
