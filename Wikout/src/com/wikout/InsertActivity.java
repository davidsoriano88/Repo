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

import com.wikout.R;

import utils.CustomOnItemSelectedListener;
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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class InsertActivity extends Activity {
	Double latitude, longitude;
	int enter;
	TextView placelink;
	String position;
	ImageView contactPhoto;
	EditText offerEt;
	EditText placeName;
	Button accept;
	Button dateLimit;
	Spinner spinner1;

	// Otras variables
	String photoPath;
	String idoferta;
	String idphoto = "";

	// Variables para controlar la fecha
	private int year;
	private int month;
	private int day;
	private Date deadline;
	// variables para control de fotografias

	String photoName;
	String url;
	File photo;
	public int existPhoto = 0;
	// constantes utilizadas para lanzar intents
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int LOAD_IMAGE = 3;

	// Location de prueba
	public Location locationbm;

	// Constante para el picker
	static final int DATE_DIALOG_ID = 999;
	final Context context = this;
	public Intent map;
	Util util= new Util();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_insert);
		initUI();
		
	}
	 public void initUI(){
		map = new Intent(getApplicationContext(), Map.class);
		contactPhoto = (ImageView) findViewById(R.id.ivFoto);
		offerEt = (EditText) findViewById(R.id.ofertaTv);
		placeName = (EditText) findViewById(R.id.nombreTv);
		accept = (Button) findViewById(R.id.btnOk);
		projectData();
		addListenerOnButton();
		addListenerOnSpinnerItemSelection();

		setCurrentDateOnView();

		placelink = (TextView) findViewById(R.id.ubicacionTv);
		Bundle bundle = getIntent().getExtras();
		enter = bundle.getInt("enter");
		if (enter == 1) {

			latitude = bundle.getDouble("latiMain");
			longitude = bundle.getDouble("longiMain");
			util.log( latitude + "," + longitude);
			position = "(posClick) " + String.valueOf(latitude) + ","
					+ String.valueOf(longitude);
			placelink.setText(position);

		} else {
			util.log("unable to show localization");
		}

		// Lo ponemos a escuchar para cuando sea pulsado
		placelink.setTextColor(Color.BLUE);
		placelink.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				dialog();
			}

		});

		accept.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (latitude == null) {
					dialog();
				} else {
					if(offerEt.getText().length()==0 | placeName.getText().length()==0){
						util.log("aceptar1");
					dialogText();
					

					}else{
					if (existPhoto == 1) {
						insertPhoto();
						finish();
					} else {
						imageClicked(v);
					}
					
				}
				}

			}

		});
 
	 }
	private void projectData() {
		Backbeam.setProject("pruebaapp");
		Backbeam.setEnvironment("dev");
		Backbeam.setContext(getApplicationContext());

		// Create the API keys in the control panel of your project
		Backbeam.setSharedKey("dev_56862947719ac4db38049d3afa2b68a78fb3b9a9");
		Backbeam.setSecretKey("dev_f69ccffe433e069c591151c93281ba6b14455a535998d7b29ca789add023ad5e4bab596eb88815cb");

	}

	public void dialog() {
		AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
		dialog1.setTitle("Ubicacion");
		dialog1.setMessage("Elija la direccion:");
		dialog1.setCancelable(false);
		dialog1.setPositiveButton("Ubicacion actual",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						Bundle bundle = getIntent().getExtras();
						latitude = bundle.getDouble("latpos");
						longitude = bundle.getDouble("longpos");
						util.log(latitude + "," + longitude);
						position = "(Actual) " + String.valueOf(latitude) + ","
								+ String.valueOf(longitude);
						placelink.setText(position);

					}

				});
		dialog1.setNegativeButton("Indicar en el mapa",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						Bundle bundle = getIntent().getExtras();
						latitude = bundle.getDouble("latiMain");
						longitude = bundle.getDouble("longiMain");
						finish();
					}
				});
		dialog1.show();
	}
	public void dialogText() {
		AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
		dialog1.setTitle("Información incompleta");
		dialog1.setMessage("Rellene los campos Incompletos, por favor.");
		dialog1.setCancelable(false);
		dialog1.setNeutralButton("Aceptar",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						

					}

				});
		
		dialog1.show();
	}
	public void addListenerOnSpinnerItemSelection() {

		spinner1 = (Spinner) findViewById(R.id.spnCategorias);
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());

	}

	private void setCurrentDateOnView() {

		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

		// set current date into textview
		dateLimit.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(day).append("-").append(month + 1).append("-")
				.append(year).append(" ")); 
		deadline = c.getTime();

	}

	private void addListenerOnButton() {

		dateLimit = (Button) findViewById(R.id.btnDateLimit);
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
		protected void insertOffer(Date createdate, final BackbeamObject commerce) {
			//Creo el objeto "offer"
			final BackbeamObject offer = new BackbeamObject("offer");
			//inserto los valores de "offer"
			offer.setString("description", offerEt.getText().toString());
			offer.setDay("deadline", deadline);
			offer.setString("udid", getId());
			offer.setString("offerstatus", "ok");
			offer.setDate("offercreationdate", createdate);
			offer.setObject("commerce", commerce);
			//TODAVIA NO CONTEMPLO LIKE NI REPORT YA QUE SE ACABA DE CREAR
			// offer.addObject("like");
			// offer.addObject("report");
			
			offer.setNumber("numlike", 0);
			offer.save(new ObjectCallback() {
				@Override
				public void success(BackbeamObject offer) {
					System.out.println("foto subida con éxito!! " + offer.getId());
				
				}
			});
		}
		// METODO PARA SUBIR FOTO
		protected void insertPhoto() {
			//Extraigo la fecha actual
			Calendar calendar = new GregorianCalendar();
			final Date createdate = calendar.getTime();
			//Creo el objeto commerce
			final BackbeamObject objectphoto = new BackbeamObject("file");
			//Hay que pasarle el objeto de tipo file "foto"
			objectphoto.uploadFile(new FileUpload(photo, "image/jpg"),
					new ObjectCallback() {
						@Override
						public void success(BackbeamObject photo) {
							System.out.println("success!! " + photo.getId());
							photo.setString("idphoto", photo.getId());
							photo.setDate("uploaddate", createdate);
							photo.save(new ObjectCallback() {
								@Override
								public void success(BackbeamObject objetofoto) {
									System.out.println("foto subida con éxito!! "
											+ objetofoto.getId());
									insertCommerce(objetofoto);
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
		
		// INSERTAR NUEVO "COMMERCE"
		protected void insertCommerce(BackbeamObject objectphoto) {
			locationbm = new Location(latitude, longitude);
			//Extraigo la fecha actual
			Calendar calendar = new GregorianCalendar();
			final Date createdate = calendar.getTime();
			//Creo el objeto commerce
			final BackbeamObject commerce = new BackbeamObject("commerce");
			//Relleno los campos del objeto
			commerce.setString("placename", placeName.getText().toString());
			commerce.setLocation("placelocation", locationbm);
			commerce.setString("category",(String) spinner1.getSelectedItem() );
			commerce.setDate("commercecreationdate", createdate);
			commerce.setString("udid", getId());
			commerce.setObject("file", objectphoto);
			//Guardo el objeto
			commerce.save(new ObjectCallback() {
				@Override
				public void success(BackbeamObject object) {
					
					//Llamo al metodo insertOffer para enlazarlo con la oferta
					insertOffer(createdate, object);

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
		if (resultCode == RESULT_OK) {

			Bundle photoBundle = new Bundle();
			util.log("bundle created");
			photoBundle = data.getExtras();

			photoPath = photoBundle.getString("path");
			photoName = photoBundle.getString("photoName");
			url = photoBundle.getString("url");
			photo = (File) photoBundle.get("photo");

			util.log(photoBundle.getString("photoName"));

			Bitmap cphoto = BitmapFactory.decodeFile(photoPath);
			contactPhoto.setImageBitmap(cphoto);
			existPhoto = 1;

		}
	}

}
