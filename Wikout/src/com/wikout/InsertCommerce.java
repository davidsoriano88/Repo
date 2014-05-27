package com.wikout;
import io.backbeam.BackbeamException;
import io.backbeam.BackbeamObject;
import io.backbeam.FileUpload;
import io.backbeam.Location;
import io.backbeam.ObjectCallback;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class InsertCommerce extends Activity {
	
	Double latitude, longitude;
	String position;
	ImageView ivPhoto;
	EditText etPlacename;
	Button btnOk;
	Spinner spnCategory;

	// Otras variables
	String photoPath, idoferta, idphoto = "";

	// Variables para controlar la fecha

	
	// variables para control de fotografias
	String photoName, url;
	File photo;
	public int existPhoto = 0;
	
	// constantes utilizadas para lanzar intents
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int LOAD_IMAGE = 3;

	// Location de prueba
	public Location locationbm;

	// Constante para el picker
	
	final Context context = this;
	
	Util util= new Util();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.insert_commerce);
		util.projectData(context);
		initUI();
		
	}
	 private void initUI(){
		
		ivPhoto = (ImageView) findViewById(R.id.ivInsertPhoto1);
		etPlacename = (EditText) findViewById(R.id.etInsertPlacename1);
		btnOk = (Button) findViewById(R.id.btnInsertOk1);
		addListenerOnSpinnerItemSelection();

		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
					if( etPlacename.getText().length()==0){
						util.log("aceptar1");
					dialogIncompleteFields();
					}else if(existPhoto == 1){
					
						insertPhoto();
						finish();
					} else {
						imageClicked(v);
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
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
						String myLatitude = prefs.getString("latpos", "no id");
						latitude = Double.parseDouble(myLatitude);
						String myLongitude = prefs.getString("longpos", "no id");
						longitude = Double.parseDouble(myLongitude);
						

					}

				});
		dialogLocation.setNegativeButton("Indicar en el mapa",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						Bundle bundle = getIntent().getExtras();
						latitude = bundle.getDouble("latiMain");
						longitude = bundle.getDouble("longiMain");
						finish();
					}
				});
		dialogLocation.show();
	}
	public void dialogIncompleteFields() {
		AlertDialog.Builder dialogIncomplete = new AlertDialog.Builder(this);
		dialogIncomplete.setTitle("Información incompleta");
		dialogIncomplete.setMessage("Rellene los campos Incompletos, por favor.");
		dialogIncomplete.setCancelable(false);
		dialogIncomplete.setNeutralButton("Aceptar",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						

					}

				});
		
		dialogIncomplete.show();
	}
	public void addListenerOnSpinnerItemSelection() {

		spnCategory = (Spinner) findViewById(R.id.spnInsertCategory1);
		spnCategory.setOnItemSelectedListener(new CustomOnItemSelectedListener());

	}

	
	// METODO PARA SUBIR FOTO
		private void insertPhoto() {
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
		private void insertCommerce(BackbeamObject objectphoto) {
			locationbm = new Location(latitude, longitude);
			//Extraigo la fecha actual
			Calendar calendar = new GregorianCalendar();
			final Date createdate = calendar.getTime();
			//Creo el objeto commerce
			final BackbeamObject commerce = new BackbeamObject("commerce");
			//Relleno los campos del objeto
			commerce.setString("placename", etPlacename.getText().toString());
			commerce.setLocation("placelocation", locationbm);
			commerce.setString("category",(String) spnCategory.getSelectedItem() );
			commerce.setDate("commercecreationdate", createdate);
			commerce.setNumber("numbubble", 0);
			commerce.setString("udid", getId());
			commerce.setObject("file", objectphoto);
			//Guardo el objeto
			commerce.save(new ObjectCallback() {
				@Override
				public void success(BackbeamObject object) {
					
					//Llamo al metodo insertOffer para enlazarlo con la oferta
					//insertOffer(createdate, object);****************************REVISAR

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
			ivPhoto.setImageBitmap(cphoto);
			existPhoto = 1;

		}
	}

}

