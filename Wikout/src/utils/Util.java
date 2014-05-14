package utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Util {
	public void showToast(Context context, String mensaje) {
		Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
	}

	 public void showInfoDialog(Context context, String title, String message){
		 AlertDialog.Builder info = new AlertDialog.Builder(context);
			info.setTitle(title);
			info.setMessage(message);
			info.setCancelable(false);
			info.setNeutralButton("Aceptar",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogo1, int id) {

						}

					});
			
			info.show();
		}
	public void showDialog(Context context){
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setCancelable(false);
		dialog.setMessage("Loading...");
		dialog.isIndeterminate();
		dialog.show();
		Thread cronometro = new Thread(){
	        public void run(){
	                try{  
	                        Thread.sleep(1000);
	                        dialog.dismiss();
	                        }
	                catch(Exception e){
	               
	                }
	        }
	    };
	   cronometro.start(); }

		public void log(String mensaje){
			boolean debug=true;
			if(debug){
				Log.i("log",mensaje);
			}
		}

	
	
}