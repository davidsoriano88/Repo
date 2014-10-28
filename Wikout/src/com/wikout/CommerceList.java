package com.wikout;

import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.Location;
import io.backbeam.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.wikout.OfferList.LazyAdapter;

import model.FontUtils;
import utils.ImageLoader;
import utils.Place;
import utils.PlacesService;
import utils.Util;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CommerceList extends ActionBarActivity {

	Context context = this;
	ListView listview;

	ActionBar ab;
	Activity act = this;


	private LazyAdapter lazyadapter;
	ListView list;


	TextView tvOffer, tvLike, tvId, tvDeadline, tvNoOffer;

	LazyAdapter adapter;
	double latitude, longitude;

	static final int REQUESTCODEMAP = 101;
	static final int REQUESTCODECOMMERCE = 102;
	public static final int RESULTOK = 100;

	public static ActionBarActivity actionbarAct;
	final ArrayList<String> listPlacenameCommerces = new ArrayList<String>();
	final ArrayList<String> listIdCommerces = new ArrayList<String>();
	final ArrayList<String> listLocationCommerces = new ArrayList<String>();
	final ArrayList<String> listIconId = new ArrayList<String>();
	final ArrayList<String> listCategory = new ArrayList<String>();
	final ArrayList<String> listGoogleId = new ArrayList<String>();
	
	final ArrayList<Double> listLocationLatitude = new ArrayList<Double>();
	final ArrayList<Double> listLocationLongitude = new ArrayList<Double>();
	
	static final String KEY_ID = "id";
	static final String KEY_PLACENAME = "placename";
	static final String KEY_LOCATION = "location";
	static final String KEY_ICONID = "iconid";
	static final String KEY_CATEGORY = "category";
	static final String KEY_GOOGLEID = "googleid";
	static final String KEY_LOCATIONLAT = "latitude";
	static final String KEY_LOCATIONLON = "longitude";
	
	final Util util = new Util();

	private AsyncTask<Void, Void, ArrayList<Place>> asyncPlaces;
	final ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.commerce_list);
		getSupportActionBar().setTitle("Comercios cercanos");
		setSupportProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listview = (ListView) findViewById(R.id.listcommerce);

		actionbarAct = this;
		context = this;

		FontUtils.setRobotoFont(context, ((Activity) context).getWindow()
				.getDecorView());

		util.projectData(context);

		Bundle location = getIntent().getExtras();// ***************atencion

		if (location.getInt("enter") == 1) {
			util.log("entra dentro del if del commerceList");
			Intent mapv2 = new Intent(this, Mapv2.class);
			startActivityForResult(mapv2, REQUESTCODEMAP);
			// finish();
		} else {
			util.log("entra tras el if del commerceList");
			latitude = location.getDouble("pointlat");
			longitude = location.getDouble("pointlon");
			System.out.println("Commerce list: " + latitude + " " + longitude);
			getBoundingLocation(latitude, longitude);
		}

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				if (util.isNetworkAvailable(context) == true) {
					if (position == listPlacenameCommerces.size() - 1) {
						Intent insertCommerce = new Intent(context,
								InsertCommerce.class);

						insertCommerce.putExtra("pointlat", latitude);
						insertCommerce.putExtra("pointlon", longitude);
						startActivityForResult(insertCommerce,
								REQUESTCODECOMMERCE);
						// finish();
					} else {
						Intent insertOffer = new Intent();
						insertOffer.putExtra("latitude", latitude);
						insertOffer.putExtra("longitude", longitude);
						insertOffer.putExtra("placename",
								listPlacenameCommerces.get(position));
						insertOffer.putExtra("idcommerce",
								listIdCommerces.get(position));
						insertOffer.putExtra("location",
								listLocationCommerces.get(position));
						insertOffer.putExtra("iconlink",
								listIconId.get(position));
						insertOffer.putExtra("googleid",
								listGoogleId.get(position));
						insertOffer.putExtra("latitude",
								listLocationLatitude.get(position));
						insertOffer.putExtra("longitude",
								listLocationLongitude.get(position));

						insertOffer.putExtra("category",
								listCategory.get(position));

						System.out.println("location intent: "
								+ listLocationCommerces.get(position)
								+ "category intent: "
								+ listCategory.get(position) + "latitude: "
								+ latitude + "longitude: " + longitude);

						setResult(RESULT_OK, insertOffer);
						// startActivity(insertOffer);
						finish();

					}
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexión a internet");
				}

			}

		});
	}

	// load image in imageView
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {

		}
		if (requestCode == REQUESTCODEMAP) {
			if (resultCode == RESULT_OK) {
				System.out.println("entra en result code");
				Bundle d = data.getExtras();
				latitude = d.getDouble("pointlat");
				longitude = d.getDouble("pointlon");
				getBoundingLocation(latitude, longitude);
			}

		}
		if (requestCode == REQUESTCODECOMMERCE && resultCode == RESULT_OK) {

			Bundle d = data.getExtras();
			String placename = d.getString("placename");
			String idcommerce = d.getString("idcommerce");
			String location = d.getString("location");
			double longitude = d.getDouble("commercelon");
			double latitude = d.getDouble("commercelat");

			System.out.println("la location es: " + d.getString("location"));

			Intent i = new Intent();
			i.putExtra("location", location);
			i.putExtra("placename", placename);
			i.putExtra("idcommerce", idcommerce);
			i.putExtra("latitude", latitude);
			i.putExtra("longitude", longitude);
			// i.putExtra("iconlink", )
			setResult(RESULT_OK, i);

			finish();
		}

	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
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

	// METODO para localizar comercios cercanos respecto a las coordenadas del
	// usuario
	protected void getBoundingLocation(final double userlat,
			final double userlon) {

		util.log("coordenadas: " + userlat + ", " + userlon);
		// Vacio los arraylists
		listPlacenameCommerces.clear();
		listIdCommerces.clear();
		listLocationCommerces.clear();
		listIconId.clear();
		listGoogleId.clear();
		listLocationLatitude.clear();
		listLocationLongitude.clear();

		listCategory.clear();

		// Declaro los double de las coordenadas
		double latNE, lonNE, latSW, lonSW;
		// Calculo el valor de cada parámetro de la consulta de bounding
		latNE = userlat + 0.0019072;
		lonNE = userlon + 0.002518;
		latSW = userlat - 0.0019072;
		lonSW = userlon - 0.002518;

		Query query = new Query("commerce");
		// BUSCO COMERCIOS POR COORDENADAS
		query.bounding("placelocation", latSW, lonSW, latNE, lonNE, 40,
				new FetchCallback() {

					@Override
					public void success(List<BackbeamObject> commerces,
							int totalCount, boolean fromCache) {

						// Vacio los arraylists
						listPlacenameCommerces.clear();
						listIdCommerces.clear();
						listLocationCommerces.clear();
						listIconId.clear();
						listGoogleId.clear();
						listLocationLatitude.clear();
						listLocationLongitude.clear();

						listCategory.clear();

						// RECORRO CADA COMERCIO
						if (totalCount == 0) {
							asyncPlaces = new GetPlaces("").execute();
							/*
							 * Intent insertCommerce = new Intent(context,
							 * InsertCommerce.class);
							 * insertCommerce.putExtra("pointlat", latitude);
							 * insertCommerce.putExtra("pointlon", longitude);
							 * startActivityForResult(insertCommerce,
							 * REQUESTCODECOMMERCE);
							 */
						} else {
							for (BackbeamObject commerce : commerces) {
								// CREAR ITEMS PARA LA LISTA
								util.log(String.valueOf(totalCount));
								listPlacenameCommerces.add(commerce
										.getString("placename"));
								listIdCommerces.add(commerce.getId());
								listLocationCommerces.add(commerce
										.getLocation("placelocation")
										.getAddress().toString());
								listGoogleId.add(commerce.getString("googleid"));
								System.out.println("Commercelist listlocationcommerce: "+commerce
										.getLocation("placelocation")
										.getAddress().toString());
								//listLocationLatitude.add(commerce.getLocation("placelocation").getAddress();
								//listLocationLongitude.clear();
								listIconId.add("null");
								listCategory.add("null");
								System.out.println(commerce
										.getLocation("placelocation")
										.getAddress().toString());
								
								HashMap<String, String> map = new HashMap<String, String>();
								
								map.put(KEY_PLACENAME,commerce
										.getString("placename"));
								
								map.put(KEY_GOOGLEID, commerce.getString("googleid"));
								
								map.put(KEY_LOCATION,commerce
										.getLocation("placelocation")
										.getAddress().toString());
								
								map.put(KEY_ID,commerce.getId());
								map.put(KEY_LOCATIONLAT, String.valueOf(commerce
										.getLocation("placelocation")
										.getLatitude()));
								map.put(KEY_LOCATIONLON,String.valueOf(commerce
										.getLocation("placelocation")
										.getLongitude()));
								map.put(KEY_ICONID, "null");
								map.put(KEY_CATEGORY,"null");
								// adding HashList to ArrayList
								songsList.add(map);

							}
							asyncPlaces = new GetPlaces("").execute();

						}
					}

				});

	}

	/*
	 * // creating Places class object placesService =new
	 * PlacesService(getResources().getString(R.string.google_api_key));
	 * 
	 * 
	 * try { // Separeate your place types by PIPE symbol "|" // If you want all
	 * types places make it as null // Check list of types supported by google
	 * // String types = null; // Listing places only cafes, restaurants
	 * 
	 * // Radius in meters - increase this value if you don't find any places
	 * double radius = 300; // 300 meters
	 * 
	 * // get nearest places nearPlaces = placesService.search(latitude,
	 * longitude, radius, types);
	 * 
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } // Get json response
	 * status String status = nearPlaces.status;
	 * 
	 * // Check for all possible status if(status.equals("OK")){ // Successfully
	 * got places details if (nearPlaces.results != null) { // loop through each
	 * place for (Place p : nearPlaces.results) { HashMap<String, String> map =
	 * new HashMap<String, String>();
	 * 
	 * // Place reference won't display in listview - it will be hidden // Place
	 * reference is used to get "place full details"
	 * 
	 * listPlacenameCommerces.add(p.name); listIdCommerces.add(p.reference);
	 * System
	 * .out.println("Detalles de place (reference): "+p.reference.toString());
	 * listLocationCommerces.add(p.formatted_address.toString());
	 * 
	 * 
	 * // adding HashMap to ArrayList placesListItems.add(map); }}}
	 */

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
						final HashMap<String, String> mapnew = new HashMap<String, String>();
			for (int i = 0; i < result.size(); i++) {
				if (listGoogleId.contains(result.get(i).getPlaceID()) != true) {
					System.out.println("LOCAL NUMERO " + i + ", valores: "
							+ result.get(i));
					HashMap<String, String> map = new HashMap<String, String>();
					listPlacenameCommerces.add(result.get(i).getName());
					map.put(KEY_PLACENAME, result.get(i).getName());
					// Location loc = new Location(result.get(i).getLatitude(),
					// +
					// result.get(i).getLongitude(),
					// result.get(i).getVicinity());
					listGoogleId.add(result.get(i).getPlaceID());
					map.put(KEY_GOOGLEID, result.get(i).getPlaceID());
					listLocationCommerces.add("Direccion: "
							+ result.get(i).getVicinity() + ";Latitud:"
							+ result.get(i).getLatitude() + ";Longitud:"
							+ result.get(i).getLongitude());
					map.put(KEY_LOCATION, "Direccion: "
							+ result.get(i).getVicinity() + ";Latitud:"
							+ result.get(i).getLatitude() + ";Longitud:"
							+ result.get(i).getLongitude());
					listIdCommerces.add(result.get(i).getPlaceID());
					map.put(KEY_ID, result.get(i).getPlaceID());
					listLocationLatitude.add(result.get(i).getLatitude());
					map.put(KEY_LOCATIONLAT, result.get(i).getLatitude().toString());
					listLocationLongitude.add(result.get(i).getLongitude());
					map.put(KEY_LOCATIONLON, result.get(i).getLongitude().toString());
					listIconId.add(result.get(i).getIcon());
					map.put(KEY_ICONID, result.get(i).getIcon());
					listCategory.add(result.get(i).getType().toString());
					map.put(KEY_CATEGORY,result.get(i).getType().toString());
					// adding HashList to ArrayList
					songsList.add(map);
				}
			}
			
			
			listPlacenameCommerces.add("NUEVO");
			mapnew.put(KEY_PLACENAME,"NUEVO");
			listIdCommerces.add("null");
			mapnew.put(KEY_ID,"null");
			listLocationCommerces.add("null");
			mapnew.put(KEY_LOCATION,"null");
			listIconId.add("null");
			mapnew.put(KEY_ICONID,"null");
			listCategory.add("");
			mapnew.put(KEY_CATEGORY,"");
			listGoogleId.add("");
			mapnew.put(KEY_GOOGLEID,"");
			listLocationLongitude.add(longitude);
			mapnew.put(KEY_LOCATIONLON,String.valueOf(longitude));
			listLocationLatitude.add(latitude);
			mapnew.put(KEY_LOCATIONLAT,String.valueOf(latitude));
			songsList.add(mapnew);
			/*StableArrayAdapter adapter = new StableArrayAdapter(context,
					android.R.layout.simple_list_item_1, listPlacenameCommerces);
			listview.setAdapter(adapter);
			*/
			// Getting adapter by passing data ArrayList
						lazyadapter = new LazyAdapter(act, songsList);
						listview.setAdapter(lazyadapter);
						
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
			ArrayList<Place> findPlaces = service.findPlaces(latitude,
					longitude, places);
			// ArrayList<Place> findPlaces = service.search(latitudeSplash,
			// longitudeSplash, 300, null);
			// System.out.println("placesprueba1: " + location.getLatitude()+
			// location.getLongitude());
			System.out.println("DENTRO DE GETPLACES");

			/*
			 * for (int i = 0; i < findPlaces.size(); i++) {
			 * 
			 * Place placeDetail = findPlaces.get(i);
			 * System.out.println("places : " + placeDetail.getName());
			 * listPlacenameCommerces.add(placeDetail.getName());
			 * 
			 * listIdCommerces.add(placeDetail.getPlaceID()); }
			 */
			return findPlaces;
		}
	}
	public class LazyAdapter extends BaseAdapter {

		private Activity activity;
		private ArrayList<HashMap<String, String>> data;
		private LayoutInflater inflater = null;
		public ImageLoader imageLoader;
		private TextView tvCommerce;

		public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
			activity = a;
			data = d;
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			imageLoader = new ImageLoader(activity.getApplicationContext());

		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.commerce_list_item, null);
			String fontPath = "fonts/Roboto-Regular.ttf";
			String fontPathLight = "fonts/Roboto-Light.ttf";

			// get the font face
			Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
			Typeface tfl = Typeface.createFromAsset(getAssets(), fontPathLight);

			// Apply the font
			tvCommerce = (TextView) convertView
					.findViewById(R.id.tvCommerceListOfferDescription);
			tvCommerce.setTypeface(tf);
			HashMap<String, String> commerceHashMap = new HashMap<String, String>();
			commerceHashMap = data.get(position);
			tvCommerce.setText(commerceHashMap.get(KEY_PLACENAME));


			
			setSupportProgressBarIndeterminateVisibility(false);
			return convertView;
		}
	}

}