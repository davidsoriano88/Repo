package walktrought;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wikout.R;
import com.wikout.Util;

public class FourthFragment extends Fragment {

	// Context context = getParentFragment().getActivity();
	Util util;
	static double lat = 0;
	static double lon = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fourth_frag, container, false);

		TextView tv = (TextView) v.findViewById(R.id.tvFragFourth);
		String string = getString(R.string.walkthrougth4);
		tv.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf"));
		
		tv.setText(string);

		
			
		Fragment fragmentC = new LastFragment();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.add(R.id.lastfragment, fragmentC ).commit();
		
		return v;
	}

	public static FourthFragment newInstance(double lati, double lng) {

		FourthFragment f = new FourthFragment();

		return f;
	}
}
