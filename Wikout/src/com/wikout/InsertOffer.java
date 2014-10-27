package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamException;
import io.backbeam.BackbeamObject;
import io.backbeam.CollectionConstraint;
import io.backbeam.FetchCallback;
import io.backbeam.FileUpload;
import io.backbeam.Location;
import io.backbeam.ObjectCallback;
import io.backbeam.Query;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import model.FontUtils;
import utils.Photo;
import utils.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class InsertOffer extends ActionBarActivity {

	public ActionBarActivity actionbarAct;

	ImageView ivPhoto;
	EditText etDescription;
	Button btnInsertOffer, dateLimit, btnLocationCommerce;
	Spinner spnCategory;
	TextView tvPlacenameLabel, tvPlacename, tvLocation, tvLocationLabel,
			tvChangeCommerce;
	View viewCommerce;

	LinearLayout layoutInsertOfferCommerce, layoutInsertOfferSelectCommerce;
	// Otras variables
	String photoPath, idoferta, idphoto = "", photoName, url;

	// Variables para controlar la fecha
	int year, month, day, existPhoto = 0, enter;
	Double userlat, userlon, commercelat, commercelon;
	private Date deadline;

	// variables para control de fotografias
	File photo;

	// constantes utilizadas para lanzar intents
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int LOAD_IMAGE = 3;
	static final int DATE_DIALOG_ID = 999;
	static final int REQUEST_LOGIN = 20;

	// Location de prueba
	public Location locationbm;
	Bundle bundle;
	// Constante para el picker

	final Context context = this;
	public Intent map;
	Util util = new Util();

	// Para el bundle de ListCommerce e InsertCommerce
	String idcommerce, placename, idObjectPhoto = null, position,
			commerceIconlink;

	String location;

	private String category;

	// Crear Arrays
	String compras[] = { "bakery", "bicycle_store", "book_store", "car_dealer",
			"car_rental", "clothing_store", "convenience_store",
			"department_store", "electronics_store", "florist",
			"furniture_store", "grocery_or_supermarket", "hardware_store",
			"home_goods_store", "jewelry_store", "liquor_store", "locksmith",
			"meal_delivery", "meal_takeaway", "movie_rental", "pet_store",
			"shoe_store", "storage", "store", "travel_agency", "establishment" };
	String ocio[] = { "amusement_park", "aquarium", "art_gallery", "bar",
			"bowling_alley", "cafe", "casino", "food", "gym", "library",
			"movie_theater", "museum", "night_club", "park", "restaurant",
			"shopping_mall", "spa", "stadium", "zoo" };
	String servicios[] = { "airport", "campground", "cemetery", "church",
			"funeral_home", "hindu_temple", "mosque", "subway_station",
			"synagogue", "train_station", "university" };
	String otros[] = { "accounting", "bank", "beauty_salon", "bus_station",
			"car_repair", "car_wash", "city_hall", "courthouse", "dentist",
			"doctor", "electrician", "finance", "fire_station", "hair_care",
			"health", "hospital", "insurance_agency", "laundry", "lawyer",
			"local_government_office", "lodging", "moving_company", "painter",
			"parking", "pharmacy", "physiotherapist", "plumber", "police",
			"post_office", "real_estate_agency", "roofing_contractor",
			"rv_park", "school", "veterinary_care", "atm", "embassy",
			"gas_station", "general_contractor", "place_of_worship",
			"taxi_stand" };
	List<String> compraslist = Arrays.asList(compras);
	List<String> ociolist = Arrays.asList(ocio);
	List<String> servicioslist = Arrays.asList(servicios);
	List<String> otroslist = Arrays.asList(otros);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.insert_offer);
		setSupportProgressBarIndeterminateVisibility(true);
		// prefers = PreferenceManager.getDefaultSharedPreferences(context);
		FontUtils.setRobotoFont(context, ((Activity) context).getWindow()
				.getDecorView());

		actionbarAct = this;
		util.projectData(context);

		bundle = getIntent().getExtras();
		initUI();
		if (bundle != null) {

			idcommerce = bundle.getString("idcommerce");
			placename = bundle.getString("placename");
			location = bundle.getString("location");
			layoutInsertOfferSelectCommerce.setVisibility(View.GONE);

			layoutInsertOfferCommerce.setVisibility(0);

			/*
			 * 
			 * tvChangeCommerce.setVisibility(0); tvPlacename.setVisibility(0);
			 * tvPlacenameLabel.setVisibility(0); tvLocation.setVisibility(0);
			 * tvLocationLabel.setVisibility(0); viewCommerce.setVisibility(0);
			 */

			tvPlacename.setText(placename);
			System.out.println("PLACENAME: " + placename);
			StringTokenizer tokens = new StringTokenizer(location, ":\"");
			String first = tokens.nextToken();// this will contain "Fruit"
			String second = tokens.nextToken();// this will contain
												// " they taste good"
			String third = tokens.nextToken();
			String fourth = tokens.nextToken();
			String fifth = tokens.nextToken();
			String sixth = tokens.nextToken();

			/*
			 * String address = location.; String city =
			 * addresses.get(0).getAddressLine(1); String country =
			 * addresses.get(0).getAddressLine(2);
			 * 
			 * tvLocation.setText("¿Dónde está?\n"+address+"\n"+city+", "+country
			 * );
			 */

			System.out.println(second);
			System.out.println(fourth);
			System.out.println(sixth);

			tvLocation.setText(second + "\n" + fourth + ", " + sixth);

			btnInsertOffer.setBackgroundColor(getResources().getColor(
					R.color.mainColor));
		} else {

			layoutInsertOfferSelectCommerce.setVisibility(0);
			layoutInsertOfferCommerce.setVisibility(View.GONE);

			/*
			 * viewCommerce.setVisibility(View.GONE);
			 * tvChangeCommerce.setVisibility(View.GONE);
			 * tvPlacename.setVisibility(View.GONE);
			 * tvPlacenameLabel.setVisibility(View.GONE);
			 * tvLocation.setVisibility(View.GONE);
			 * tvLocationLabel.setVisibility(View.GONE);
			 */

		}
	}

	private void initUI() {

		getSupportActionBar().setTitle("Nueva Oferta");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		map = new Intent(getApplicationContext(), Map.class);

		layoutInsertOfferSelectCommerce = (LinearLayout) findViewById(R.id.layoutInsertOfferSelectCommerce);
		layoutInsertOfferCommerce = (LinearLayout) findViewById(R.id.layoutInsertOfferCommerce);
		ivPhoto = (ImageView) findViewById(R.id.ivInsertOfferPhoto);
		viewCommerce = (View) findViewById(R.id.viewCommerce);
		tvChangeCommerce = (TextView) findViewById(R.id.tvInsertOfferChangeCommerce);
		etDescription = (EditText) findViewById(R.id.etInsertOfferDescription);
		tvPlacename = (TextView) findViewById(R.id.tvInsertOfferPlacename);
		tvPlacenameLabel = (TextView) findViewById(R.id.tvInsertOfferPlacenamelabel);
		tvLocation = (TextView) findViewById(R.id.tvInsertOfferCommerceLocation);
		tvLocationLabel = (TextView) findViewById(R.id.tvInsertOfferCommerceLocationlabel);
		btnInsertOffer = (Button) findViewById(R.id.btnInsertOfferOk);

		addListenerOnButton();
		setCurrentDateOnView();
		makeTextViewHyperlink(tvChangeCommerce);

		tvChangeCommerce.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				if (util.isNetworkAvailable(context) == true) {
					dialogGetLocation();
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexión a internet");
				}
			}
		});

		btnLocationCommerce = (Button) findViewById(R.id.btnInsertOfferCommerce);

		if (etDescription.isFocused()) {
			btnInsertOffer.requestFocus();
		}

		// Lo ponemos a escuchar para cuando sea pulsado

		btnLocationCommerce.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				if (util.isNetworkAvailable(context) == true) {
					dialogGetLocation();
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexión a internet");
				}
			}

		});

		btnInsertOffer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (util.isNetworkAvailable(context) == true) {
					// Digo que viene de InsertOffer
					Util.setPreferenceInt(context, "place", 3);

					if (etDescription.getText().length() == 0) {
						util.log("aceptar1");
						dialogIncompleteFields();

					} else if (tvPlacename.getText().length() == 0) {
						AlertDialog.Builder dialogIncomplete = new AlertDialog.Builder(
								context);
						dialogIncomplete.setTitle("Información incompleta");
						dialogIncomplete
								.setMessage("Selecciona el comercio de la oferta, por favor.");
						dialogIncomplete.setCancelable(false);
						dialogIncomplete.setNeutralButton("Aceptar",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialogo1, int id) {

									}

								});

						dialogIncomplete.show();
					} else if (Util.getPreferenceBoolean(context, "login") == true) {
						System.out.println("entra al primer if de insertoffer");
						if (photo != null) {
							System.out
									.println("entra al segudno if de insertoffer");
							setSupportProgressBarIndeterminateVisibility(true);
							btnInsertOffer.setEnabled(false);
							insertOfferPhoto(actualDate());
						} else {
							System.out
									.println("entra al else del if de insertoffer");
							setSupportProgressBarIndeterminateVisibility(true);
							util.log("no hay foto");
							btnInsertOffer.setEnabled(false);
							insertNewOffer(actualDate(), idcommerce,
									idObjectPhoto);

						}
					} else {
						System.out.println("Entra al else de InsertOffer");
						// CREO EL INTENT PARA IR A LOGINACTIVITY
						Intent in = new Intent(context, LoginActivity.class);
						in.putExtra("procedencia", 3);
						startActivityForResult(in, REQUEST_LOGIN);
					}

					// imageClicked(v);
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexión a internet");
				}
			}
		});
		setSupportProgressBarIndeterminateVisibility(false);

	}

	public void dialogGetLocation() {
		AlertDialog.Builder dialogLocation = new AlertDialog.Builder(this);
		dialogLocation.setTitle("Ubicación");
		dialogLocation.setMessage("¿Cómo prefieres indicar la ubicación del establecimiento?:");
		dialogLocation.setCancelable(true);
		dialogLocation.setPositiveButton("Ubicación actual",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						enter = 0;

						// prefers =
						// PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						System.out.println("PREFERS LAT: "
								+ Util.getPreferenceDouble(context, "latpos"));
						System.out.println("PREFERS LON: "
								+ Util.getPreferenceDouble(context, "longpos"));
						userlat = Util.getPreferenceDouble(context, "latpos");

						userlon = Util.getPreferenceDouble(context, "longpos");

						// btnLocation.setText(latitude +","+longitude);
						// util.showToast(context,latitude+","+longitude);
						Intent listCommerce = new Intent(context,
								CommerceList.class);
						System.out.println("Insert offer: LAT " + userlat);
						listCommerce.putExtra("pointlat", userlat);
						listCommerce.putExtra("pointlon", userlon);
						startActivityForResult(listCommerce, 30);
					}

				});
		dialogLocation.setNegativeButton("Indicar en el mapa",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						/*
						 * Bundle bundle = getIntent().getExtras(); latitude =
						 * bundle.getDouble("latiMain"); longitude =
						 * bundle.getDouble("longiMain");
						 */
						enter = 1;
						Intent listCommerce = new Intent(context,
								CommerceList.class);
						listCommerce.putExtra("enter", enter);
						startActivityForResult(listCommerce, 30);

					}
				});
		dialogLocation.show();
	}

	public void dialogIncompleteFields() {
		AlertDialog.Builder dialogIncomplete = new AlertDialog.Builder(this);
		dialogIncomplete.setTitle("Información incompleta");
		dialogIncomplete
				.setMessage("Rellena los campos Incompletos, por favor.");
		dialogIncomplete.setCancelable(false);
		dialogIncomplete.setNeutralButton("Aceptar",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {

					}

				});

		dialogIncomplete.show();
	}

	private void setCurrentDateOnView() {

		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 3;
		day = c.get(Calendar.DAY_OF_MONTH);
		System.out.println("Fecha: " + day + ":" + month + ":" + year);
		if (month >= 12) {
			month = month - 12;
			year++;
		}
		// set current date into textview
		dateLimit.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(day).append("-").append(month + 1).append("-")
				.append(year).append(" "));

		c.set(year, month, day);
		deadline = c.getTime();
	}

	private void addListenerOnButton() {

		dateLimit = (Button) findViewById(R.id.btnInsertOfferDeadline);
		dateLimit.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {

				showDialog(DATE_DIALOG_ID);

			}

		});

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			// set date picker as current date
			return new DatePickerDialog(this, datePickerListener, year, month,
					day);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

		// cuando el cuadro de dialogo se cierre, se ejecutara este metodo por
		// debajo
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			year = selectedYear;
			month = selectedMonth;
			day = selectedDay;

			// pone la fecha elegida en el textview
			dateLimit.setText(new StringBuilder().append(day).append("-")
					.append(month + 1).append("-").append(year).append(" "));

			Calendar calendar = new GregorianCalendar(selectedYear,
					selectedMonth, selectedDay);
			deadline = calendar.getTime();

		}
	};

	private String commercestreet;

	private String googleid;

	// METODO PARA INSERTAR OFERTA
	protected void insertNewOffer(final Date createdate,
			final String idcommerce, final String idfile) {
		// Creo el objeto "offer"
		final BackbeamObject offer = new BackbeamObject("offer");

		// HACER SELECT PARA COMPROBAR SI EL COMERCIO ESTA INSERTADO
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idcommerce);
		// HAGO LA CONSULTA DEL COMERCIO EN CONCRETO UNIENDO OFERTAS
		Query query = new Query("commerce");
		query.setQuery("where this in ? ", collection);
		query.fetch(100, 0, new FetchCallback() {

			private Location locationCommerce;

			@Override
			public void success(List<BackbeamObject> commerces, int totalCount,
					boolean fromCache) {
				System.out.println("TOTAL DE COMERCIOS CON ESE NOMBRE: "
						+ totalCount);
				// RECORRO CADA COMERCIO
				if (totalCount != 0) {

					// Sacar idcommerce del bundle al volver a esta activity
					final BackbeamObject commerce = new BackbeamObject(
							"commerce", idcommerce);

					if (idfile != null) {
						final BackbeamObject file = new BackbeamObject("file",
								idfile);
						offer.setObject("file", file);
					}
					// inserto los valores de "offer"
					// util.log(idfile);
					offer.setString("description", etDescription.getText()
							.toString());
					offer.setDay("deadline", deadline);
					offer.setString("udid", getId());
					offer.setString("offerstatus", "ok");
					offer.setDate("offercreationdate", createdate);

					offer.setObject("commerce", commerce);
					offer.setNumber("numlike", 0);
					// TODAVIA NO CONTEMPLO LIKE NI REPORT YA QUE SE ACABA DE
					// CREAR
					// offer.addObject("like");
					// offer.addObject("report");

					offer.save(new ObjectCallback() {// **************************************************************************
						@Override
						public void success(BackbeamObject offer) {
							Backbeam.read("commerce", idcommerce,
									new ObjectCallback() {
										@Override
										public void success(
												BackbeamObject commerce) {
											final int contador = commerce
													.getNumber("actualoffers")
													.intValue();
											System.out.println(commerce
													.getString("placename")
													+ "\nNumero de ofertas: "
													+ contador);
											commerce.setNumber("actualoffers",
													contador + 1);
											commerce.save(new ObjectCallback() {
												@Override
												public void success(
														BackbeamObject object) {
													System.out.println("updated! :) "
															+ object.getId());
													System.out.println(object
															.getString("placename")
															+ "\nNumero de ofertas: "
															+ object.getNumber(
																	"actualoffers")
																	.intValue());
													util.showToast(context,
															"Oferta insertada");

													btnInsertOffer
															.setEnabled(true);

													setSupportProgressBarIndeterminateVisibility(false);
													Intent i = new Intent(
															context,
															OfferList.class);
													i.putExtra("commerceid",
															object.getId()
																	.toString());
													startActivity(i);
													finish();
												}
											});
										}
									});

						}

					});

				} else {

					Geocoder geocoder = new Geocoder(context);
					List<Address> addresses = null;

					try {
						addresses = geocoder.getFromLocation(commercelat,
								commercelon, 1);

					} catch (IOException e) {
						e.printStackTrace();
					}

					String strLocation = addresses.get(0).toString();
					System.out.println(strLocation);
					locationCommerce = new Location(commercelat, commercelon,
							strLocation);

					// Extraigo la fecha actual
					Calendar calendar = new GregorianCalendar();
					final Date createdate = calendar.getTime();
					// Creo el objeto commerce
					final BackbeamObject commerce = new BackbeamObject(
							"commerce");

					// NO INSERTO FOTO DE MOMENTO
					/*
					 * if (objectphoto != null) { commerce.setObject("file",
					 * objectphoto); }
					 */

					// Relleno los campos del objeto
					commerce.setString("placename", placename);

					commerce.setLocation("placelocation", locationCommerce);
					commerce.setString("category", setCategory(category)); // CREAR
																			// METODO
					commerce.setDate("commercecreationdate", createdate);
					commerce.setString("udid", getId());
					commerce.setNumber("actualoffers", 0);
					commerce.setString("googleid", googleid);

					commerce.setNumber("numbubble", 0);

					// Guardo el objeto
					commerce.save(new ObjectCallback() {
						@Override
						public void success(final BackbeamObject commerce) {
							// Llamo al metodo insertPhoto para enlazarlo con la
							// foto

							// Sacar idcommerce del bundle al volver a esta
							// activity

							// inserto los valores de "offer"
							// util.log(idfile);
							System.out.println("OFERTA Y COMERCIO CREADOS");
							offer.setString("description", etDescription
									.getText().toString());
							offer.setDay("deadline", deadline);
							offer.setString("udid", getId());
							offer.setString("offerstatus", "ok");
							offer.setDate("offercreationdate", createdate);

							offer.setObject("commerce", commerce);
							offer.setNumber("numlike", 0);
							// TODAVIA NO CONTEMPLO LIKE NI REPORT YA QUE SE
							// ACABA DE
							// CREAR
							// offer.addObject("like");
							// offer.addObject("report");

							offer.save(new ObjectCallback() {// **************************************************************************
								@Override
								public void success(BackbeamObject offer) {
									Backbeam.read("commerce", commerce.getId(),
											new ObjectCallback() {
												@Override
												public void success(
														BackbeamObject commerce) {
													final int contador = commerce
															.getNumber(
																	"actualoffers")
															.intValue();
													System.out.println(commerce
															.getString("placename")
															+ "\nNumero de ofertas: "
															+ contador);
													commerce.setNumber(
															"actualoffers",
															contador + 1);
													commerce.save(new ObjectCallback() {
														@Override
														public void success(
																BackbeamObject object) {
															System.out.println("updated! :) "
																	+ object.getId());
															System.out.println(object
																	.getString("placename")
																	+ "\nNumero de ofertas: "
																	+ object.getNumber(
																			"actualoffers")
																			.intValue());
															util.showToast(
																	context,
																	"Oferta insertada");
															Intent i = new Intent(
																	context,
																	OfferList.class);
															i.putExtra(
																	"commerceid",
																	object.getId()
																			.toString());
															startActivity(i);

															finish();
														}
													});
												}
											});

								}

							});

						}
					});
				}
			}
		});

	}

	// METODO PARA SUBIR FOTO de offer
	protected void insertOfferPhoto(final Date createdate) {
		final BackbeamObject objectPhoto = new BackbeamObject("file");
		// Hay que pasarle el objeto de tipo file "foto"
		objectPhoto.uploadFile(new FileUpload(photo, "image/jpg"),
				new ObjectCallback() {
					@Override
					public void success(BackbeamObject photo) {
						System.out.println("success!! " + photo.getId());
						photo.setString("idphoto", photo.getId());
						photo.setDate("uploaddate", createdate);
						photo.save(new ObjectCallback() {
							@Override
							public void success(BackbeamObject objectPhoto) {
								System.out.println("foto subida con éxito!! "
										+ objectPhoto.getId());
								idObjectPhoto = objectPhoto.getId();

								insertNewOffer(createdate, idcommerce,
										idObjectPhoto);
							}
						});
					}

					@Override
					public void failure(BackbeamException exception) {
						System.out.println("failure!");
						exception.printStackTrace();
					}
				});

	}

	public String getId() {
		String id = android.provider.Settings.System.getString(
				super.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return id;
	}

	public void imageClicked(View imageView) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.question)
				.setPositiveButton(R.string.camera,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								onPhotoDialogPositiveClick();
							}
						})
				.setNegativeButton(R.string.galeria,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								onPhotoDialogNegativeClick();
							}
						});

		// launch dialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

	}

	// launching camera activity
	public void onPhotoDialogPositiveClick() {
		// launch camera
		Intent picture = new Intent(this, Photo.class);
		picture.putExtra("ACTION_REQUESTED", "CAMERA");
		startActivityForResult(picture, REQUEST_IMAGE_CAPTURE);
	}

	// if gallery has been pushed
	public void onPhotoDialogNegativeClick() {
		// launch gallery
		Intent picture = new Intent(this, Photo.class);
		picture.putExtra("ACTION_REQUESTED", "GALLERY");
		startActivityForResult(picture, LOAD_IMAGE);
	}

	// load image in imageView
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
			if (photo != null) {
				setSupportProgressBarIndeterminateVisibility(true);
				btnInsertOffer.setEnabled(false);
				insertOfferPhoto(actualDate());
			} else {
				setSupportProgressBarIndeterminateVisibility(true);
				util.log("no hay foto");
				btnInsertOffer.setEnabled(false);
				insertNewOffer(actualDate(), idcommerce, idObjectPhoto);

			}
		}
		// if there's no errors, the image is loaded
		if (resultCode == 10000) {

			Bundle photoBundle = new Bundle();
			util.log("bundle created");
			photoBundle = data.getExtras();

			photoPath = photoBundle.getString("path");
			photoName = photoBundle.getString("photoName");
			url = photoBundle.getString("url");
			photo = (File) photoBundle.get("photo");

			util.log(photoBundle.getString("photoName"));

			Bitmap cphoto = BitmapFactory.decodeFile(photoPath);
			ivPhoto.setImageBitmap(cphoto);
			existPhoto = 1;

		}
		if (requestCode == 30 && resultCode == RESULT_OK) {
			Bundle commerce = new Bundle();
			util.log("bundle created");
			commerce = data.getExtras();
			idcommerce = commerce.getString("idcommerce");
			placename = commerce.getString("placename");
			double latitude = commerce.getDouble("latitude");
			double longitude = commerce.getDouble("longitude");
			location = commerce.getString("location");

			googleid = commerce.getString("googleid");
			// commerceIconlink = bundle.getString("iconlink");
			category = commerce.getString("category");
			System.out.println("placename COMERCIO insertoffer: " + placename);
			System.out.println("CATEGORIA COMERCIO insertoffer: " + category);
			System.out.println("LOCATION GOOGLE INTENT: " + location);
			System.out.println("LOCATION COMERCIO: " + latitude + ", "
					+ longitude);
			Geocoder geocoder = new Geocoder(context);
			List<Address> addresses = null;

			try {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);

			} catch (IOException e) {
				e.printStackTrace();
			}
			commercelat = addresses.get(0).getLatitude();
			commercelon = addresses.get(0).getLongitude();
			String address = addresses.get(0).getAddressLine(0);
			String city = addresses.get(0).getAddressLine(1);
			String country = addresses.get(0).getAddressLine(2);
			String straddress = address + " \n" + city + " " + country;
			String strLocation = addresses.get(0).toString();
			tvLocation.setText(address + "\n" + city + ", " + country);
			tvPlacename.setText(placename);
			/*
			 * // GITANADA if (location.contains("[") == true) {
			 * 
			 * StringTokenizer tokens = new StringTokenizer(location, ":\"");
			 * String first = tokens.nextToken();// this will contain "Fruit"
			 * String second = tokens.nextToken();// this will contain //
			 * " they taste good" String third = tokens.nextToken(); String
			 * fourth = tokens.nextToken(); String fifth = tokens.nextToken();
			 * String sixth = tokens.nextToken();
			 * 
			 * System.out.println(second); System.out.println(fourth);
			 * System.out.println(sixth);
			 * 
			 * // location = commerce.getString("address");
			 * System.out.println("LOCATION: " + location);
			 * 
			 * tvPlacename.setText(placename); tvLocation.setText(second + "\n"
			 * + fourth + ", " + sixth); }
			 * 
			 * else { System.out.println("LOCATION GOOGLE: " + location);
			 * StringTokenizer tokens = new StringTokenizer(location, ";:");
			 * String first = tokens.nextToken();// this will contain "Fruit"
			 * String second = tokens.nextToken();// this will contain
			 * commercestreet = second; // " they taste good" String third =
			 * tokens.nextToken(); String fourth = tokens.nextToken();
			 * commercelat = Double.valueOf(fourth);
			 * 
			 * String fifth = tokens.nextToken(); String sixth =
			 * tokens.nextToken(); commercelon = Double.valueOf(sixth);
			 * 
			 * System.out.println(second); System.out.println(fourth);
			 * System.out.println(sixth);
			 * 
			 * // location = commerce.getString("address");
			 * System.out.println("LOCATION: " + location);
			 * 
			 * tvPlacename.setText(placename); tvLocation.setText(second);
			 * 
			 * }
			 */
			// btnLocationCommerce.setVisibility(View.GONE);
			layoutInsertOfferSelectCommerce.setVisibility(View.GONE);
			layoutInsertOfferCommerce.setVisibility(0);
			/*
			 * viewCommerce.setVisibility(0); tvChangeCommerce.setVisibility(0);
			 * tvPlacename.setVisibility(0); tvPlacenameLabel.setVisibility(0);
			 * tvLocation.setVisibility(0); tvLocationLabel.setVisibility(0);
			 */
			if (tvPlacename.getText().length() != 0) {
				btnInsertOffer.setBackgroundColor(getResources().getColor(
						R.color.mainColor));
				btnLocationCommerce.setBackgroundColor(Color.GRAY);
			}
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// METODO PARA OBTENER LA FECHA ACTUAL
	protected Date actualDate() {
		Calendar calendar = new GregorianCalendar();
		final Date createdate = calendar.getTime();
		return createdate;
	}

	private void makeTextViewHyperlink(TextView tvReport) {

		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append(tvReport.getText());
		ssb.setSpan(new URLSpan("#"), 0, ssb.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tvReport.setText(ssb, TextView.BufferType.SPANNABLE);

	}

	/*
	 * if (android.os.Build.VERSION.SDK_INT <
	 * android.os.Build.VERSION_CODES.HONEYCOMB) { prefers = PreferenceManager
	 * .getDefaultSharedPreferences(getApplicationContext());
	 * 
	 * } else { prefers = getSharedPreferences("MisPreferencias",
	 * Context.MODE_PRIVATE);
	 * 
	 * }
	 */
	private String setCategory(String value) {
		if (compraslist.contains(value)) {
			return "compras";
		} else if (ociolist.contains(value)) {
			return "ocio";

		} else if (otroslist.contains(value)) {
			return "otros";

		} else if (servicioslist.contains(value)) {
			return category;
		} else {
			return "";
		}
	}
}
