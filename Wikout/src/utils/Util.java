package utils;

import java.util.List;

import io.backbeam.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
	 public void checkVersion(final Context context, final String minversion){
		
		 final AlertDialog.Builder info = new AlertDialog.Builder(context);
		 info.setTitle("Actualización");
		 Query query = new Query("settings");
		 System.out.println("Después de query");
		 query.fetch(100, 0, new FetchCallback() {

		      @Override
		      public void success(List<BackbeamObject> objects,
		        int totalCount, boolean fromCache) {
		        // pick a place (in real code check the objects.size() first)
		    
		    	  System.out.println("Llega al success");
		    	  for (BackbeamObject setting : objects) {
		    		  //cojo el objeto minversion
		    		  System.out.println("Entra en el for");
		    		  
		    		  
		    			  //Si la versión es menor que la del market
		    			  if(Integer.parseInt(setting.getString("minversion"))>Integer.parseInt(minversion)){
		    				  System.out.println("Valor de required update: "+  setting.getBoolean("requiredupdate"));
		    				  if(setting.getBoolean("requiredupdate")==true ){
				    				  //obligo a ir al market
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
				    				  //ofrezco ir al market
				    					info.setMessage("Existe una actualización de Wikout.\n ¿Quieres descargártela?");
				    					info.setCancelable(true);
				    					info.setPositiveButton("Sí. ¡Claro!",
				    							new DialogInterface.OnClickListener() {
				    								@Override
				    								public void onClick(DialogInterface dialogo1, int id) {
				    									Uri uri = Uri.parse("http://www.google.com");
				    									Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				    									context.startActivity(intent);
				    								}});
				    					info.setNegativeButton("Por ahora no.",
				    							new DialogInterface.OnClickListener() {
						    						@Override
						    						public void onClick(DialogInterface dialogo1, int id) {
						    							
						    						}});
				    					info.show();
				    			  }
		    		  
		    		  
		    			  
		    		  
		    		  
		    			  
		    		  }
		    			  
		    	  }
		      }
		});
		 

		}
	 
	 
	 
	 
	public void showProgressDialog(Context context){
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setCancelable(true);
		dialog.setMessage("Cargando...");
		dialog.isIndeterminate();
		dialog.show();
		Thread cronometro = new Thread(){
	        @Override
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

		public void showProgressDialog(Context context, final int i) {
			final ProgressDialog dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Cargando...");
			dialog.isIndeterminate();
			dialog.show();
			Thread cronometro = new Thread(){
		        @Override
				public void run(){
		                try{  
		                        Thread.sleep(i);
		                        dialog.dismiss();
		                        }
		                catch(Exception e){
		               
		                }
		        }
		    };

		   cronometro.start(); 
		}
	
		public Bitmap writeTextOnDrawable(Context context,int drawableId, String text) {

		    Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId)
		            .copy(Bitmap.Config.ARGB_8888, true);

		    Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

		    Paint paint = new Paint();
		    paint.setStyle(Style.FILL);
		    paint.setColor(Color.WHITE);
		    paint.setTypeface(tf);
		    paint.setTextAlign(Align.CENTER);
		    paint.setTextSize(convertToPixels(context, 80));

		    Rect textRect = new Rect();
		    paint.getTextBounds(text, 0, text.length(), textRect);

		    Canvas canvas = new Canvas(bm);

		    //If the text is bigger than the canvas , reduce the font size
		    if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
		        paint.setTextSize(convertToPixels(context, 20));        //Scaling needs to be used for different dpi's

		    //Calculate the positions
		    int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

		    //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
		    int yPos = (int) ((canvas.getHeight() /2) - ((paint.descent() + paint.ascent())/4)) ;  

		    canvas.drawText(text, xPos, yPos, paint);
		    
		    int width = bm.getWidth();
	        int height = bm.getHeight();

	        float bitmapRatio = (float)width / (float) height;
	        if (bitmapRatio > 0) {
	            width = 40; //MAXsize
	            height = (int) (width / bitmapRatio);
	        } else {
	            height = 40; //MAXsize
	            width = (int) (height * bitmapRatio);
	        }
	        return Bitmap.createScaledBitmap(bm, width, height, true);
		   
		}



		public static int convertToPixels(Context context, int nDP)
		{
		    final float conversionScale = context.getResources().getDisplayMetrics().density;

		    return (int) ((nDP * conversionScale) + 0.5f) ;

		}
}
