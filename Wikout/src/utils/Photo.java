package utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wikout.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class Photo extends Activity {

	// Variables utilizadas para lanzar intents
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int LOAD_IMAGE = 3;
	static final int CROP_IMAGE = 2;

	// Variables la manipulacion de de la foto.
	String photoPath = "EMPTY";
	File photoFile = null;
	File image = null;
	Uri photoUri = null;
	OutputStream fOutputSteam = null;
	
	//
	String photoName ="";
	String url = "";
	Util util = new Util();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtengo el intent que ha lanzado esta activity
		Intent starterIntent = getIntent();
		Bundle dataBundle = starterIntent.getExtras();

		// Segun lo que se haya pedido lanzo la camara o la galeria.
		String actionRequested = dataBundle.getString("ACTION_REQUESTED");

		if (actionRequested.matches("CAMERA")) {
			dispatchTakePictureIntent();
		} else {
			dispatchSelectFromGallery();
		}
	}

	private void dispatchTakePictureIntent() {
		Log.i("PHOTO:", "i'm at dispatchTakePicture");

		// Creo el intent
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Verifico que haya una camara disponible
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

			// Creamos el fichero donde se vaya a guardar la foto
			try {
				photoFile = createImageFile();
				photoUri = Uri.fromFile(photoFile);
				// guardar en el fichero creado
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				Log.i("PHOTO:", "i want a foto in: "
						+ photoPath);



				// lanzamos la actividad for result
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

			} catch (IOException ex) {
				util.showToast(this,"error when creating file");
						
				finish();
			}
		}
	}

	private void dispatchSelectFromGallery() {

		// Creo el intent que me abre la actividad de la galeria para elegir.
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		// Establezco los parametros del intent
		photoPickerIntent.setType("image/*");
		photoPickerIntent.putExtra("crop", "true");

		// Creamos el fichero para guardar la foto.
		try {
			photoFile = createImageFile();
			photoUri = Uri.fromFile(photoFile);

			// Le digo donde me tiene que guardar el resultado.
			photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

			// Pongo los parametros del crop.
			photoPickerIntent.setType("image/*");
			photoPickerIntent.putExtra("crop", "true");
			photoPickerIntent.putExtra("aspectX", 2);
			photoPickerIntent.putExtra("aspectY", 1);
			photoPickerIntent.putExtra("outputX", 400);
			photoPickerIntent.putExtra("outputY", 200);

			// Lanzo el activity de la galeria.
			startActivityForResult(photoPickerIntent, LOAD_IMAGE);

		} catch (IOException e) {
			util.showToast(this,"error when creating file");
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Si acabo de tomar una foto y todo ha ido bien
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

			try {
				cropCapturedImage(photoUri);
			} catch (ActivityNotFoundException anf) {

				// Si el OS no soporta el crop que pase la foto tal cual.
				Intent pictureIntent = new Intent();
				pictureIntent.putExtra("path", photoPath);
				pictureIntent.putExtra("photoName", photoName);
				pictureIntent.putExtra("url", url);
				pictureIntent.putExtra("photo", image);

				setResult(10000, pictureIntent);
				finish();
			}
		}

		if ((requestCode == CROP_IMAGE || requestCode == LOAD_IMAGE)
				&& resultCode == RESULT_OK) {

			// Si vengo de CROP_IMAGE o de LOAD_IMAGE y todo ha ido bien que
			// pase el path.
			Intent pictureIntent = new Intent();
			pictureIntent.putExtra("path", photoPath);
			pictureIntent.putExtra("photoName", photoName);
			pictureIntent.putExtra("url", url);
			pictureIntent.putExtra("photo", image);

			setResult(10000, pictureIntent);
			finish();
		}
	}

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {

		// Creamos un nombre para la foto
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "Photo_" + timeStamp + "_";


		// Obtengo el folder publico de fotos, le añado un folder mis Contactos
		File storageDir = new File(Environment
				.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES).toString()
				+ "/Contactos");

		// Si no existe el directorio que me lo cree
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}

		// Creamos un fichero con el nombre generado en la carpeta Contactos.
		image = File.createTempFile(imageFileName, ".jpg", storageDir);

		// photoPath es el path entero de la foto.
		photoPath = image.getAbsolutePath();
		url = image.getAbsolutePath();

		// Devuelvo el fichero con el nombre creado.
		photoName = imageFileName + ".jpg";
		return image;
	}

	public void cropCapturedImage(Uri picUri) {
		// hacemos un intent para la aplicacion de crop
		Intent cropIntent = new Intent("com.android.camera.action.CROP");

		// indicamos que se trata de una foto y uri
		cropIntent.setDataAndType(picUri, "image/*");
		// Pongo los parametros.
		cropIntent.putExtra("crop", "true");
		// Que sea cuadrado, es decir que haya un relacion 1 a 1 entre los lados
		cropIntent.putExtra("aspectX", 2);
		cropIntent.putExtra("aspectY", 1);
		// Resolucion de salida.
		cropIntent.putExtra("outputX", 400);
		cropIntent.putExtra("outputY", 200);

		// Que lo guarde en ese uri
		cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
		// Lanzo el activity
		startActivityForResult(cropIntent, CROP_IMAGE);
	}
}
