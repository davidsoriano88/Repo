package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.FetchCallback;
import io.backbeam.Query;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import utils.Settings;
import utils.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

public class SplashScreen extends Activity {
	

  private long splashDelay = 500; //6 seconds.
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
    setContentView(R.layout.splash_screen);
    /*
    RateMeMaybe rmm = new RateMeMaybe( );
    rmm.setPromptMinimums(10, 14, 10, 30);
    rmm.run();
*/
	Backbeam.setProject("pruebaapp");
	Backbeam.setEnvironment("dev");
	Backbeam.setContext(context);
	// Create the API keys in the control panel of your project
	Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
	Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");
	
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
    	
    //Si existe y es mayor a 10 dias:
    }else if(time - Settings.getTimeLastCheckVersion(context)>= 864000){
    	System.out.println("Hace tanto tiempo de la ultima vez: "+(time - Settings.getTimeLastCheckVersion(context)));
    	final Resources res = getResources();
		
		//
		
		//CONSULTA VERSIÓN
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
						 info.setTitle("Actualización");
						 info.setMessage("Existe una actualización de Wikout.\n ¿Quieres descargártela?");
	    					info.setCancelable(true);
	    					info.setPositiveButton("Sí. ¡Claro!",
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
    
    
    
  
  }
  
  public void initUI(){
	  progressBar=(ProgressBar)findViewById(R.id.Initprogress);
	  TimerTask task = new TimerTask(){
		 @Override
		 public void run(){
			 beginYourTask();
	        Intent mainIntent = new Intent().setClass(SplashScreen.this, Map.class);
	        
	        startActivity(mainIntent);
	        finish();
		 }
	  };

	  Timer timer = new Timer();
	  timer.schedule(task, splashDelay);//after 6 seconds throws the task.
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