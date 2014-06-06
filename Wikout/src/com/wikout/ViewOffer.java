package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamObject;
import io.backbeam.CollectionConstraint;
import io.backbeam.FetchCallback;
import io.backbeam.ObjectCallback;
import io.backbeam.Query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import utils.Util;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewOffer extends ActionBarActivity {

	TextView tvDescription,tvDeadline,tvCreationDate,tvLocation,tvNumLike;
	static TextView tvDistance;
	ImageButton btnFlag,btnLike;
	ImageView ivPhoto;
	String idofferparameter = "",idcommerceparameter = "";
	Context context;
	Util util = new Util();
	
	long startui;
	
	boolean lastlike= false;
	//lastlike TRUE: Si pulsa el boton LIKE, inserta STATUSLIKE "1"
	//lastlike FALSE: Si pulsa el boton DISLIKE, inserta STATUSLIKE "0"
	int numbubble = 0;
	
	
	//Radio de la tierra (en metros)
	final static double radio = 6371000;
	final static double distancedouble = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.view_offer);
		context=this;
		util.projectData(context);
		//util.showProgressDialog(context, 1900);
		initUi();
		initQueries();

	}

	private void initQueries() {
		
		Bundle bundle = getIntent().getExtras();
		idofferparameter = bundle.getString("idoffer");
		idcommerceparameter = bundle.getString("idcommerce");
		new LoadDataTask().execute();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(bundle.getString("placename"));
		
	}

	private void loadData(String idoffer) {
		
		Backbeam.read("offer", idoffer, new ObjectCallback() {
			@Override
			public void success(BackbeamObject offer) {
				tvDescription.setText(offer.getString("description"));
				if(offer.getNumber("numlike").intValue()==0){
					//tvNumLike.setText("Ningún usuario ha dado a Me Gusta");
					tvNumLike.setVisibility(4);
				}else{tvNumLike.setVisibility(0);
					if(offer.getNumber("numlike").intValue()==1){
					tvNumLike.setText(offer.getNumber("numlike").toString()+" usuario ha dado a Me Gusta");
				}else{
					tvNumLike.setText(offer.getNumber("numlike").toString()+" usuarios han dado a Me Gusta");
					}
				}
				// Paso las fechas a los edittexts
				SimpleDateFormat datef = new SimpleDateFormat("dd-MM-yyyy");
				String deadline = datef.format(offer.getDay("deadline").getTime());
				tvDeadline.setText("Válido hasta: "+deadline);
				String creation = datef.format(offer.getDate("offercreationdate"));
				tvCreationDate.setText("Fecha de creación: "+creation);
				// Tengo que leer el objeto commerce para poder acceder a sus datos
				Backbeam.read("commerce", offer.getObject("commerce").getId(), new ObjectCallback() {
					@Override
					public void success(BackbeamObject commerce) {
						
						// paso la dirección/coordenadas al textView correspondiente
						/*tvLocation.setText(String.valueOf(commerce.getLocation("placelocation").getLatitude())+
								","+String.valueOf(commerce.getLocation("placelocation").getLongitude()));*/
						//tvLocation.setText(commerce.getLocation("placelocation").getAddress());
						//**********************************************************
						Geocoder geocoder;
						List<Address> addresses = null;
						geocoder = new Geocoder(context);
						try {
							addresses = geocoder.getFromLocation(commerce.getLocation("placelocation").getLatitude(), commerce.getLocation("placelocation").getLongitude(), 1);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						String address = addresses.get(0).getAddressLine(0);
						String city = addresses.get(0).getAddressLine(1);
						String country = addresses.get(0).getAddressLine(2);
						util.log("pancratio: "+address + city + country);
				tvLocation.setText(address+"\n"+city+", "+country);
				setSupportProgressBarIndeterminateVisibility(false);
						//***********************************************************
						//Recibo las coordenadas del usuario para poder calcular la distancia hasta el Commerce
						SharedPreferences prefers = PreferenceManager.getDefaultSharedPreferences(context);
						String myLatitude = prefers.getString("latpos", "null");
						double latitude = Double.parseDouble(myLatitude);
						String myLongitude = prefers.getString("longpos", "null");
						double longitude = Double.parseDouble(myLongitude);
						
						//Mando latitud y long del usuario y del comercio para calcular la distancia
						haversine(commerce.getLocation("placelocation").getLatitude(),
								commerce.getLocation("placelocation").getLongitude(), 
								latitude, 
								longitude);
						//Habilito el boton para que el usuario pueda hacer like.
					btnLike.setEnabled(true);
						}});
				;}
		});

	}

	private void getPhoto(String idcommerce) {
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idcommerce);

		Query query = new Query("offer");
		query.setQuery("where this in ? join file", collection);
		query.fetch(100, 0, new FetchCallback() {
			@Override
			public void success(List<BackbeamObject> companies, int totalCount,
					boolean fromCache) {
				for (BackbeamObject company : companies) {
					BackbeamObject fileObject = company.getObject("file");
					if(fileObject!=null){
					TreeMap<String, Object> options = new TreeMap<String, Object>();
					options.put("width", 400);
					options.put("height",200);
					String logoURL = fileObject.composeFileURL(options);

					// Codigo para poner la foto en el imageView
					URL newurl = null;
					try {
						newurl = new URL(logoURL);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						Bitmap mIcon_val = BitmapFactory.decodeStream(newurl
								.openConnection().getInputStream());
						ivPhoto.setImageBitmap(mIcon_val);
						setSupportProgressBarIndeterminateVisibility(false);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}else{
						//ivPhoto.setImageDrawable(getResources().getDrawable( R.drawable.nophoto));
						setSupportProgressBarIndeterminateVisibility(false);
					}

				}
			}
		});
	}

	private void initUi() {
		setSupportProgressBarIndeterminateVisibility(true);
		// EDITEXTS
		tvDescription = (TextView) findViewById(R.id.tvViewOfferDescription);
		tvDeadline = (TextView) findViewById(R.id.tvViewOfferDeadline);
		tvCreationDate = (TextView) findViewById(R.id.tvViewOfferCreationDate);
		tvLocation = (TextView) findViewById(R.id.tvViewOfferLocation);
		tvNumLike = (TextView) findViewById(R.id.tvViewOfferNumlike);
		tvDistance = (TextView) findViewById(R.id.tvViewOfferDistance);
		// BOTONES
		btnLike = (ImageButton) findViewById(R.id.btnViewOfferLike);
		btnFlag = (ImageButton) findViewById(R.id.btnViewOfferFlag);
		// IMAGEVIEW
		ivPhoto = (ImageView) findViewById(R.id.ivViewOfferPhoto);
		
		
		btnLike.setEnabled(false);
		btnLike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//insertLike(idofferparameter);
				new RefreshDataTask().execute();
			}
		});
		btnFlag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final CharSequence[] items = { "Referencia Incorrecta",
						"Contenido Ofensivo", "Cancelar" };

				AlertDialog.Builder builder = new AlertDialog.Builder(
						ViewOffer.this);
				builder.setTitle("Elija el motivo: ");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						// Do something with the selection
						switch (item) {
						case 0:
							insertReport(idofferparameter, "incorrecto");
							break;
						case 1:
							insertReport(idofferparameter, "ofensivo");
							break;
						case 2:
							finish();
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	// METODO PARA INSERTAR LIKE
		private void insertLike(final String idoffer, final String idcommerce) {
			// CREO OBJETOS
			btnLike.setEnabled(false);
			
			//final BackbeamObject commerce = new BackbeamObject("commerce", idcommerce);
			final BackbeamObject like = new BackbeamObject("like");
			final BackbeamObject offer = new BackbeamObject("offer", idoffer);
			// Escribo los campos de "like" 
			like.setString("udid", getId());
			like.setDate("likedate", actualDate());
			// Compruebo el boolean y, si es TRUE, inserta un LIKE. Si es FALSE,
			// inserta un DISLIKE.
			// Por último, se hace el count.
			Backbeam.read("commerce", idcommerce, new ObjectCallback() {
				@Override
				public void success(BackbeamObject commerce) {
					if (lastlike == true) {
						like.setString("statuslike", "1");
						numbubble = commerce.getNumber("numbubble").intValue();
						commerce.setNumber("numbubble", numbubble+1);
						System.out.println("1");
						btnLike.setImageDrawable(getResources().getDrawable(R.drawable.unlike_icon));
						lastlike=false;
						
						
					} else {
						like.setString("statuslike", "0");
						numbubble = commerce.getNumber("numbubble").intValue();
						commerce.setNumber("numbubble", numbubble-1);
						System.out.println("0");
						btnLike.setImageDrawable(getResources().getDrawable(R.drawable.like_icon));
						lastlike=true;
						
					}
							commerce.save(new ObjectCallback() {
							@Override
							public void success(BackbeamObject object) {
								System.out.println("updated! :) "+object.getId());
								like.setObject("offer", offer);
								like.save(new ObjectCallback() {
									@Override
									public void success(BackbeamObject object) {
										System.out.println("like guardado");
										Query query = new Query("like");
										// Consulto los LIKE cuyo STATUSLIKE == "1".
										query.setQuery("where statuslike = ? and offer is ? ", "1",
												idoffer);
										// query.setQuery("where this in ? join last 10 like having statuslike = ?",idoffer,
										// "1");
										System.out.println("Tras la consulta del success");
										query.fetch(100, 0, new FetchCallback() {
											@Override
											public void success(List<BackbeamObject> objects,
													final int totalCountLike, boolean fromCache) {
												
												System.out.println("totalCount: " + totalCountLike);
												Query query = new Query("like");
												// Consulto los LIKE cuyo STATUSLIKE == "1".
												query.setQuery("where statuslike = ? and offer is ? ",
														"0", idoffer);
												
												// query.setQuery("where this in ? join last 10 like having statuslike = ?",idoffer,"1");
												System.out.println("Tras la consulta del success");
												query.fetch(100, 0, new FetchCallback() {
													@Override
													public void success(List<BackbeamObject> objects,
															int totalCountDislike, boolean fromCache) {
														
														System.out.println("totalCount: "
																+ totalCountDislike);
														offer.setNumber("numlike", totalCountLike
																- totalCountDislike);
														offer.save(new ObjectCallback() {
															@Override
															public void success(BackbeamObject object) {
																
																System.out.println(object
																		.getString("description"));
																System.out.println(object
																		.getNumber("numlike"));
																if(offer.getNumber("numlike").intValue()==0){
																	//tvNumLike.setText("Ningún usuario ha dado a Me Gusta");
																	tvNumLike.setVisibility(4);
																}else{ tvNumLike.setVisibility(0);
																	if(offer.getNumber("numlike").intValue()==1){
																	tvNumLike.setText(offer.getNumber("numlike").toString()+" usuario ha dado a Me Gusta");
																}else{
																	tvNumLike.setText(offer.getNumber("numlike").toString()+" usuarios han dado a Me Gusta");
																	}
																}
																
																	btnLike.setEnabled(true);
																	setSupportProgressBarIndeterminateVisibility(false);
																
															}
														});
													}
												});
											}

										});
									}
								});
							}
						});
						}
					});
		}

	// METODO PARA OBTENER LA UDID DEL SMARTPHONE
	private String getId() {
		String id = android.provider.Settings.System.getString(
				super.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return id;
	}

	// METODO PARA OBTENER LA FECHA ACTUAL
	private Date actualDate() {
		Calendar calendar = new GregorianCalendar();
		final Date createdate = calendar.getTime();
		return createdate;
	}

	// METODO PARA CREAR DENUNCIA
	private void insertReport(final String idoffer, String reason) {

		BackbeamObject report = new BackbeamObject("report");
		final BackbeamObject offer = new BackbeamObject("offer", idoffer);

		report.setString("udid", getId());
		report.setDate("reportdate", actualDate());
		report.setString("reason", reason);
		report.setString("reportstatus", "pending");
		report.setObject("offer", offer);
		report.save(new ObjectCallback() {
			@Override
			public void success(BackbeamObject object) {
				offer.setString("offerstatus", "pending");
				offer.save(new ObjectCallback() {
					@Override
					public void success(BackbeamObject object) {
						System.out.println("Oferta "
								+ object.getString("description")
								+ " en supervision");
						System.out.println(offer.getString("description"));
						AlertDialog.Builder info = new AlertDialog.Builder(context);
						info.setTitle("Denuncia realizada");
						info.setMessage("Gracias, en breve analizaremos su solicitud.");
						info.setCancelable(false);
						info.setNeutralButton("Aceptar",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogo1, int id) {
										finish();
									}

								});
						
						info.show();
						
					}
				});
			}
		});
	}
	// Consulta para saber qué estado tiene el último like del usuario
	private void queryLike(String idoffer) {
		// SE PUEDE PASAR offerobject y meter en la query el id
		BackbeamObject offerobject = new BackbeamObject("offer", idoffer);

		Query query = new Query("like");
		query.setQuery("where udid = ? and offer is ? sort by created_at desc",
				getId(), offerobject);
		query.fetch(100, 0, new FetchCallback() {

			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
				if (totalCount == 0) {
					// NO HA HECHO CLIC ANTES
					//btnLike.setText(R.string.like);
					btnLike.setImageDrawable(getResources().getDrawable(R.drawable.like_icon));
					System.out.println("no ha hecho clic antes");
					lastlike = true;
					
					System.out.println("Estado del boolean: " + lastlike);
					
				} else {
					System.out.println("ha hecho clic antes");
					BackbeamObject likeobject = objects.get(0);
					System.out.println(totalCount);
					String status = "";
					status = likeobject.getString("statuslike");

					if (status.equals("1")) {
						// Deshabilitar boton
						//btnLike.setText(R.string.dislike);
						btnLike.setImageDrawable(getResources().getDrawable(R.drawable.unlike_icon));
						System.out.println("habilita boton");
						System.out.println("Estado del boolean: " + lastlike);
						lastlike = false;
					} else {
						// Habilitar boton
						//btnLike.setText(R.string.like);

						btnLike.setImageDrawable(getResources().getDrawable(R.drawable.like_icon));
						System.out.println("deshabilita boton");
						System.out.println("Estado del boolean: " + lastlike);
						lastlike = true;
					}
				}
			}
		});

		btnLike.setEnabled(true);
		
	}


	//METODO PARA CALCULAR LA RUTA
	private void haversine(double placelat, double placelon, double userlat, double userlon) {
		String distancestring ="";
		Double distancedouble =(double) 0;
		double dLat = Math.toRadians(userlat - placelat);
		double dLon = Math.toRadians(userlon - placelon);
		placelat = Math.toRadians(placelat);
		userlat = Math.toRadians(userlat);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(placelat) * Math.cos(userlat);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		
		distancedouble = radio*c;
		
		//A LA HORA DE PONER EL TEXTO EN LA ETIQUETA, SI PASA DE 1000metros, escribo la distancedouble en KM.
		if(distancedouble>= 1000){
		// EJEMPLO: 23400,123 --> 23,4
			distancedouble = distancedouble / 1000;
			distancedouble = (double)Math.round(distancedouble * 10) /10;
			distancestring = distancedouble.toString();
			util.log("Dist: "+distancestring+" km.");
			tvDistance.setText("Dist: "+distancestring+" km.");
		}else{
			int distanceint = distancedouble.intValue();
			util.log("Dist: "+distanceint+" m.");
			tvDistance.setText("Dist: "+distanceint+" m.");
		}
		
		
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
			new LoadDataTask().execute();
			return true;
        	case android.R.id.home: 
			finish();
        		return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu3, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	//Async Task para cargar datos al abrir la activity
	private class LoadDataTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			util.log("recorremos post execute mydata");
			loadData(idofferparameter);
			queryLike(idofferparameter);
			getPhoto(idofferparameter);//idcommerceparameter);
			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			util.log("recorremos pre execute");
			setSupportProgressBarIndeterminateVisibility(true);
			util.log("mostramos dialog mydata");
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			util.log("doInBackgroundRecorrido mydata");

			return true;
		}
	}
	
	//Async Task para actualizar datos al hacer clic en LIKE/DISLIKE
	private class RefreshDataTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			util.log("recorremos post execute mydata");
			insertLike(idofferparameter, idcommerceparameter);
			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			util.log("recorremos pre execute");
			setSupportProgressBarIndeterminateVisibility(true);
			util.log("mostramos dialog mydata");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			util.log("doInBackgroundRecorrido mydata");
			
			return true;
		}
	}
	
}
