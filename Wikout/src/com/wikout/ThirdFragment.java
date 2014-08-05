package com.wikout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class ThirdFragment extends Fragment  {

	//Context context = getParentFragment().getActivity();
	Util util;
	static double lat = 0;
	static double lon=0;
	MainActivity mainActivity;
	// DECLARO Strings
	String appReqUpdate, appMinVers;
	
	private String provider;
	Intent mainIntent =null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.third_frag, container, false);

		TextView tv = (TextView) v.findViewById(R.id.tvFragThird);
		tv.setText(getArguments().getString("msg"));

		final CheckBox cbxDontShow = (CheckBox) v
				.findViewById(R.id.cbxDontShow);

		Button btnFinish = (Button) v.findViewById(R.id.btnFinish);
		btnFinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getActivity();
				if (cbxDontShow.isChecked() == true) {
					
					SharedPreferences sharedPref = context
							.getSharedPreferences("MisPreferencias",
									Context.MODE_PRIVATE);

					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putBoolean("notour", true);
					editor.commit();

			       
				} 
					
					Intent mainIntent = new Intent().setClass(context, Map.class);
					System.out.println(lat);
			        mainIntent.putExtra("latitudSplash", lat);
					mainIntent.putExtra("longitudSplash", lon);
					startActivity(mainIntent);
					getActivity().finish();
			
			}
		});
		
	

		return v;
	}

	public static ThirdFragment newInstance(String text, double lati, double lng) {

		ThirdFragment f = new ThirdFragment();
		Bundle b = new Bundle();
		b.putString("msg", text);
		lat = lati;
		lon = lng;
		f.setArguments(b);

		return f;
	}
	

	}