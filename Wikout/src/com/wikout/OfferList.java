package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.CollectionConstraint;
import io.backbeam.FetchCallback;
import io.backbeam.JoinResult;
import io.backbeam.ObjectCallback;
import io.backbeam.Query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import utils.ImageLoader;
import utils.Util;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
	Context con = this;
	Util util = new Util();
	Bitmap bmPhoto =null;
	ImageView ivPhoto, ivOfferPhoto;
	static TextView tvLocation;
	String idcommerce="",placeName;
	ListView list;
	private Button btnAdd;
    LazyAdapter adapter;
	TextView tvOffer,tvLike,tvId,tvDeadline;
	// XML node keys
	
	static final String KEY_ID = "id";

	static final String KEY_THUMB_URL = "thumb_url";
	static final String KEY_DESCRIPTION = "description";
	static final String KEY_LIKES = "likes";
	static final String KEY_DEADLINE = "deadline";

	public class LazyAdapter extends BaseAdapter {
	    
	    private Activity activity;
	    private ArrayList<HashMap<String, String>> data;
	    private LayoutInflater inflater=null;
	    public ImageLoader imageLoader; 
	    
	    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
	        activity = a;
	        data=d;
	        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        imageLoader=new ImageLoader(activity.getApplicationContext());
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
	        View vi=convertView;
	        if(convertView==null)
	            vi = inflater.inflate(R.layout.offer_list_item, null);
	        tvOffer = (TextView) convertView
					.findViewById(R.id.tvOfferListOfferDescription);
			tvLike = (TextView) convertView
					.findViewById(R.id.tvOfferListOfferNumlike);
			tvDeadline = (TextView) convertView.findViewById(R.id.tvOfferListOfferDeadline);
			ivOfferPhoto = (ImageView) convertView.findViewById(R.id.ivOfferListOfferPhoto);

	        
			
	        HashMap<String, String> song = new HashMap<String, String>();
	        song = data.get(position);
	        
	        // Setting all values in listview
	        tvOffer.setText(song.get(KEY_DESCRIPTION));
	        tvLike.setText(song.get(KEY_LIKES));
	        tvDeadline.setText(song.get(KEY_DEADLINE));
	        imageLoader.DisplayImage(song.get(KEY_THUMB_URL), ivOfferPhoto);
	        return vi;
	    }
	}
	
	
	private void getPhoto(String idcommerce) {
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idcommerce);

		Query query = new Query("commerce");
		query.setQuery("where this in ? join file", collection);
		query.fetch(100, 0, new FetchCallback() {
			@Override
			public void success(List<BackbeamObject> companies, int totalCount,
					boolean fromCache) {
				
				for (BackbeamObject company : companies) {
					System.out.println("dentro de success foto");
					BackbeamObject fileObject = company.getObject("file");
					if(fileObject!=null){
						TreeMap<String, Object> options = new TreeMap<String, Object>();
						options.put("width", 100);
						options.put("height", 50);
						String logoURL = fileObject.composeFileURL(options);
	
						//Codigo para poner la foto en el imageView
						URL newurl = null;
						try {
							newurl = new URL(logoURL);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
						
						try {
							
							bmPhoto = BitmapFactory.decodeStream(newurl
									.openConnection().getInputStream());
							util.log("icono cargado");
							
							
							//image.setImageBitmap(mIcon_val);
						} catch (IOException e) {
							e.printStackTrace();
						}
					
						ivPhoto.setImageBitmap(bmPhoto);
				}else{
					ivPhoto.setImageDrawable(getResources().getDrawable( R.drawable.nophoto));
					
				}
				}}
		});
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.offer_list);
		util.projectData(con);
		
		initUi();
		
		
		}

	private void initUi(){
		setSupportProgressBarIndeterminateVisibility(true);
			Bundle bundle = getIntent().getExtras();
			ivPhoto=new ImageView(this);
			ivPhoto = (ImageView) findViewById(R.id.ivOfferListCommercePhoto);
			btnAdd=(Button)findViewById(R.id.btnAddOffer);
			list=(ListView)findViewById(R.id.listOff);
			//queryOffer(bundle.getString("id"));
			util.log(bundle.getString("id"));
			idcommerce = bundle.getString("id");
			//getPhoto(bundle.getString("id"));
			
			
			
			
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			new LoadDataTask().execute();
			
			btnAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
				Intent addOffer=new Intent(con,InsertOffer.class);
				addOffer.putExtra("idcommerce",idcommerce);
				addOffer.putExtra("placename",placeName);
				startActivity(addOffer);
					
				}
				
				});
			 list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int pos,long arg3) {
						
							Intent intent = new Intent(con,ViewOffer.class);	 
							intent.putExtra("idoffer", dataid.get(pos));
							intent.putExtra("idcommerce", idcommerce);
							intent.putExtra("placename",placeName);
							startActivity(intent);
							
						
					
							
						}});
			
	}
	

	
	public String getId() {
	String id = android.provider.Settings.System.getString(
			super.getContentResolver(),
			android.provider.Settings.Secure.ANDROID_ID);
	return id;
}

	public void queryOffer(String idcommerce){
		final ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
		
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idcommerce);
		
		Query query = new Query("commerce");
		query.setQuery("where this in ? join last 100 offer", collection);
		query.fetch(100, 0, new FetchCallback() {

			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
			
				BackbeamObject commerce = objects.get(0);
				JoinResult join = commerce.getJoinResult("offer");
				placeName=commerce.getString("placename");
				getSupportActionBar().setTitle(placeName);
				List<BackbeamObject> offers = join.getResults();
				// Contemplo si alguna referencia NO TIENE ofertas
				if (offers.size() == 0) {
					// No hay ofertas disponibles
					setSupportProgressBarIndeterminateVisibility(false);
					util.log("oferta no existente");
				} else {
					// Hay ofertas
					util.log("ofertas existentes");
					for (BackbeamObject offer : offers) {
						dataid.add(offer.getId());
						// Anadir al set Adapter
						// creating new HashMap
						final HashMap<String, String> map = new HashMap<String, String>();
						
						//CONSULTA PARA LA FOTO
						
						CollectionConstraint collection = new CollectionConstraint();
						collection.addIdentifier(offer.getId());

						Query query = new Query("offer");
						query.setQuery("where this in ? join file", collection);
						query.fetch(100, 0, new FetchCallback() {
							@Override
							public void success(List<BackbeamObject> companies, int totalCount,
									boolean fromCache) {
								
								for (BackbeamObject company : companies) {
									
									
									System.out.println("dentro de success foto");
									BackbeamObject fileObject = company.getObject("file");
							//if(fileObject!=null){
										TreeMap<String, Object> options = new TreeMap<String, Object>();
										options.put("width", 25);
										options.put("height", 25);
										String logoURL = fileObject.composeFileURL(options);

										map.put(KEY_THUMB_URL, logoURL);
									
										
								//}else{
									map.put(KEY_THUMB_URL, "null");
								//}
								}}});
						SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
						String formatted = format1.format(offer.getDay("deadline").getTime());
						// adding each child node to HashMap key => value
						map.put(KEY_ID, offer.getId());
						map.put(KEY_DESCRIPTION,offer.getString("description"));
						map.put(KEY_LIKES, offer.getNumber("numlike").toString());
						map.put(KEY_DEADLINE, formatted);
						

						// adding HashList to ArrayList
						songsList.add(map);
						
						
						
					}
				}
			}

		});
		
		// Getting adapter by passing data ArrayList
        adapter=new LazyAdapter(this, songsList);        
        list.setAdapter(adapter);
	}


	

// METODO PARA OBTENER LA FECHA ACTUAL  
protected Date actualDate(){
	Calendar calendar = new GregorianCalendar();
	final Date createdate = calendar.getTime();
	return createdate;
}
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
}
//Async Task para cargar datos al abrir la activity
	private class LoadDataTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			util.log("recorremos post execute mydata");
			queryOffer(idcommerce);
			
			getPhoto(idcommerce);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			util.log("recorremos pre execute");
			//setSupportProgressBarIndeterminateVisibility(true);
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
	    new LoadDataTask().execute();
	    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu3, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
}
/*
setSupportProgressBarIndeterminateVisibility(false);*/