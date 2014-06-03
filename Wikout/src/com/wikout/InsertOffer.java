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

import utils.Photo;
import utils.Util;
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
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class InsertOffer extends ActionBarActivity {
	
	Double latitude, longitude;
	int enter;
	public ActionBarActivity fio;
	
	String position;
	ImageView ivPhoto;
	EditText etDescription,etPlacename;
	Button btnOk,dateLimit,btnLocation;
	Spinner spnCategory;
	TextView tvCommerce;
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
	Util util= new Util();
	
	//Para el bundle de ListCommerce e InsertCommerce
	String idcommerce,placename, idObjectPhoto=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.insert_offer);
		fio=this;
		util.projectData(context);
		bundle = getIntent().getExtras();
		initUI();
		if(bundle!=null){
			idcommerce=bundle.getString("idcommerce");
			placename=bundle.getString("placename");
			etPlacename.setVisibility(0);
			tvCommerce.setVisibility(0);
			etPlacename.setText(placename);
			btnLocation.setVisibility(4);
		}
	}
	 private void initUI(){
		map = new Intent(getApplicationContext(), Map.class);
		ivPhoto = (ImageView) findViewById(R.id.ivInsertPhoto);
		etDescription = (EditText) findViewById(R.id.etInsertDescription);
		etPlacename = (EditText) findViewById(R.id.etInsertPlacename);
		tvCommerce=(TextView) findViewById(R.id.tvPlaceNameInfo);
		btnOk = (Button) findViewById(R.id.btnInsertOffer);
		addListenerOnButton();
		getSupportActionBar().setTitle("Nueva Oferta");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setCurrentDateOnView();

		btnLocation = (Button) findViewById(R.id.btnCommerce);
		
		
		
		// Lo ponemos a escuchar para cuando sea pulsado
		
		btnLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// request your webservice here. Possible use of AsyncTask and
				// ProgressDialog
				dialogGetLocation();
			}

		});

		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
					if(etDescription.getText().length()==0){
						util.log("aceptar1");
					dialogIncompleteFields();
					

					} else if(etPlacename.getText().length()==0) {
						AlertDialog.Builder dialogIncomplete = new AlertDialog.Builder(context);
						dialogIncomplete.setTitle("Información incompleta");
						dialogIncomplete.setMessage("Seleccione el comercio de la oferta, por favor.");
						dialogIncomplete.setCancelable(false);
						dialogIncomplete.setNeutralButton("Aceptar",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogo1, int id) {
										

									}

								});
						
						dialogIncomplete.show();
					}
					else if(photo!=null){
						setSupportProgressBarIndeterminateVisibility(true);
							insertOfferPhoto(actualDate());
							}else{
								setSupportProgressBarIndeterminateVisibility(true);
								util.log("no hay foto");
								insertNewOffer(actualDate(), idcommerce, idObjectPhoto);
								finish();//*********************************************************
							}
						
						//imageClicked(v);
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
						enter=0;
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
						
						String myLatitude = prefs.getString("latpos", "no id");
						latitude = Double.parseDouble(myLatitude);
						String myLongitude = prefs.getString("longpos", "no id");
						longitude = Double.parseDouble(myLongitude);
						//btnLocation.setText(latitude +","+longitude);
						util.showToast(context,latitude+","+longitude);
						Intent listCommerce = new Intent(context, CommerceList.class);
						listCommerce.putExtra("pointlat", latitude);
						listCommerce.putExtra("pointlon", longitude);
						startActivityForResult(listCommerce,30);
					}

				});
		dialogLocation.setNegativeButton("Indicar en el mapa",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {
						/*Bundle bundle = getIntent().getExtras();
						latitude = bundle.getDouble("latiMain");
						longitude = bundle.getDouble("longiMain");*/
						enter=1;
						Intent listCommerce = new Intent(context, CommerceList.class);
						listCommerce.putExtra("enter", enter);
						startActivityForResult(listCommerce,30);
						
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
	

	private void setCurrentDateOnView() {

		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

		// set current date into textview
		dateLimit.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(day).append("-").append(month + 4).append("-")
				.append(year).append(" ")); 
		deadline = c.getTime();

	}

	private void addListenerOnButton() {

		dateLimit = (Button) findViewById(R.id.btnInsertDeadline);
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
	protected void insertNewOffer(final Date createdate, final String idcommerce, final String idfile) {
		//Creo el objeto "offer"
		final BackbeamObject offer = new BackbeamObject("offer");
				//Sacar idcommerce del bundle al volver a esta activity
		final BackbeamObject commerce = new BackbeamObject("commerce", idcommerce);
		
		if(idfile!=null){
		final BackbeamObject file = new BackbeamObject("file", idfile);
		offer.setObject("file", file);
		}
		//inserto los valores de "offer"
		//util.log(idfile);
		offer.setString("description", etDescription.getText().toString());
		offer.setDay("deadline", deadline);
		offer.setString("udid", getId());
		offer.setString("offerstatus", "ok");
		offer.setDate("offercreationdate", createdate);
		
		offer.setObject("commerce", commerce);
		offer.setNumber("numlike", 0);
		//TODAVIA NO CONTEMPLO LIKE NI REPORT YA QUE SE ACABA DE CREAR
		// offer.addObject("like");
		// offer.addObject("report");
		
		offer.save(new ObjectCallback() {//**************************************************************************
			@Override
			public void success(BackbeamObject offer) {
				System.out.println("foto subida con éxito!! " + offer.getId());
				finish();
				setSupportProgressBarIndeterminateVisibility(false);
			}
		});
	}
	
	// METODO PARA SUBIR FOTO de offer
	protected void insertOfferPhoto(final Date createdate) {
		final BackbeamObject objectPhoto = new BackbeamObject("file");
		//Hay que pasarle el objeto de tipo file "foto"
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
								idObjectPhoto=objectPhoto.getId();
								
								insertNewOffer(createdate, idcommerce,idObjectPhoto);
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

		}if(requestCode==30 && resultCode==RESULT_OK){
			Bundle commerce = new Bundle();
			util.log("bundle created");
			commerce = data.getExtras();
			idcommerce = commerce.getString("idcommerce");
			placename = commerce.getString("placename");
			etPlacename.setText(placename);
			tvCommerce.setVisibility(0);
			etPlacename.setVisibility(0);
			if(etPlacename.getText().length()!=0){
				btnOk.setBackgroundColor(getResources().getColor(R.color.mainColor));
				btnLocation.setBackgroundColor(Color.GRAY);
			}
		}
		
		

			
		
	}
	private void commerceData(String idcommerce) {
		Backbeam.read("commerce", idcommerce, new ObjectCallback() {
			@Override
			public void success(BackbeamObject offer) {
				offer.getString("placename");
				offer.getLocation("placelocation");
				offer.getString("category");
				//Ocultar botón de comercio
				//btnLocation.is;
			
			}
				
			});
		
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
	protected Date actualDate(){
		Calendar calendar = new GregorianCalendar();
		final Date createdate = calendar.getTime();
		return createdate;
	}


}
