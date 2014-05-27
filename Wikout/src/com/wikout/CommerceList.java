package com.wikout;

import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utils.Util;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CommerceList extends Activity {
Context context;
final ArrayList<String> listPlacenameCommerces = new ArrayList<String>();
final ArrayList<String> listIdCommerces = new ArrayList<String>();
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.commerce_list);
	    context= this;
	    final Util util = new Util();
	    
	    Bundle location = getIntent().getExtras();
	    getBoundingLocation(location.getDouble("latiMain"),location.getDouble("longiMain"));
	    final ListView listview = (ListView) findViewById(R.id.listcommerce);
	    

	    
	    
	    final StableArrayAdapter adapter = new StableArrayAdapter(this,
	        android.R.layout.simple_list_item_1, listPlacenameCommerces);
	    listview.setAdapter(adapter);

	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	      @Override
	      public void onItemClick(AdapterView<?> parent, final View view,
	          int position, long id) {
	    	  
	    	  util.showToast(context,"click"); 
	    	  Intent insertCommerce = new Intent(context,InsertCommerce.class);
	    	  insertCommerce.putExtra("placename", listPlacenameCommerces.get(position));
	    	  insertCommerce.putExtra("idcommerce", listIdCommerces.get(position));
	    	  startActivity(insertCommerce);
	    	  }
	      /* final String item = (String) parent.getItemAtPosition(position);
	        view.animate().setDuration(2000).alpha(0)
	            .withEndAction(new Runnable() {
	              @Override
	              public void run() {
	                list.remove(item);
	                adapter.notifyDataSetChanged();
	                view.setAlpha(1);
	              }
	            });*/
	      

	    });
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
	  
	// METODO para localizar comercios cercanos respecto a las coordenadas del
	// usuario
	protected void getBoundingLocation(double userlat, double userlon) {
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
						for (BackbeamObject commerce : commerces) {
							// CREAR ITEMS PARA LA LISTA
							listPlacenameCommerces.add(commerce
									.getString("placename"));
							listIdCommerces.add(commerce.getId());

						}
						listPlacenameCommerces.add("NUEVO");
					}
					
				});
	}

	} 