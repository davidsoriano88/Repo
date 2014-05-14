package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.CollectionConstraint;
import io.backbeam.FetchCallback;
import io.backbeam.JoinResult;
import io.backbeam.ObjectCallback;
import io.backbeam.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.wikout.R;

import utils.Util;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Reference extends Activity {
	public TextView name,tvOffer,ubication,valid;
	public String string,string1;
	public Button button1,button2;
	public ImageView photo;
	Util util = new Util();
	

	  @Override
	  protected void onCreate(Bundle savedInstanceState) 
	  {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.list_activity);
	    initUI();
	  }
	  
	  public void initUI()
	  {
		  	
		  	name=(TextView)findViewById(R.id.nombreTv);
			tvOffer=(TextView)findViewById(R.id.ofertaTv);
			button1=(Button)findViewById(R.id.button1);
			button2=(Button)findViewById(R.id.button2);
			ubication=(TextView)findViewById(R.id.ubicacionTv);
			photo =(ImageView)findViewById(R.id.ivFoto);
			valid=(TextView)findViewById(R.id.validoTv);
			name.setTextColor(Color.WHITE);
			button1.setTextColor(Color.WHITE);
			
			loadData();
		}

		public void loadData(){
			Bundle bundle = getIntent().getExtras();
	        name.setText(bundle.getString("placename"));
			queryOffer(bundle.getString("id"));
			util.log(bundle.getString("id"));
	       }
		
		private void queryOffer(String idreference) {
			Backbeam.setProject("pruebaapp");
			Backbeam.setEnvironment("dev");
			Backbeam.setContext(getApplicationContext());
			// Create the API keys in the control panel of your project
			Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
			Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");
			
			CollectionConstraint collection = new CollectionConstraint();
			collection.addIdentifier(idreference);

			Query query = new Query("commerce");
			query.setQuery("where this in ? join last 10 offer", collection);
			query.fetch(100, 0, new FetchCallback() {

				@Override
				public void success(List<BackbeamObject> objects, int totalCount,
						boolean fromCache) {
					BackbeamObject place = objects.get(0);
					JoinResult join = place.getJoinResult("offer");
					List<BackbeamObject> offers = join.getResults();
					
					// if there are references without offers
					for (BackbeamObject offer:offers) {
						System.out.println("description "+ offer.getString("description"));
						System.out.println("numlike "+ offer.getNumber("numlike"));
						
						SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
						String formatted = format1.format(offer.getDay("deadline").getTime());
						System.out.println("deadline " +formatted);
					}

				}
			});

		}
		
		private void insertLike(final String idoffer){
			Backbeam.setProject("pruebaapp");
			Backbeam.setEnvironment("dev");
			Backbeam.setContext(getApplicationContext());
			// Create the API keys in the control panel of your project
			Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
			Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");
			
			Calendar calendar = new GregorianCalendar();
			final Date createdate = calendar.getTime();
			//create objects
			final BackbeamObject like = new BackbeamObject("like");
			final BackbeamObject offer = new BackbeamObject("offer", idoffer);
			like.setString("udid", getId().toString());
			like.setDate("likedate", createdate);
			like.setObject("offer", offer);
					
			like.save(new ObjectCallback() {
				@Override
				public void success(BackbeamObject object) {
					System.out.println("like guardado");			
				}});
			//counts likes per offer
			CollectionConstraint collection = new CollectionConstraint();
			collection.addIdentifier(idoffer);
			Query query = new Query("offer");
			query.setQuery("where this in ? join last 10 like", collection);
			query.fetch(100, 0, new FetchCallback() {

				@Override
				public void success(List<BackbeamObject> objects, int totalCount,
						boolean fromCache) {
					BackbeamObject place = objects.get(0); 
				    JoinResult join = place.getJoinResult("like");
				    //"count" gives the amount of likes of the offer
				    int count = join.getCount();
				    //set the number of likes:
				    offer.setNumber("numlike", count);
					offer.save(new ObjectCallback() {
					    @Override
					    public void success(BackbeamObject object) {
					    	System.out.println(offer.getString("description"));
					    	System.out.println(offer.getNumber("numlike"));
					    }});
				}
			});
		}
		public String getId() {
			String id = android.provider.Settings.System.getString(
					super.getContentResolver(),
					android.provider.Settings.Secure.ANDROID_ID);
			return id;
		}
}
