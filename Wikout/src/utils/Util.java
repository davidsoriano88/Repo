package utils;

import io.backbeam.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
	
		

		public void projectData(Context context) {
			Backbeam.setProject("pruebaapp");
			Backbeam.setEnvironment("dev");
			Backbeam.setContext(context);
			// Create the API keys in the control panel of your project
			Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
			Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");
			
		}
	
	
}