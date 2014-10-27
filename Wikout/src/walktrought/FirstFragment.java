package walktrought;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wikout.R;

public class FirstFragment extends Fragment {
	Context context = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.first_frag, container, false);

		TextView tv = (TextView) v.findViewById(R.id.tvFragFirst);
		String string = getString(R.string.walkthrougth1);
		tv.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/Roboto-Regular.ttf"));
		tv.setText(string);

		return v;
	}

	public static FirstFragment newInstance() {

		FirstFragment f = new FirstFragment();

		return f;
	}
}