package utils;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;


public class CustomOnItemSelectedListener implements OnItemSelectedListener {
	Util util = new Util();
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		util.showToast(parent.getContext(),"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString());
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}