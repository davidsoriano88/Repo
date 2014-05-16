package com.wikout;

import java.util.Timer;
import java.util.TimerTask;

import com.wikout.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends Activity {

  private long splashDelay = 2000; //6 seconds.

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initUI();
  }
  
  public void initUI(){
	  TimerTask task = new TimerTask(){
		 @Override
		 public void run(){
	        Intent mainIntent = new Intent().setClass(SplashScreen.this, Map.class);
	        startActivity(mainIntent);
	        finish();
		 }
	  };

	  Timer timer = new Timer();
	  timer.schedule(task, splashDelay);//after 6 seconds throws the task.
  }

}