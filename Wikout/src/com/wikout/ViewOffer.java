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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import model.FontUtils;
import utils.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewOffer extends ActionBarActivity {

	TextView tvReport,tvDescription,tvDeadline,tvCreationDate,tvLocation,tvNumLike;
	static TextView tvDistance;
	ImageView ivPhoto;
	String idofferparameter = "",idcommerceparameter = "";
	Context context;
	Util util = new Util();
	ImageButton btnRoute;
	View viewLike;
	
	
	long startui;
	
	boolean lastlike= false;
	//lastlike TRUE: Si pulsa el boton LIKE, inserta STATUSLIKE "1"
	//lastlike FALSE: Si pulsa el boton DISLIKE, inserta STATUSLIKE "0"
	int numbubble = 0;
	protected boolean enableOk=true;
	double latitude, longitude;
	double commercelat, commercelon;
	
	
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

		FontUtils.setRobotoFont(context, (ViewGroup) ((Activity) context).getWindow().getDecorView());
		//util.showProgressDialog(context, 1900);
		initUi();
		initQueries();

	}

	private void initQueries() {
		
		Bundle bundle = getIntent().getExtras();
		idofferparameter = bundle.getString("idoffer");
		idcommerceparameter = bundle.getString("idcommerce");
		latitude = bundle.getDouble("latitude");
		longitude = bundle.getDouble("longitude");
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
					//tvNumLike.setText("Ning�n usuario ha dado a Me Gusta");
					tvNumLike.setVisibility(View.GONE);
					viewLike.setVisibility(View.GONE);
					
				}else{tvNumLike.setVisibility(0);
				viewLike.setVisibility(0);
					if(offer.getNumber("numlike").intValue()==1){
					
					tvNumLike.setText("Le han dado a "+getResources().getString(R.string.heart) +" "+offer.getNumber("numlike").toString()+" persona");
				}else{

					tvNumLike.setText("Le han dado a "+getResources().getString(R.string.heart) +" "+offer.getNumber("numlike").toString()+" personas");
					}
				}
				// Paso las fechas a los edittexts
				DateFormat df4 = DateFormat.getDateInstance(DateFormat.FULL);

				int duration = fechasdiferenciaendias(offer.getDay("deadline").getTime());
				System.out.println("Duration: "+duration);
				if (duration < 10) {
					tvDeadline.setTextColor(Color.RED);
					
					if (duration == 1) {
						tvDeadline.setText("V�lido hasta ma�ana");
					}
					if (duration == 0) {
						tvDeadline.setText("V�lido hasta hoy");
					}else{
						tvDeadline.setText("V�lido hasta dentro de: " + duration
								+ " d�as");
					}
				} else {
					tvDeadline.setText("V�lido hasta dentro de: " + duration + " d�as");
				}
				
				String creation = df4.format(offer.getDate("offercreationdate"));
				tvCreationDate.setText("Creado el: "+creation);
				// Tengo que leer el objeto commerce para poder acceder a sus datos
				Backbeam.read("commerce", offer.getObject("commerce").getId(), new ObjectCallback() {
					@Override
					public void success(BackbeamObject commerce) {
						
						// paso la direcci�n/coordenadas al textView correspondiente
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
				tvLocation.setText("�D�nde est�?\n"+address+"\n"+city+", "+country);
				
						//***********************************************************
						//Recibo las coordenadas del usuario para poder calcular la distancia hasta el Commerce
						SharedPreferences prefers = PreferenceManager.getDefaultSharedPreferences(context);
						String myLatitude = prefers.getString("latpos", "null");
						String myLongitude = prefers.getString("longpos", "null");
						double latitude = Double.parseDouble(myLatitude);
						double longitude = Double.parseDouble(myLongitude);
						commercelat=commerce.getLocation("placelocation").getLatitude();
						commercelon=commerce.getLocation("placelocation").getLongitude();
						
						//Mando latitud y long del usuario y del comercio para calcular la distancia
						haversine(commerce.getLocation("placelocation").getLatitude(),
								commerce.getLocation("placelocation").getLongitude(), 
								latitude, 
								longitude);
						//Habilito el boton para que el usuario pueda hacer like.
//					btnLike.setEnabled(true);
						}});
				;}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu3, menu);
		if (enableOk == true) {
			menu.findItem(R.id.actionlike).setEnabled(true).setVisible(true);
			menu.findItem(R.id.actionunlike).setEnabled(false).setVisible(false);
		} else {
			menu.findItem(R.id.actionlike).setEnabled(false).setVisible(false);
			menu.findItem(R.id.actionunlike).setEnabled(true).setVisible(true);

		}
		return super.onCreateOptionsMenu(menu);
	}
	


	
	@Override
	public void supportInvalidateOptionsMenu() {
		
		super.supportInvalidateOptionsMenu();
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
						//setSupportProgressBarIndeterminateVisibility(false);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}else{
						//ivPhoto.setImageDrawable(getResources().getDrawable( R.drawable.nophoto));
						//setSupportProgressBarIndeterminateVisibility(false);
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
		btnRoute = (ImageButton)findViewById(R.id.btnViewOfferGo);
		tvReport = (TextView) findViewById(R.id.tvViewOfferReport);
		viewLike = (View) findViewById(R.id.View01);
		
		Drawable img;
		Resources res = getResources();
		img = res.getDrawable(R.drawable.flag_icon);
		//You need to setBounds before setCompoundDrawables , or it couldn't display
		img.setBounds(0, 0, 20, 20);
		tvReport.setCompoundDrawables(img, null, null, null); 
		tvReport.setText(getResources().getString(R.string.report));
		makeTextViewHyperlink(tvReport);
		
		tvReport.setOnClickListener(new OnClickListener() {
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
		// BOTONES
		btnRoute.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
					AlertDialog.Builder dialogLocation = new AlertDialog.Builder(context);
					dialogLocation.setTitle("�C�mo va?");
					dialogLocation.setMessage("Elija su modo de desplazamiento:");
					dialogLocation.setCancelable(true);
					dialogLocation.setPositiveButton("A pie",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogo1, int id) {
									
									String url = "http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+commercelat+","+commercelon+"&dirflg=w";
									Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)); 
									intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
									startActivity(intent); 
									
								}

							});
					dialogLocation.setNegativeButton("En coche",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogo1, int id) {
									String url = "http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+commercelat+","+commercelon+"&dirflg=d";
									Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)); 
									intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
									startActivity(intent); 
									
								}
							});
					dialogLocation.show();

			}
		});
		// IMAGEVIEW
		ivPhoto = (ImageView) findViewById(R.id.ivViewOfferPhoto);
		
		
		
		
	}

	private void makeTextViewHyperlink(TextView tvReport) {

		  SpannableStringBuilder ssb = new SpannableStringBuilder();
		  ssb.append(tvReport.getText());
		  ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		  tvReport.setText(ssb, TextView.BufferType.SPANNABLE);
		
	}

	// METODO PARA INSERTAR LIKE
		private void insertLike(final String idoffer, final String idcommerce) {
			// CREO OBJETOS
//btnLike.setEnabled(false);
			
			//final BackbeamObject commerce = new BackbeamObject("commerce", idcommerce);
			final BackbeamObject like = new BackbeamObject("like");
			final BackbeamObject offer = new BackbeamObject("offer", idoffer);
			// Escribo los campos de "like" 
			like.setString("udid", getId());
			like.setDate("likedate", actualDate());
			// Compruebo el boolean y, si es TRUE, inserta un LIKE. Si es FALSE,
			// inserta un DISLIKE.
			// Por �ltimo, se hace el count.
			Backbeam.read("commerce", idcommerce, new ObjectCallback() {
				@Override
				public void success(BackbeamObject commerce) {
					if (lastlike == true) {
						like.setString("statuslike", "1");
						numbubble = commerce.getNumber("numbubble").intValue();
						commerce.setNumber("numbubble", numbubble+1);
						System.out.println("1");
						lastlike=false;
						enableOk=false;
						supportInvalidateOptionsMenu();
						
						
					} else {
						like.setString("statuslike", "0");
						numbubble = commerce.getNumber("numbubble").intValue();
						commerce.setNumber("numbubble", numbubble-1);
						System.out.println("0");
						lastlike=true;
						enableOk=true;
						supportInvalidateOptionsMenu();
						
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
																if (offer
																		.getNumber(
																				"numlike")
																		.intValue() == 0) {
																	// tvNumLike.setText("Ning�n usuario ha dado a Me Gusta");
																	tvNumLike
																			.setVisibility(View.GONE);

																	viewLike.setVisibility(View.GONE);
																} else {
																	tvNumLike.setVisibility(0);

																	viewLike.setVisibility(0);
																	if (offer
																			.getNumber(
																					"numlike")
																			.intValue() == 1) {
																		tvNumLike
																				.setText("Le han dado a "
																						+ getResources()
																								.getString(
																										R.string.heart)
																						+ " "
																						+ offer.getNumber(
																								"numlike")
																								.toString()
																						+ " persona");
																	} else {
																		tvNumLike
																				.setText("Le han dado a "
																						+ getResources()
																								.getString(
																										R.string.heart)
																						+ " "
																						+ offer.getNumber(
																								"numlike")
																								.toString()
																						+ " personas");
																	}
																}
																
//btnLike.setEnabled(true);
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
	// Consulta para saber qu� estado tiene el �ltimo like del usuario
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
					System.out.println("no ha hecho clic antes");
					lastlike = true;
					enableOk=true;
					supportInvalidateOptionsMenu();
					
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
						System.out.println("habilita boton");
						System.out.println("Estado del boolean: " + lastlike);
						lastlike = false;
						enableOk=false;
						supportInvalidateOptionsMenu();
					} else {
						// Habilitar boton
						//btnLike.setText(R.string.like);

						System.out.println("deshabilita boton");
						System.out.println("Estado del boolean: " + lastlike);
						lastlike = true;
						enableOk=true;
						supportInvalidateOptionsMenu();
					}
				}
			}
		});

//btnLike.setEnabled(true);
		
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
		setSupportProgressBarIndeterminateVisibility(false);
		
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
       /* case R.id.refresh:
			new LoadDataTask().execute();
			return true;*/
			
        case R.id.actionlike:
        	new RefreshDataTask().execute();
        	return true;
        	
        case R.id.actionunlike:
        	new RefreshDataTask().execute();
        	return true;
        	
        case android.R.id.home: 
			finish();
        		return true;
        case R.id.actionshare:
        	shareIntent();
        	
        default:
            return super.onOptionsItemSelected(item);
        }
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

	public static int fechasdiferenciaendias(Date fechafinal) {

		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

		String fechafinalstring = df.format(fechafinal);
		try {
			fechafinal = df.parse(fechafinalstring);
		} catch (ParseException ex) {
		}
		Date dt = new Date();
		long fechainicialms = dt.getTime();

		long fechafinalms = fechafinal.getTime();
		long diferencia = fechafinalms - fechainicialms;
		double dias = Math.floor(diferencia / (1000 * 60 * 60 * 24));
		return ((int) dias);
	}
	
	
	public void shareIntent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "Here is the share content body";
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Subject Here");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}
}
