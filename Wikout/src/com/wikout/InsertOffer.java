package com.wikout;

import io.backbeam.Backbeam;
import io.backbeam.BackbeamException;
import io.backbeam.BackbeamObject;
import io.backbeam.FileUpload;
import io.backbeam.Location;
import io.backbeam.ObjectCallback;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

	Double latitude, longitude;
	int enter;
	public ActionBarActivity fio;

	String position;
	ImageView ivPhoto;
	EditText etDescription;
	Button btnInsertCommerce, dateLimit, btnLocationCommerce;
	Spinner spnCategory;
	TextView tvPlacenameLabel, tvPlacename, tvLocation, tvLocationLabel,tvChangeCommerce;
	View viewCommerce;
	// Otras variables
	String photoPath, idoferta, idphoto = "";

	// Variables para controlar la fecha
	private int year, month, day;
	private Date deadline;

	// variables para control de fotografias
	String photoName, url;
	File photo;
	public int existPhoto = 0;

	// constantes utilizadas para lanzar intents
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int LOAD_IMAGE = 3;

	// Location de prueba
	public Location locationbm;
	Bundle bundle;
	// Constante para el picker
	static final int DATE_DIALOG_ID = 999;
	final Context context = this;
	public Intent map;
	Util util = new Util();

	// Para el bundle de ListCommerce e InsertCommerce
	String idcommerce, location, placename, idObjectPhoto = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.insert_offer);
		FontUtils.setRobotoFont(context, ((Activity) context).getWindow()
				.getDecorView());
		fio = this;
		util.projectData(context);
		bundle = getIntent().getExtras();
		initUI();
		if (bundle != null) {
			
			idcommerce = bundle.getString("idcommerce");
			placename = bundle.getString("placename");
			location = bundle.getString("location");

			btnLocationCommerce.setVisibility(View.GONE);

			viewCommerce.setVisibility(0);
			tvChangeCommerce.setVisibility(0);
			tvPlacename.setVisibility(0);
			tvPlacenameLabel.setVisibility(0);
			tvLocation.setVisibility(0);
			tvLocationLabel.setVisibility(0);

			tvPlacename.setText(placename);
			tvLocation.setText(location);

			btnInsertCommerce.setBackgroundColor(getResources().getColor(
					R.color.mainColor));
		} else {
			btnLocationCommerce.setVisibility(0);

			viewCommerce.setVisibility(View.GONE);
			tvChangeCommerce.setVisibility(View.GONE);
			tvPlacename.setVisibility(View.GONE);
			tvPlacenameLabel.setVisibility(View.GONE);
			tvLocation.setVisibility(View.GONE);
			tvLocationLabel.setVisibility(View.GONE);
		}
	}

	private void initUI() {
		map = new Intent(getApplicationContext(), Map.class);
		ivPhoto = (ImageView) findViewById(R.id.ivInsertOfferPhoto);
		viewCommerce = (View) findViewById(R.id.viewCommerce);
		tvChangeCommerce = (TextView) findViewById(R.id.tvInsertOfferChangeCommerce);
		etDescription = (EditText) findViewById(R.id.etInsertOfferDescription);
		tvPlacename = (TextView) findViewById(R.id.tvInsertOfferPlacename);
		tvPlacenameLabel = (TextView) findViewById(R.id.tvInsertOfferPlacenamelabel);
		tvLocation = (TextView) findViewById(R.id.tvInsertOfferCommerceLocation);
		tvLocationLabel = (TextView) findViewById(R.id.tvInsertOfferCommerceLocationlabel);
		btnInsertCommerce = (Button) findViewById(R.id.btnInsertOfferOk);
		addListenerOnButton();
		getSupportActionBar().setTitle("Nueva Oferta");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setCurrentDateOnView();
		makeTextViewHyperlink(tvChangeCommerce);

		tvChangeCommerce.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				if (isNetworkAvailable() == true) {
					dialogGetLocation();
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexión a internet");
				}
			}
		});

		btnLocationCommerce = (Button) findViewById(R.id.btnInsertOfferCommerce);

		if (etDescription.isFocused()) {
			btnInsertCommerce.requestFocus();
		}

		// Lo ponemos a escuchar para cuando sea pulsado

		btnLocationCommerce.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				if (isNetworkAvailable() == true) {
					dialogGetLocation();
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexión a internet");
				}
			}

		});

		btnInsertCommerce.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isNetworkAvailable() == true) {
					if (etDescription.getText().length() == 0) {
						util.log("aceptar1");
						dialogIncompleteFields();

					} else if (tvPlacename.getText().length() == 0) {
						AlertDialog.Builder dialogIncomplete = new AlertDialog.Builder(
								context);
						dialogIncomplete.setTitle("Información incompleta");
						dialogIncomplete
								.setMessage("Seleccione el comercio de la oferta, por favor.");
						dialogIncomplete.setCancelable(false);
						dialogIncomplete.setNeutralButton("Aceptar",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialogo1, int id) {

									}

								});

						dialogIncomplete.show();
					} else if (photo != null) {
						btnInsertCommerce.setEnabled(false);
						setSupportProgressBarIndeterminateVisibility(true);
						btnInsertCommerce.setEnabled(false);
						insertOfferPhoto(actualDate());
					} else {
						setSupportProgressBarIndeterminateVisibility(true);
						util.log("no hay foto");
						insertNewOffer(actualDate(), idcommerce, idObjectPhoto);

					}

					// imageClicked(v);
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexión a internet");
				}
			}
		});

	}

	public void dialogGetLocation() {
		AlertDialog.Builder dialogLocation = new AlertDialog.Builder(this);
		dialogLocation.setTitle("Ubicacion");
		dialogLocation.setMessage("Elija la direccion:");
		dialogLocation.setCancelable(true);
		dialogLocation.setPositiveButton("Ubicacion actual",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						enter = 0;
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(context);

						String myLatitude = prefs.getString("latpos", "no id");
						latitude = Double.parseDouble(myLatitude);
						String myLongitude = prefs
								.getString("longpos", "no id");
						longitude = Double.parseDouble(myLongitude);
						// btnLocation.setText(latitude +","+longitude);
						// util.showToast(context,latitude+","+longitude);
						Intent listCommerce = new Intent(context,
								CommerceList.class);
						listCommerce.putExtra("pointlat", latitude);
						listCommerce.putExtra("pointlon", longitude);
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
				.setMessage("Rellene los campos Incompletos, por favor.");
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

	// METODO PARA INSERTAR OFERTA
	protected void insertNewOffer(final Date createdate,
			final String idcommerce, final String idfile) {
		// Creo el objeto "offer"
		final BackbeamObject offer = new BackbeamObject("offer");
		// Sacar idcommerce del bundle al volver a esta activity
		final BackbeamObject commerce = new BackbeamObject("commerce",
				idcommerce);

		if (idfile != null) {
			final BackbeamObject file = new BackbeamObject("file", idfile);
			offer.setObject("file", file);
		}
		// inserto los valores de "offer"
		// util.log(idfile);
		offer.setString("description", etDescription.getText().toString());
		offer.setDay("deadline", deadline);
		offer.setString("udid", getId());
		offer.setString("offerstatus", "ok");
		offer.setDate("offercreationdate", createdate);

		offer.setObject("commerce", commerce);
		offer.setNumber("numlike", 0);
		// TODAVIA NO CONTEMPLO LIKE NI REPORT YA QUE SE ACABA DE CREAR
		// offer.addObject("like");
		// offer.addObject("report");

		offer.save(new ObjectCallback() {// **************************************************************************
			@Override
			public void success(BackbeamObject offer) {
				Backbeam.read("commerce", idcommerce, new ObjectCallback() {
					@Override
					public void success(BackbeamObject commerce) {
						final int contador = commerce.getNumber("actualoffers")
								.intValue();
						System.out.println(commerce.getString("placename")
								+ "\nNumero de ofertas: " + contador);
						commerce.setNumber("actualoffers", contador + 1);
						commerce.save(new ObjectCallback() {
							@Override
							public void success(BackbeamObject object) {
								System.out.println("updated! :) "
										+ object.getId());
								System.out.println(object
										.getString("placename")
										+ "\nNumero de ofertas: "
										+ object.getNumber("actualoffers")
												.intValue());
								util.showToast(context, "Oferta insertada");
								finish();
								btnInsertCommerce.setEnabled(true);
								setSupportProgressBarIndeterminateVisibility(false);
							}
						});
					}
				});

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

	private String getId() {
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
			location = commerce.getString("location");
			tvPlacename.setText(placename);
			tvLocation.setText(location);
			btnLocationCommerce.setVisibility(View.GONE);

			viewCommerce.setVisibility(0);
			tvChangeCommerce.setVisibility(0);
			tvPlacename.setVisibility(0);
			tvPlacenameLabel.setVisibility(0);
			tvLocation.setVisibility(0);
			tvLocationLabel.setVisibility(0);
			if (tvPlacename.getText().length() != 0) {
				btnInsertCommerce.setBackgroundColor(getResources().getColor(
						R.color.mainColor));
				btnLocationCommerce.setBackgroundColor(Color.GRAY);
			}
		}

	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

}
