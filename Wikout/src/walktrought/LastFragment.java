package walktrought;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.wikout.Map;
import com.wikout.R;
import com.wikout.Util;

public class LastFragment extends Fragment {

	// Context context = getParentFragment().getActivity();
	Util util;
	static double lat = 0;
	static double lon = 0;

	Button btnFinish;
	CheckBox cbxDontShow;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.last_frag, container, false);

		btnFinish = (Button) v.findViewById(R.id.btnFinish);

		cbxDontShow = (CheckBox) v.findViewById(R.id.cbxDontShow);

		cbxDontShow.setTypeface(Typeface.createFromAsset(getActivity()
				.getAssets(), "fonts/Roboto-Regular.ttf"));
		btnFinish.setTypeface(Typeface.createFromAsset(getActivity()
				.getAssets(), "fonts/Roboto-Regular.ttf"));

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

}
