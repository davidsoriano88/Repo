package com.wikout;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

public class SplashScreen extends Activity {
	

  private long splashDelay = 500; //6 seconds.
  private static int myProgress=0;
	private ProgressBar progressBar;
	private int progressStatus=0;
	private Handler myHandler=new Handler();
	
	

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash_screen);
    initUI();
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