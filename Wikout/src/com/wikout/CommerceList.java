package com.wikout;

import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utils.Util;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CommerceList extends ActionBarActivity {
Context context;
ListView listview;
double latitude,longitude;
static final int REQUESTCODEMAP = 101;
static final int REQUESTCODECOMMERCE = 102;
public static final int RESULTOK = 100;
public static ActionBarActivity fa;
final ArrayList<String> listPlacenameCommerces = new ArrayList<String>();
final ArrayList<String> listIdCommerces = new ArrayList<String>();
final Util util = new Util();
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    setContentView(R.layout.commerce_list);
	    context= this;
	    util.projectData(context);
	    fa=this;
	    
	    getSupportActionBar().setTitle("Comercios cercanos");
	    setSupportProgressBarIndeterminateVisibility(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	   listview = (ListView) findViewById(R.id.listcommerce);
	   Bundle location = getIntent().getExtras();//***************atencion
	    
	    if(location.getInt("enter")==1){
	    	util.log("entra dentro del if del commerceList");
	    	Intent mapv2 = new Intent(this,Mapv2.class);
	    	startActivityForResult(mapv2, REQUESTCODEMAP);
	    	//finish();
	    }else{
	    util.log("entra tras el if del commerceList");
	    latitude=location.getDouble("pointlat");
	    longitude=location.getDouble("pointlon");
	    getBoundingLocation(latitude,longitude);}
	    
	    
	    
	   
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	      @Override
	      public void onItemClick(AdapterView<?> parent, final View view,
	          int position, long id) {
	    	  if(position==listPlacenameCommerces.size()-1){
	    	 Intent insertCommerce = new Intent(context,InsertCommerce.class);
	    	  
	    	  insertCommerce.putExtra("pointlat",latitude);
	    	  insertCommerce.putExtra("pointlon",longitude);
	    	  startActivityForResult(insertCommerce,REQUESTCODECOMMERCE);
	    	  //finish();
	    	  }else{
	    		 Intent insertOffer= new Intent();
		    	  insertOffer.putExtra("placename", listPlacenameCommerces.get(position));
		    	  insertOffer.putExtra("idcommerce", listIdCommerces.get(position));
		    	  setResult(RESULT_OK, insertOffer);
		    	  //startActivity(insertOffer);
		    	  finish();

	    	  }
	    	 
	    	  }
	      
	    
	      

	    });
	  }
	// load image in imageView
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if(data==null){
				
			}
		    if (requestCode == REQUESTCODEMAP) {
		        if(resultCode == RESULT_OK){
		        	System.out.println("entra en result code");
		        	Bundle d = data.getExtras();
		            latitude=d.getDouble("pointlat");
		            longitude=d.getDouble("pointlon");
		            getBoundingLocation(latitude, longitude);
		        }

		    }if(requestCode== REQUESTCODECOMMERCE && resultCode==RESULT_OK){
		    	Bundle d=data.getExtras();
		    	String placename =d.getString("placename");
		    	String idcommerce = d.getString("idcommerce");
		    	Intent i = new Intent();
		    	i.putExtra("placename", placename);
		    	i.putExtra("idcommerce", idcommerce);
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
	protected void getBoundingLocation(double userlat, double userlon) {
		
		util.log("coordenadas: "+userlat+", "+userlon);
		// Vacio los arraylists
		listPlacenameCommerces.clear();
		listIdCommerces.clear();
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
						// RECORRO CADA COMERCIO
						if (totalCount==0){

						Intent insertCommerce = new Intent(context, InsertCommerce.class);
							insertCommerce.putExtra("pointlat",latitude);
					    	insertCommerce.putExtra("pointlon",longitude);
					    	startActivityForResult(insertCommerce,REQUESTCODECOMMERCE);
						}else{
						for (BackbeamObject commerce : commerces) {
							// CREAR ITEMS PARA LA LISTA
							util.log(String.valueOf(totalCount));
							listPlacenameCommerces.add(commerce
									.getString("placename"));
							listIdCommerces.add(commerce.getId());

						}
						listPlacenameCommerces.add("NUEVO");
						listIdCommerces.add("null");
						StableArrayAdapter adapter = new StableArrayAdapter(context,
						        android.R.layout.simple_list_item_1, listPlacenameCommerces);
						    listview.setAdapter(adapter);
						    setSupportProgressBarIndeterminateVisibility(false);
						}
					}
					
				});
	}

	} 