package com.wikout;

import io.backbeam.BackbeamObject;
import io.backbeam.CollectionConstraint;
import io.backbeam.FetchCallback;
import io.backbeam.JoinResult;
import io.backbeam.Query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import utils.Util;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OfferList extends ActionBarActivity {

	
	private ArrayList<String> dataoffer = new ArrayList<String>();
	private ArrayList<String> datalike = new ArrayList<String>();
	private ArrayList<String> dataid = new ArrayList<String>();
	private ArrayList<Bitmap> dataphoto = new ArrayList<Bitmap>();
	
	ActionBar ab;
	Context con = this;
	Util util = new Util();
	Bitmap bmPhoto =null;
	ImageView ivPhoto, ivOfferPhoto;
	static TextView tvLocation;
	String idcommerce="",placeName;
	ListView list;
	private Button btnAdd;
	
	
	public static class viewHolder {
		TextView tvOffer,tvLike,tvId;
		ImageView ivOfferPhoto;
		
	}

	private class EfficientAdapter extends BaseAdapter {

		private Context context;
		LayoutInflater inflater;

		public EfficientAdapter(Context context) {this.context = context;
			inflater = LayoutInflater.from(context);

		}

		@Override
		public int getCount() {return datalike.size();
		}

		@Override
		public Object getItem(int position) {return position;
		}

		@Override
		public long getItemId(int position) {return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			viewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.offer_list_item, null);
				holder = new viewHolder();

				convertView.setTag(holder);

			} else {
				holder = (viewHolder) convertView.getTag();
			}
			holder.tvOffer = (TextView) convertView
					.findViewById(R.id.textViewName);
			holder.tvLike = (TextView) convertView
					.findViewById(R.id.textViewCode);
			holder.ivOfferPhoto = (ImageView) convertView.findViewById(R.id.ivOfferPhoto);
			
			
			holder.tvOffer.setText(dataoffer.get(position));
			holder.tvLike.setText("Likes: "+datalike.get(position));
			//holder.ivOfferPhoto.setImageBitmap(dataphoto.get(position));
			
			
			//holder.tid.setText(dataid.get(position));

			/*holder.btnView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,ViewOffer.class);	 
					intent.putExtra("idoffer", dataid.get(position));
					intent.putExtra("idcommerce", idcommerce);
					intent.putExtra("placename",placeName);
					startActivity(intent);
					
				}
			});*/
			return convertView;
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
						options.put("width", 160);
						options.put("height", 80);
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
					ivPhoto.setImageDrawable(getResources().getDrawable( R.drawable.ic_launcher));
					
				}
				}}
		});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.offer_list);
		util.projectData(con);
		
		initUi();
		
		
		}

	private void initUi(){
			Bundle bundle = getIntent().getExtras();
			ivPhoto=new ImageView(this);
			ivPhoto = (ImageView) findViewById(R.id.ivOfferListPhoto);
			btnAdd=(Button)findViewById(R.id.btnAddOffer);
			list=(ListView)findViewById(R.id.listOff);
			//queryOffer(bundle.getString("id"));
			util.log(bundle.getString("id"));
			idcommerce = bundle.getString("id");
			//getPhoto(bundle.getString("id"));
			new LoadDataTask().execute();
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			btnAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
				Intent addOffer=new Intent(con,InsertOffer.class);
				addOffer.putExtra("idcommerce",idcommerce);
				addOffer.putExtra("placename",placeName);
				startActivity(addOffer);
					
				}
				
				});
			
		}
	

	/*@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Toast.makeText(con, "Item :" + position, Toast.LENGTH_LONG).show();
	}*/
	
	public String getId() {
	String id = android.provider.Settings.System.getString(
			super.getContentResolver(),
			android.provider.Settings.Secure.ANDROID_ID);
	return id;
}



//METODO PARA OBTENER LOS DATOS DE LAS OFERTAS
	private void queryOffer(String idcommerce) {
		//vacio los arraylists
		
		dataoffer.clear();
		datalike.clear();
		dataid.clear();
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
					util.log("oferta no existente");
				} else {
					// Hay ofertas
					util.log("oferta existentes");
					for (BackbeamObject offer : offers) {
						
						SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
						String formatted = format1.format(offer.getDay("deadline").getTime());
						dataoffer.add(offer.getString("description")+ " Hasta: " + formatted);
						datalike.add(offer.getNumber("numlike").toString());
						dataid.add(offer.getId());
						
						/*CollectionConstraint collection = new CollectionConstraint();
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
									if(fileObject!=null){
										TreeMap<String, Object> options = new TreeMap<String, Object>();
										options.put("width", 50);
										options.put("height", 25);
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
											dataphoto.add(bmPhoto);
											
											//image.setImageBitmap(mIcon_val);
										} catch (IOException e) {
											e.printStackTrace();
										}
									
										
								}else{
									
									dataphoto.add(BitmapFactory.decodeResource(con.getResources(), R.drawable.ic_launcher));
								}
								}}
						});*/
						
						
						
						
						// Anadir al set Adapter
						list.setAdapter(new EfficientAdapter(con) );

					}
				}
			}

		});

	}
	

// METODO PARA OBTENER LA FECHA ACTUAL
protected Date actualDate(){
	Calendar calendar = new GregorianCalendar();
	final Date createdate = calendar.getTime();
	return createdate;
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
			util.showProgressDialog(con);
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
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
}
