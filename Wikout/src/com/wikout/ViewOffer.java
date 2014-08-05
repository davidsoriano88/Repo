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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewOffer extends ActionBarActivity {

	TextView tvReport,tvDistance,tvDescription,tvDeadline,tvCreationDate,tvLocation,tvNumLike;
	ImageView ivPhoto;
	ImageButton btnRoute;
	View viewLike;
	Context context = this;
	Util util = new Util();
	
	String idofferparameter = "",idcommerceparameter = "", placename = "";
	
	boolean lastlike= false;
	//lastlike TRUE: Si pulsa el boton LIKE, inserta STATUSLIKE "1"
	//lastlike FALSE: Si pulsa el boton DISLIKE, inserta STATUSLIKE "0"
	int numbubble = 0;
	protected boolean enableOk=true;
	double userlat, userlon,commercelat, commercelon;
	String userlocation;
	
	//Radio de la tierra (en metros)
	final static double radio = 6371000;
	final static double distancedouble = 0;
	SharedPreferences prefers = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.view_offer);
		FontUtils.setRobotoFont(context, ((Activity) context).getWindow()
				.getDecorView());
		 if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			 prefers = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		 }else{
		prefers=getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
		 }
		 prefers = getSharedPreferences(
					"MisPreferencias", Context.MODE_PRIVATE);
		util.projectData(context);
	//	prefers = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		System.out.println("PREFERS LAT: "+String.valueOf(prefers.getFloat("latpos", 0)));
		System.out.println("PREFERS LON: "+String.valueOf(prefers.getFloat("longpos", 0)));
		userlat = (double)prefers.getFloat("latpos", 0);
		userlon = (double)prefers.getFloat("longpos", 0);
		System.out.println(" LAT: "+userlat);
		//util.showProgressDialog(context, 1900);
		initUi();
		initQueries();

	}

	private void initQueries() {
		
		Bundle bundle = getIntent().getExtras();
		idofferparameter = bundle.getString("idoffer");
		idcommerceparameter = bundle.getString("idcommerce");
		placename = bundle.getString("placename");
		userlocation = bundle.getString("userlocation");
		//userlon = bundle.getDouble("userlongitude");
		//userlat  = bundle.getDouble("userlatitude");
		
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

				int duration = dateFormulae(offer.getDay("deadline").getTime());
				System.out.println("Duration: "+duration);
				if (duration < 10) {
					tvDeadline.setTextColor(Color.RED);
					
					if (duration == 1) {
						tvDeadline.setText("Válido hasta mañana");
					}
					if (duration == 0) {
						tvDeadline.setText("Válido hasta hoy");
					}else{
						tvDeadline.setText("Válido hasta dentro de: " + duration
								+ " días");
					}
				} else {
					tvDeadline.setText("Válido hasta dentro de: " + duration + " días");
				}
				
				String creation = df4.format(offer.getDate("offercreationdate"));
				tvCreationDate.setText("Creado el: "+creation);
				
				
				
				
				// Tengo que leer el objeto commerce para poder acceder a sus datos
				Backbeam.read("commerce", offer.getObject("commerce").getId(), new ObjectCallback() {
					@Override
					public void success(BackbeamObject commerce) {

						//tvLocation.setText("¿Dónde está?\n"+commerce.getLocation("placelocation").getAddress().toString());
						Geocoder geocoder = new Geocoder(context);
						List<Address> addresses = null;
						
						try {
							addresses = geocoder.getFromLocation(commercelat=commerce.getLocation("placelocation").getLatitude(), commercelon=commerce.getLocation("placelocation").getLongitude(), 1);
							
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						String address = addresses.get(0).getAddressLine(0);
						String city = addresses.get(0).getAddressLine(1);
						String country = addresses.get(0).getAddressLine(2);

						tvLocation.setText("¿Dónde está?\n"+address+"\n"+city+", "+country);
						// paso la dirección/coordenadas al textView correspondiente
						/*tvLocation.setText(String.valueOf(commerce.getLocation("placelocation").getLatitude())+
								","+String.valueOf(commerce.getLocation("placelocation").getLongitude()));*/
						//tvLocation.setText(commerce.getLocation("placelocation").getAddress());
						//**********************************************************
						System.out.println("DIST:"+"\nuserlat: "+userlat+"\nuserlong: "+userlon
								+"\ncommercelat: "+commercelat+"\ncommercelon: "+commercelon);
						
						//Mando latitud y long del usuario y del comercio para calcular la distancia
						haversine(commercelat,commercelon,userlat,userlon);
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
		
		//FontUtils.setRobotoFont(getApplicationContext(), (ViewGroup) ((Activity) getApplicationContext()).getWindow().getDecorView());
		
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
					dialogLocation.setTitle("¿Cómo va?");
					dialogLocation.setMessage("Elija su modo de desplazamiento:");
					dialogLocation.setCancelable(true);
					dialogLocation.setPositiveButton("A pie",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogo1, int id) {
									
									String url = "http://maps.google.com/maps?saddr="+userlat+","+userlon+"&daddr="+commercelat+","+commercelon+"&dirflg=w";
									Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)); 
									intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
									startActivity(intent); 
									
								}

							});
					dialogLocation.setNegativeButton("En coche",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogo1, int id) {
									String url = "http://maps.google.com/maps?saddr="+userlat+","+userlon+"&daddr="+commercelat+","+commercelon+"&dirflg=d";
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
			
			if (isNetworkAvailable() == true) {
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
							// Por último, se hace el count.
							Backbeam.read("commerce", idcommerce, new ObjectCallback() {
								@Override
								public void success(BackbeamObject commerce) {
									if (lastlike == true) {
										like.setString("statuslike", "1");
										numbubble = commerce.getNumber("numbubble").intValue();
										commerce.setNumber("numbubble", numbubble+1);
										lastlike=false;
										enableOk=false;
										supportInvalidateOptionsMenu();
										
										
									} else {
										like.setString("statuslike", "0");
										numbubble = commerce.getNumber("numbubble").intValue();
										commerce.setNumber("numbubble", numbubble-1);
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
																					// tvNumLike.setText("Ningún usuario ha dado a Me Gusta");
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
			
			} else {
				util.showInfoDialog(context, "Lo sentimos",
						"Es necesaria conexión a internet");
			}
			
			
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

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	// METODO PARA CREAR DENUNCIA
	private void insertReport(final String idoffer, String reason) {

		if (isNetworkAvailable() == true) {
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
		
		} else {
			util.showInfoDialog(context, "Lo sentimos",
					"Es necesaria conexión a internet");
		}
		
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

	public static int dateFormulae(Date fechafinal) {

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
		String shareBody = "Estoy viendo la oferta de "+placename+" en Wikout. Descárgatelo de aquí: "+"LINK";
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Subject Here");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}
}
