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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewOffer extends ActionBarActivity {

	TextView tvDescription, tvDeadline, tvCreationDate,tvLocation,tvNumLike;
	static TextView tvDistance;
	Button btnLike, btnFlag;
	ImageView ivPhoto;
	String idofferparameter = "",idcommerceparameter = "";
	Context context;
	Util util = new Util();
	
	boolean lastlike= false;
	//lastlike TRUE: Si pulsa el boton LIKE, inserta STATUSLIKE "1"
	//lastlike FALSE: Si pulsa el boton DISLIKE, inserta STATUSLIKE "0"
	boolean statuslike=false;
	
	//Radio de la tierra (en metros)
	final static double radio = 6371000;
	final static double distancedouble = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_offer);
		context=this;
		util.projectData(context);
		initUi();
		initQueries();

	}

	private void initQueries() {
		Bundle bundle = getIntent().getExtras();
		idofferparameter = bundle.getString("idoffer");
		idcommerceparameter = bundle.getString("idcommerce");
		loadData(bundle.getString("idoffer"));
		queryLike(idofferparameter);
		getPhoto(bundle.getString("idcommerce"));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	private void loadData(String idoffer) {
		
		Backbeam.read("offer", idoffer, new ObjectCallback() {
			@Override
			public void success(BackbeamObject offer) {
				tvDescription.setText("Descripción: "+offer.getString("description"));
				tvNumLike.setText(offer.getNumber("numlike").toString());
				// Paso las fechas a los edittexts
				SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy");
				String deadline = format1.format(offer.getDay("deadline")
						.getTime());
				tvDeadline.setText("Válido hasta: "+deadline);
				String creation = format1.format(offer
						.getDate("offercreationdate"));
				tvCreationDate.setText("Fecha de creación: "+creation);
				// Lo mismo con location
				Backbeam.read("commerce", offer.getObject("commerce").getId(), new ObjectCallback() {
					@Override
					public void success(BackbeamObject commerce) {
						SharedPreferences prefers = PreferenceManager.getDefaultSharedPreferences(context);
						String myLatitude = prefers.getString("latpos", "null");
						double latitude = Double.parseDouble(myLatitude);
						String myLongitude = prefers.getString("longpos", "null");
						double longitude = Double.parseDouble(myLongitude);
						
						/*tvLocation.setText(String.valueOf(commerce.getLocation("placelocation").getLatitude())+
								","+String.valueOf(commerce.getLocation("placelocation").getLongitude()));*/
						tvLocation.setText(commerce.getLocation("placelocation").getAddress());
						haversine(commerce.getLocation("placelocation").getLatitude(),
								commerce.getLocation("placelocation").getLongitude(), 
								latitude, 
								longitude);
						
						btnLike.setEnabled(true);
					}});
			}
		});

	}

	protected void getPhoto(String idcommerce) {
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idcommerce);

		Query query = new Query("commerce");
		query.setQuery("where this in ? join file", collection);
		query.fetch(100, 0, new FetchCallback() {
			@Override
			public void success(List<BackbeamObject> companies, int totalCount,
					boolean fromCache) {
				for (BackbeamObject company : companies) {
					System.out.println("dentro de success foto");
					BackbeamObject fileObject = company.getObject("file");
					TreeMap<String, Object> options = new TreeMap<String, Object>();
					options.put("width", 160);
					options.put("height", 80);
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
						util.log("icono cargado");
						ivPhoto.setImageBitmap(mIcon_val);
						// image.setImageBitmap(mIcon_val);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
	}

	private void initUi() {
		// EDITEXTS
		tvDescription = (TextView) findViewById(R.id.tvViewOfferDescription);
		tvDeadline = (TextView) findViewById(R.id.tvViewOfferDeadline);
		tvCreationDate = (TextView) findViewById(R.id.tvViewOfferCreationDate);
		tvLocation = (TextView) findViewById(R.id.tvViewOfferLocation);
		tvNumLike = (TextView) findViewById(R.id.tvViewOfferNumlike);
		tvDistance = (TextView) findViewById(R.id.tvViewOfferDistance);
		// BOTONES
		btnLike = (Button) findViewById(R.id.btnViewOfferLike);
		btnFlag = (Button) findViewById(R.id.btnViewOfferFlag);
		// IMAGEVIEW
		ivPhoto = (ImageView) findViewById(R.id.ivViewOfferPhoto);
		
		btnLike.setEnabled(false);
		btnLike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				insertLike(idofferparameter);
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
							try {
								finalize();

							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();

			}
		});

		/*	
*/

	}


	// METODO PARA INSERTAR LIKE
		protected void insertLike(final String idoffer) {
			// CREO OBJETOS
			btnLike.setEnabled(false);
			final BackbeamObject like = new BackbeamObject("like");
			final BackbeamObject offer = new BackbeamObject("offer", idoffer);
			// Escribo los campos de "like"
			like.setString("udid", getId());
			like.setDate("likedate", actualDate());
			// Compruebo el boolean y, si es TRUE, inserta un LIKE. Si es FALSE,
			// inserta un DISLIKE.
			// Por último, se hace el count.
			if (lastlike == true) {
				like.setString("statuslike", "1");
				System.out.println("1");
				lastlike=false;
				btnLike.setText(R.string.dislike);
				
			} else {
				like.setString("statuslike", "0");
				System.out.println("0");
				lastlike=true;
				btnLike.setText(R.string.like);
			}
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
							// query.setQuery("where this in ? join last 10 like having statuslike = ?",idoffer,
							// "1");
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
											tvNumLike.setText(String.valueOf(object.getNumber("numlike")));
										}
									});
								}
							});
						}

					});
				}
			});
			btnLike.setEnabled(true);
		}

	// METODO PARA OBTENER LA UDID DEL SMARTPHONE
	protected String getId() {
		String id = android.provider.Settings.System.getString(
				super.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return id;
	}

	// METODO PARA OBTENER LA FECHA ACTUAL
	protected Date actualDate() {
		Calendar calendar = new GregorianCalendar();
		final Date createdate = calendar.getTime();
		return createdate;
	}

	// METODO PARA CREAR DENUNCIA
	protected void insertReport(final String idoffer, String reason) {

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
						util.showToast(getApplicationContext(),
								"Informe enviado");
					}
				});
			}
		});
	}

	protected void queryLike(String idoffer) {
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
					btnLike.setText(R.string.like);
					System.out.println("no ha hecho clic antes");
					lastlike = true;
					System.out.println("Estado del boolean: " + lastlike);
					statuslike = true;
				} else {
					System.out.println("ha hecho clic antes");
					BackbeamObject likeobject = objects.get(0);
					System.out.println(totalCount);
					String status = "";
					status = likeobject.getString("statuslike");

					if (status.equals("1")) {
						// Deshabilitar boton
						btnLike.setText(R.string.dislike);
						System.out.println("habilita boton");
						System.out.println("Estado del boolean: " + lastlike);
						lastlike = false;
						statuslike = false;
					} else {
						// Habilitar boton
						btnLike.setText(R.string.like);
						System.out.println("deshabilita boton");
						System.out.println("Estado del boolean: " + lastlike);
						lastlike = true;
						statuslike = true;

					}
				}
			}
		});

		btnLike.setEnabled(true);
	}


	//METODO PARA CALCULAR LA RUTA
	public void haversine(double placelat, double placelon, double userlat, double userlon) {
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
			tvDistance.setText(distancestring+" km.");
		}else{
			int distanceint = distancedouble.intValue();
			util.log("Dist: "+distanceint+" m.");
			tvDistance.setText(distanceint+" m.");
		}
		
		
	}

	
}
