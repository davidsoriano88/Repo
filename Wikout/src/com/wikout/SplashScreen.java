package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.Query;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import model.FontUtils;
import utils.MyLocation.LocationResult;
import utils.RateMeMaybe;
import utils.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashScreen extends ActionBarActivity  implements LocationListener {
	

  private static int myProgress=0;
	private ProgressBar progressBar;
	private int progressStatus=0;
	private Handler myHandler=new Handler();
	private Context context=this;
	public LocationResult locationResult = null;
	Util util;
	double lat = 0,lng=0;
	
	// DECLARO Strings
	String appReqUpdate, appMinVers;
	
	private LocationManager locationManager;
	private String provider;
	Intent mainIntent =null;

	
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash_activity);
    
    //Cambiar la fuente
    FontUtils.setRobotoFont(context, ((Activity) context).getWindow().getDecorView());
    
    //RateMeMaybe
    RateMeMaybe rmm = new RateMeMaybe(this);
    rmm.setPromptMinimums(10, 14, 10, 30);
    rmm.setDialogMessage("You really seem to like this app, "
                    +"since you have already used it %totalLaunchCount% times! "
                    +"It would be great if you took a moment to rate it.");
    rmm.setDialogTitle("Rate this app");
    rmm.setPositiveBtn("Yeeha!");
    rmm.run();
    
    //Backbeam ProjectData
	Backbeam.setProject("pruebaapp");
	Backbeam.setEnvironment("dev");
	Backbeam.setContext(context);
	// Create the API keys in the control panel of your project
	Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
	Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");
	
	
	//util.refreshActualOffers();
	
	//Get the location manager
	getLocationMethod();
			
	//Alternativa al locationManager	
/*	MyLocation myLocation = new MyLocation();
	myLocation.getLocation(this, locationResult);
	util.logBug(locationResult.toString());*/
	
	
	
	
    //CREO LA REFERENCIA de TIEMPO
    int time = (int) (System.currentTimeMillis()/1000);
    System.out.println("tiempoo actual Milisegundos: "+System.currentTimeMillis());
    System.out.println("tiempoo actual Integer: "+time);
    
    
    //Si no existe la referencia del tiempo LA CREO

    
    //Creo el intent pa ahorrar codigo
    Uri uri = Uri.parse("http://www.google.com");
	final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	
	
	//COMPRUEBO SI LLEVA MÁS DE 10 DIAS INSTALADA
    if(Settings.getTimeLastCheckVersion(context)==0){
    	Settings.setTimeLastCheckVersion(context, time);
    	initUI();
    	
    //Si existe y es mayor a 10 dias:
    }else if(time - Settings.getTimeLastCheckVersion(context)>= 864000){
    	final Resources res = getResources();
		
		//CONSULTA VERSIÓN INSTALADA
		Query query = new Query("setting");
		query.fetch(100, 0, new FetchCallback() {

			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
				
				// ARRAY de objetos BackBeam
				for (BackbeamObject setting : objects) {

					// Analizo cada objeto y extraigo el valor
					if (setting.getString("key").contains("required_update")) {
						appReqUpdate = setting.getString("value");
					} if(setting.getString("key").contains("min_version")) {
						 appMinVers = setting.getString("value");
					}
					
				}
				
				//LOGICA
				//Si versión del market de la app es > que ésta
				if(Integer.parseInt(appMinVers)>Integer.parseInt(res.getString(R.string.min_version))){
					
					//si obligo o no que se la descarguen
					if(appReqUpdate.contains("true")){
						
						//obligo ir al market y creo el alert dialog
						AlertDialog.Builder info = new AlertDialog.Builder(context);
						
						info.setTitle("Actualización");
						info.setMessage("Existe una nueva versión de Wikout.");
    					info.setCancelable(false);
    					info.setNeutralButton("¡Descárgatela!",
    							new DialogInterface.OnClickListener() {
    								@Override
    								public void onClick(final DialogInterface dialogo1, final int id) {
    									context.startActivity(intent);
    									android.os.Process.killProcess(android.os.Process.myPid());
    								}
    							});
	    				info.show();
						
					}else{
						//Sugiero ir al market
						AlertDialog.Builder info = new AlertDialog.Builder(context);
						
						info.setTitle("Actualización");
						info.setMessage("Existe una actualización de Wikout.\n ¿Quieres descargártela?");
	    				info.setCancelable(true);
	    				info.setPositiveButton("Sí. ¡Claro!",
    							new DialogInterface.OnClickListener() {
    								@Override
    								public void onClick(DialogInterface dialogo1, int id) {
    									context.startActivity(intent);
    									initUI();
    									
    								}});
	    				
    					info.setNegativeButton("Por ahora no.",
    							new DialogInterface.OnClickListener() {
		    						@Override
		    						public void onClick(DialogInterface dialogo1, int id) {
		    							initUI();
		    						}});
    					info.show();
					}}}});
    	
    	
    }else{
    	initUI();
    }
    if (savedInstanceState == null) {
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new PlaceholderFragment()).commit();
	}
    
    
  
  }

	private void getLocationMethod() {
		//COJO EL PROVEEDOR DE LOCALIZACIONES (GPS Y EL PROVEEDOR DE LA RED)
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		// Initialize the location fields
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {
			System.out.println("Location not available");
			System.out.println("Location not available");
		}
}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.splash_screen, container,
					false);
			return rootView;
		}
	}
  
  
  public void initUI(){
	  progressBar=(ProgressBar)findViewById(R.id.Initprogress);
	  TimerTask task = new TimerTask(){
		 @Override
		 public void run(){
			//METODO COMENTADO--> DA PROBLEMAS. 
			// beginYourTask();

			 
	        mainIntent = new Intent().setClass(SplashScreen.this, Map.class);
	        mainIntent.putExtra("latitudSplash", lat);
			mainIntent.putExtra("longitudSplash", lng);
			startActivity(mainIntent);
	        finish();   
		 }
	  };
	  Timer timer = new Timer();
	  timer.schedule(task, 3000);//after 3 seconds throws the task.
  
  }
  
  //COMPROBAR METODOS:
  public void beginYourTask()
  {
  	myProgress=0;
     
      progressBar.setMax(500);
      
      new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(progressStatus<500)
				{
					progressStatus=performTask();
					myHandler.post(new Runnable()
					{
					public void run() {
					progressBar.setProgress(progressStatus);
					}
					});
					
				}
				myHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
	                   progressStatus=0; 
	                   myProgress=0;
						
					}
				});
				
			}
			private int performTask()
			{
				
					return ++myProgress;	
			}
		}).start();
 }
  


	

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 10, 1, this);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		lat = location.getLatitude();
		lng = location.getLongitude();
		
		System.out.println(String.valueOf(lat));
		System.out.println(String.valueOf(lng));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

  
}