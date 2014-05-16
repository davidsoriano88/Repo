package com.wikout;

import io.backbeam.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import utils.Util;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Reference extends Activity {
	public TextView tvPlacename, tvDescription, tvLocation;
	public Button btnLike, btnFlag;
	public ImageView ivPhoto;
	Context context;
	Util util = new Util();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
		util.projectData(context);
		initUI();
	}

	public void initUI() {

		tvPlacename = (TextView) findViewById(R.id.tvReferencePlacename);
		tvDescription = (TextView) findViewById(R.id.tvReferenceDescription);
		btnLike = (Button) findViewById(R.id.btnReferenceLike);
		btnFlag = (Button) findViewById(R.id.btnReferenceFlag);
		tvLocation = (TextView) findViewById(R.id.tvReferenceLocation);
		ivPhoto = (ImageView) findViewById(R.id.ivFoto);
		tvPlacename.setTextColor(Color.WHITE);
		btnLike.setTextColor(Color.WHITE);

		loadData();
	}

	public void loadData() {
		Bundle bundle = getIntent().getExtras();
		tvPlacename.setText(bundle.getString("placename"));
		queryOffer(bundle.getString("id"));
		util.log(bundle.getString("id"));
	}

	private void queryOffer(String idreference) {
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idreference);

		Query query = new Query("commerce");
		query.setQuery("where this in ? join last 10 offer", collection);
		query.fetch(100, 0, new FetchCallback() {

			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
				BackbeamObject place = objects.get(0);
				JoinResult join = place.getJoinResult("offer");
				List<BackbeamObject> offers = join.getResults();

				// if there are references without offers
				for (BackbeamObject offer : offers) {
					System.out.println("description "
							+ offer.getString("description"));
					System.out.println("numlike " + offer.getNumber("numlike"));

					SimpleDateFormat format1 = new SimpleDateFormat(
							"dd-MM-yyyy");
					String formatted = format1.format(offer.getDay("deadline")
							.getTime());
					System.out.println("deadline " + formatted);
				}

			}
		});

	}

//METODO obsoleto, CAMBIARLO! 
	private void insertLike(final String idoffer) {
		Calendar calendar = new GregorianCalendar();
		final Date createdate = calendar.getTime();
		// create objects
		final BackbeamObject like = new BackbeamObject("like");
		final BackbeamObject offer = new BackbeamObject("offer", idoffer);
		like.setString("udid", getId().toString());
		like.setDate("likedate", createdate);
		like.setObject("offer", offer);

		like.save(new ObjectCallback() {
			@Override
			public void success(BackbeamObject object) {
				System.out.println("like guardado");
			}
		});
		// counts likes per offer
		CollectionConstraint collection = new CollectionConstraint();
		collection.addIdentifier(idoffer);
		Query query = new Query("offer");
		query.setQuery("where this in ? join last 10 like", collection);
		query.fetch(100, 0, new FetchCallback() {

			@Override
			public void success(List<BackbeamObject> objects, int totalCount,
					boolean fromCache) {
				BackbeamObject place = objects.get(0);
				JoinResult join = place.getJoinResult("like");
				// "count" gives the amount of likes of the offer
				int count = join.getCount();
				// set the number of likes:
				offer.setNumber("numlike", count);
				offer.save(new ObjectCallback() {
					@Override
					public void success(BackbeamObject object) {
						System.out.println(offer.getString("description"));
						System.out.println(offer.getNumber("numlike"));
					}
				});
			}
		});
	}

	public String getId() {
		String id = android.provider.Settings.System.getString(
				super.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return id;
	}
}
