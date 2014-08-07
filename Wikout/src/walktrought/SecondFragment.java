package walktrought;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wikout.R;

public class SecondFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.second_frag, container, false);

		TextView tv = (TextView) v.findViewById(R.id.tvFragSecond);

		String string = getString(R.string.walkthrougth2);
		tv.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/Roboto-Regular.ttf"));

		tv.setText(string);

		return v;
	}

	public static SecondFragment newInstance() {

		SecondFragment f = new SecondFragment();

		return f;

	}
}