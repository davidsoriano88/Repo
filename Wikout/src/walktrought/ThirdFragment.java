package walktrought;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wikout.R;

public class ThirdFragment extends Fragment {

	Intent mainIntent = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.third_frag, container, false);

		TextView tv = (TextView) v.findViewById(R.id.tvFragThird);
		String string = getString(R.string.walkthrougth3);
		tv.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf"));
		
		tv.setText(string);

		return v;
	}

	public static ThirdFragment newInstance() {

		ThirdFragment f = new ThirdFragment();

		return f;

	}
}