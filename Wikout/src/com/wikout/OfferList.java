package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.CollectionConstraint;
import io.backbeam.FetchCallback;
import io.backbeam.JoinResult;
import io.backbeam.Query;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import model.FontUtils;
import utils.ImageLoader;
import utils.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OfferList extends ActionBarActivity {

	private ArrayList<String> dataid = new ArrayList<String>();

	ActionBar ab;
	Activity act = this;
	Context context = this;

	Util util = new Util();

	Bitmap bmPhoto = null;
	ImageView ivPhoto;
	// static TextView tvLocation;
	ListView list;
	Button btnAdd;

	TextView tvOffer, tvLike, tvId, tvDeadline, tvNoOffer, tvSms;

	LazyAdapter adapter;

	String idcommerce = "", placeName, userlocation, commercelocation;
	double latitude, longitude, userlat, userlon;
	// XML node keys

	static final String KEY_ID = "id";
	static final String KEY_THUMB_URL = "thumb_url";
	static final String KEY_DESCRIPTION = "description";
	static final String KEY_LIKES = "likes";
	static final String KEY_DEADLINE = "deadline";

	public class LazyAdapter extends BaseAdapter {

		private Activity activity;
		private ArrayList<HashMap<String, String>> data;
		private LayoutInflater inflater = null;
		public ImageLoader imageLoader;

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
			convertView = inflater.inflate(R.layout.offer_list_item, null);
			String fontPath = "fonts/Roboto-Black.ttf";
			String fontPathLight = "fonts/Roboto-Light.ttf";

			// get the font face
			Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
			Typeface tfl = Typeface.createFromAsset(getAssets(), fontPathLight);

			// Apply the font
			tvOffer = (TextView) convertView
					.findViewById(R.id.tvOfferListOfferDescription);
			tvOffer.setTypeface(tf);
			tvLike = (TextView) convertView
					.findViewById(R.id.tvOfferListOfferNumlike);
			tvLike.setTypeface(tfl);
			tvDeadline = (TextView) convertView
					.findViewById(R.id.tvOfferListOfferDeadline);
			tvDeadline.setTypeface(tfl);
			

			HashMap<String, String> offerHashMap = new HashMap<String, String>();
			offerHashMap = data.get(position);

			// Setting all values in listview
			if (offerHashMap.get(KEY_DESCRIPTION).length() > 68) {
				tvOffer.setText(offerHashMap.get(KEY_DESCRIPTION).substring(0,
						60)
						+ "...");
			} else {
				tvOffer.setText(offerHashMap.get(KEY_DESCRIPTION));
			}

			tvLike.setText(offerHashMap.get(KEY_LIKES) + " "
					+ getResources().getString(R.string.heartofferlist));
			tvDeadline.setText("V�lido hasta: "
					+ offerHashMap.get(KEY_DEADLINE));

			/*
			 * if (song.get(KEY_DEADLINE) != null) { URL newurl = null;
			 * 
			 * try { newurl = new URL(song.get(KEY_THUMB_URL)); } catch
			 * (MalformedURLException e1) { // TODO Auto-generated catch block
			 * e1.printStackTrace(); } try { bmPhoto =
			 * BitmapFactory.decodeStream(newurl
			 * .openConnection().getInputStream()); } catch (IOException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); } } else {
			 */

			System.out.println("items cargandose");
			setSupportProgressBarIndeterminateVisibility(false);
			return convertView;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.offer_list);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();

		StrictMode.setThreadPolicy(policy);
		System.out.println("El getDisplayLanguage (espa�ol) instalado es: "
				+ Locale.getDefault().getDisplayLanguage() + "\n"
				+ "El getcountry (ES) instalado es: "
				+ Locale.getDefault().getCountry() + "\n"
				+ "El getLanguage (es) instalado es: "
				+ Locale.getDefault().getLanguage() + "\n"
				+ "El getISO3Language (spa) instalado es: "
				+ Locale.getDefault().getISO3Language());

		initUi();

	}

	private void initUi() {
		setSupportProgressBarIndeterminateVisibility(true);
		Bundle bundle = getIntent().getExtras();
		// ivPhoto = new ImageView(this);
		btnAdd = (Button) findViewById(R.id.btnAddOffer);
		tvNoOffer = (TextView) findViewById(R.id.tvListOfferNoOffer);
		list = (ListView) findViewById(R.id.listOff);
		tvSms = (TextView) findViewById(R.id.tvOfferListSms);
		tvSms.setVisibility(View.GONE);
		// queryOffer(bundle.getString("id"));

		if (bundle.getString("commerceid") != null) {
			idcommerce = bundle.getString("commerceid");
			userlat = Util.getPreferenceDouble(context, "latpos");
			userlon = Util.getPreferenceDouble(context, "longpos");

		} else {
			util.log(bundle.getString("id"));
			idcommerce = bundle.getString("id");

			// userlocation = bundle.getString("location");
			userlat = bundle.getDouble("userlatitude");
			userlon = bundle.getDouble("userlongitude");

		}

		// getPhoto(bundle.getString("id"));
		FontUtils.setRobotoFont(context, (ViewGroup) ((Activity) context)
				.getWindow().getDecorView());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		new LoadDataTask().execute();
		

		btnAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (util.isNetworkAvailable(context) == true) {
					AlertDialog.Builder info = new AlertDialog.Builder(context);
					info.setTitle("Insertar Nueva Oferta");
					info.setMessage("Est�s a punto de insertar una nueva oferta");
					info.setCancelable(true);
					info.setNeutralButton("Aceptar",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogo1,
										int id) {
									Intent addOffer = new Intent(context,
											InsertOffer.class);
									addOffer.putExtra("idcommerce", idcommerce);
									addOffer.putExtra("placename", placeName);
									addOffer.putExtra("location",
											commercelocation);
									startActivity(addOffer);
								}

							});

					info.show();

				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexi�n a internet");
				}

			}

		});
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

				Intent intent = new Intent(context, ViewOffer.class);
				intent.putExtra("idoffer", dataid.get(pos));
				intent.putExtra("idcommerce", idcommerce);
				intent.putExtra("placename", placeName);
				// intent.putExtra("userlocation", userlocation);
				intent.putExtra("userlongitude", userlon);
				intent.putExtra("userlatitude", userlat);
				startActivity(intent);

			}
		});

	}

	public String getId() {
		String id = android.provider.Settings.System.getString(
				super.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return id;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/*
		 * case R.id.refresh: //new LoadDataTask().execute(); /*Intent intent =
		 * getIntent(); finish(); startActivity(intent); return true;
		 */
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Async Task para cargar datos al abrir la activity
	private class LoadDataTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			util.projectData(context);
			util.log("recorremos post execute mydata");
			// dataid.clear();
			CollectionConstraint collection = new CollectionConstraint();
			collection.addIdentifier(idcommerce);

			ivPhoto = (ImageView) findViewById(R.id.ivOfferListCommercePhoto);

			Query query = new Query("commerce");
			query.setQuery("where this in ? join file", collection);
			query.fetch(100, 0, new FetchCallback() {
				@Override
				public void success(List<BackbeamObject> companies,
						int totalCount, boolean fromCache) {

					for (BackbeamObject company : companies) {
						System.out.println("foto comercio");
						BackbeamObject fileObject = company.getObject("file");
						if (fileObject != null) {
							TreeMap<String, Object> options = new TreeMap<String, Object>();
							options.put("width", 120);
							options.put("height", 40);
							String logoURL = fileObject.composeFileURL(options);

							// Codigo para poner la foto en el imageView

							try {
								URL newurl = null;
								newurl = new URL(logoURL);

								bmPhoto = BitmapFactory.decodeStream(newurl
										.openConnection().getInputStream());
								util.log("icono cargado");

								// image.setImageBitmap(mIcon_val);
							} catch (IOException e) {
								e.printStackTrace();
							}

							ivPhoto.setImageBitmap(bmPhoto);
						} else {
							// ivPhoto.getLayoutParams().height = 0;

							ivPhoto.setVisibility(View.GONE);
							// ivPhoto.setImageDrawable(getResources().getDrawable(
							// R.drawable.nophoto));

						}
					}
				}
			});

			// queryOffer(idcommerce);

			final ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
			final Date today = util.actualDate();

			/*
			 * Query query = new Query("commerce");
			 * query.setQuery("where this in ? join last 100 offer",
			 * collection); query.
			 */

			Backbeam.select("commerce")
					.setQuery("where this in ? join last 100 offer", collection)
					.fetch(100, 0, new FetchCallback() {

						@Override
						public void success(List<BackbeamObject> objects,
								int totalCount, boolean fromCache) {

							System.out.println("total objects   "
									+ objects.size());
							if (objects.size() == 0) {
								System.out.println("no pilla el array");
							}
							BackbeamObject commerce = objects.get(0);
							JoinResult join = commerce.getJoinResult("offer");
							placeName = commerce.getString("placename");
							commercelocation = commerce
									.getLocation("placelocation").getAddress()
									.toString();
							getSupportActionBar().setTitle(placeName);
							List<BackbeamObject> offers = join.getResults();
							// Contemplo si alguna referencia NO TIENE ofertas
							if (offers.size() == 0) {
								// No hay ofertas disponibles
								setSupportProgressBarIndeterminateVisibility(false);
								util.log("oferta no existente");
								list.setVisibility(View.GONE);
								tvNoOffer.setVisibility(0);
								tvNoOffer.setTextSize(20);
								tvNoOffer.setPadding(0, 5, 0, 5);
								tvNoOffer.setGravity(Gravity.CENTER);
								tvNoOffer.setText("No hay ofertas");
							} else {
								// Hay ofertas
								util.log("ofertas existentes");
								tvNoOffer.setVisibility(View.GONE);
								songsList.clear();
								for (BackbeamObject offer : offers) {
									
									//23-10. He a�adido SI ADEM�S, el estado de la oferta es OK, que se cargue
									if (offer.getDay("deadline")
											.getTimeInMillis() >= today
											.getTime() ) {
										dataid.add(offer.getId());
										// Anadir al set Adapter
										// creating new HashMap
										final HashMap<String, String> map = new HashMap<String, String>();

										SimpleDateFormat format1 = new SimpleDateFormat(
												"dd-MM-yyyy");
										String formatted = format1.format(offer
												.getDay("deadline").getTime());
										// adding each child node to HashMap key
										// => value
										map.put(KEY_ID, offer.getId());
										map.put(KEY_DESCRIPTION,
												offer.getString("description"));
										map.put(KEY_LIKES,
												offer.getNumber("numlike")
														.toString());
										map.put(KEY_DEADLINE, formatted);

										// adding HashList to ArrayList
										songsList.add(map);
									}

								}tvSms.setVisibility(0);
							}

						}
					});

			// Getting adapter by passing data ArrayList
			adapter = new LazyAdapter(act, songsList);
			list.setAdapter(adapter);
			//
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			util.log("recorremos pre execute");
			setSupportProgressBarIndeterminateVisibility(true);
			util.log("mostramos dialog mydata");
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			util.log("doInBackgroundRecorrido mydata");

			return true;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// new LoadDataTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.menu3, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
/*
 * // CONSULTA PARA LA FOTO
 * 
 * /*CollectionConstraint collection = new CollectionConstraint();
 * collection.addIdentifier(offer.getId());
 * 
 * Query query = new Query("offer"); query.setQuery("where this in ? join file",
 * collection); query.fetch(100, 0, new FetchCallback() {
 * 
 * @Override public void success(List<BackbeamObject> companies, int totalCount,
 * boolean fromCache) { if (totalCount == 0) {
 * System.out.println("totalcount0"); map.put(KEY_THUMB_URL, "null"); } else {
 * 
 * for (BackbeamObject company : companies) {
 * 
 * BackbeamObject fileObject = company .getObject("file"); if (fileObject !=
 * null) { TreeMap<String, Object> options = new TreeMap<String, Object>();
 * options.put("width", 25); options.put("height", 25); String logoURL =
 * fileObject .composeFileURL(options); System.out.println(logoURL);
 * map.put(KEY_THUMB_URL, logoURL);
 * 
 * } else { map.put(KEY_THUMB_URL, "null"); } } } } });
 */

