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
import java.util.List;
import java.util.TreeMap;

import com.wikout.R;

import utils.Util;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OfferList extends ListActivity {

	private ArrayList<String> dataoffer = new ArrayList<String>();
	private ArrayList<String> datalike = new ArrayList<String>();
	private ArrayList<String> dataid = new ArrayList<String>();
	Context con = this;
	Util util = new Util();
	Bitmap mIcon_val =null;
	ImageView image;
	TextView nombre;
	String idcommerce="";
	public static class viewHolder {
		TextView toffer;
		TextView tlike;
		TextView tid;
		Button blike;	
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
				convertView = inflater.inflate(R.layout.list_activity, null);
				holder = new viewHolder();

				convertView.setTag(holder);

			} else {
				holder = (viewHolder) convertView.getTag();
			}
			holder.toffer = (TextView) convertView
					.findViewById(R.id.textViewName);
			holder.tlike = (TextView) convertView
					.findViewById(R.id.textViewCode);
			holder.blike = (Button) convertView.findViewById(R.id.btLike);
			
			
			holder.toffer.setText(dataoffer.get(position));
			holder.tlike.setText("Likes: "+datalike.get(position));
			
			
			
			//holder.tid.setText(dataid.get(position));

			holder.blike.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(),
							OfferReport.class);	 
					intent.putExtra("idoffer", dataid.get(position));
					intent.putExtra("idcommerce", idcommerce);
					startActivity(intent);
					finish();
				}
			});
			return convertView;
		}

	}
	protected void getPhoto(String idcommerce) {
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
						mIcon_val = BitmapFactory.decodeStream(newurl
								.openConnection().getInputStream());
						util.log("icono cargado");
						image.setImageBitmap(mIcon_val);
						//image.setImageBitmap(mIcon_val);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
				}
			}
		});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.listview_activity);
		initUi();
		
		
		}

		public void initUi(){
			Bundle bundle = getIntent().getExtras();
			image=new ImageView(this);
		image = (ImageView) findViewById(R.id.ivTitleReference);
		nombre=(TextView)findViewById(R.id.idRef);
		
		queryOffer(bundle.getString("id"));
		util.log(bundle.getString("id"));
		idcommerce = bundle.getString("id");
		getPhoto(bundle.getString("id"));
		}
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Toast.makeText(con, "Item :" + position, Toast.LENGTH_LONG).show();
	}
	
	public String getId() {
	String id = android.provider.Settings.System.getString(
			super.getContentResolver(),
			android.provider.Settings.Secure.ANDROID_ID);
	return id;
}



//METODO PARA OBTENER LOS DATOS DE LAS OFERTAS
	private void queryOffer(String idreference) {
		projectData();
		dataoffer.clear();
		datalike.clear();
		dataid.clear();
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idreference);

		Query query = new Query("commerce");
		query.setQuery("where this in ? join last 100 offer", collection);
		query.fetch(100, 0, new FetchCallback() {

			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
				BackbeamObject place = objects.get(0);
				JoinResult join = place.getJoinResult("offer");

				nombre.setText(place.getString("placename"));
				util.log(place.getString("placename"));
				List<BackbeamObject> offers = join.getResults();
				util.log("succes!!");
				// Contemplar si alguna referencia NO TIENE ofertas
				if (offers.size() == 0) {
					// No hay ofertas disponibles
				} else {
					// Hay ofertas
					for (BackbeamObject offer : offers) {
						SimpleDateFormat format1 = new SimpleDateFormat(
								"dd-MM-yyyy");
						String formatted = format1.format(offer.getDay(
								"deadline").getTime());
						dataoffer.add(offer.getString("description")
								+ " Hasta: " + formatted);
						datalike.add(offer.getNumber("numlike").toString());
						dataid.add(offer.getId());
						// Anadir al set Adapter
						setListAdapter(new EfficientAdapter(con));

					}
				}
			}

		});

	}
//METODO PARA INSERTAR LIKE
protected void insertLike(final String idoffer) {
	// CREO OBJETOS
	final BackbeamObject like = new BackbeamObject("like");
	final BackbeamObject offer = new BackbeamObject("offer", idoffer);
	// Escribo los campos de "like"
	like.setString("udid", getId());
	like.setDate("likedate",actualDate());
	like.setString("statuslike", "1");
	like.setObject("offer", offer);

	like.save(new ObjectCallback() {
		@Override
		public void success(BackbeamObject object) {
			System.out.println("like guardado");
			Query query = new Query("like");
			query.setQuery("where offer = ?", idoffer);
			query.fetch(100, 0, new FetchCallback() {
				@Override
					public void success(List<BackbeamObject> objects, int totalCount,boolean fromCache) {
						final int numlike = totalCount;
						Backbeam.read("offer", idoffer, new ObjectCallback() {
							@Override
							public void success(BackbeamObject offer) {
								offer.setNumber("numlike", numlike);
								offer.save(new ObjectCallback() {
								@Override
								public void success(BackbeamObject object) {
									System.out.println("updated! :) "+object.getId());
									Bundle bundle = getIntent().getExtras();
									queryOffer(bundle.getString("id"));
								}
							});
							}
						});
					}});
		
		}
	});
}
	
//DATOS DEL PROYECTO
protected void projectData() {
	Backbeam.setProject("pruebaapp");
	Backbeam.setEnvironment("dev");
	Backbeam.setContext(getApplicationContext());

	// Create the API keys in the control panel of your project
	Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
	Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");

}
// METODO PARA OBTENER LA FECHA ACTUAL
protected Date actualDate(){
	Calendar calendar = new GregorianCalendar();
	final Date createdate = calendar.getTime();
	return createdate;
}

}