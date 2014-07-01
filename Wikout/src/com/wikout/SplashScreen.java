package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.Query;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import utils.MyLocation;
import utils.MyLocation.LocationResult;
import utils.RateMeMaybe;
import utils.Settings;
import utils.Util;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class SplashScreen extends ActionBarActivity {
	

  private static int myProgress=0;
	private ProgressBar progressBar;
	private int progressStatus=0;
	private Handler myHandler=new Handler();
	private Context context=this;
	 Util util;
	
	// DECLARO Strings
	String appReqUpdate, appMinVers;
	
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash_activity);
    
    RateMeMaybe rmm = new RateMeMaybe(this);
    rmm.setPromptMinimums(10, 14, 10, 30);
    rmm.setDialogMessage("You really seem to like this app, "
                    +"since you have already used it %totalLaunchCount% times! "
                    +"It would be great if you took a moment to rate it.");
    rmm.setDialogTitle("Rate this app");
    rmm.setPositiveBtn("Yeeha!");
    rmm.run();

	Backbeam.setProject("pruebaapp");
	Backbeam.setEnvironment("dev");
	Backbeam.setContext(context);
	// Create the API keys in the control panel of your project
	Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
	Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");
	
	
	//util.refreshActualOffers();
	
	
	LocationResult locationResult = new LocationResult(){
	  

		@Override
		public void gotLocation(Location location) {
			// TODO Auto-generated method stub
			
		}
	};
	MyLocation myLocation = new MyLocation();
	myLocation.getLocation(this, locationResult);
	
	
	
	
	
    //CREO LA REFERENCIA de TIEMPO
    int time = (int) (System.currentTimeMillis()/1000);
    System.out.println("tiempoo actual Milisegundos: "+System.currentTimeMillis());
    System.out.println("tiempoo actual Integer: "+time);
    
    
    //Si no existe la referencia del tiempo LA CREO
    System.out.println("tiempo registrado Integer: "+Settings.getTimeLastCheckVersion(context));
    System.out.println("resta de tiempo: " +(time-Settings.getTimeLastCheckVersion(context)));
    if(Settings.getTimeLastCheckVersion(context)==0){
    	Settings.setTimeLastCheckVersion(context, time);
    	System.out.println("no existe ref de tiempo");
    	initUI();
    //Si existe y es mayor a 10 dias:
    }else if(time - Settings.getTimeLastCheckVersion(context)>= 864000){
    	System.out.println("Hace tanto tiempo de la ultima vez: "+(time - Settings.getTimeLastCheckVersion(context)));
    	final Resources res = getResources();
		
		//
		
		//CONSULTA VERSI�N
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
				//Si versi�n del market de la app es > que �sta
				if(Integer.parseInt(appMinVers)>Integer.parseInt(res.getString(R.string.min_version))){
					
					//si obligo o no que se la descarguen
					if(appReqUpdate.contains("true")){
						//obligo ir al market y creo el alert dialog
						AlertDialog.Builder info = new AlertDialog.Builder(context);
						 info.setTitle("Actualizaci�n");
						info.setMessage("Existe una nueva versi�n de Wikout.");
    					info.setCancelable(false);
    					info.setNeutralButton("�Desc�rgatela!",
    							new DialogInterface.OnClickListener() {
    								@Override
    								public void onClick(final DialogInterface dialogo1, final int id) {
    									Uri uri = Uri.parse("http://www.google.com");
    									Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    									context.startActivity(intent);
    									android.os.Process.killProcess(android.os.Process.myPid());
    								}

    							});
    					
    					info.show();
						
					}else{
						//Sugiero ir al market
						AlertDialog.Builder info = new AlertDialog.Builder(context);
						 info.setTitle("Actualizaci�n");
						 info.setMessage("Existe una actualizaci�n de Wikout.\n �Quieres descarg�rtela?");
	    					info.setCancelable(true);
	    					info.setPositiveButton("S�. �Claro!",
	    							new DialogInterface.OnClickListener() {
	    								@Override
	    								public void onClick(DialogInterface dialogo1, int id) {
	    									Uri uri = Uri.parse("http://www.google.com");
	    									Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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
			// beginYourTask();
	        Intent mainIntent = new Intent().setClass(SplashScreen.this, Map.class);
	        
	     startActivity(mainIntent);
	        finish();   
		 }
	  };

	  Timer timer = new Timer();
	  timer.schedule(task, 3000);//after 6 seconds throws the task.
  
  }
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
}