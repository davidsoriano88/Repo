package utils;

import java.util.Arrays;
import java.util.List;

import com.wikout.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

private List<Integer> images;

public ArrayAdapterWithIcon(Context context, List<String> items, List<Integer> images) {
    super(context, android.R.layout.select_dialog_item, items);
    this.images = images;
}

public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
    super(context, android.R.layout.select_dialog_item, items);
    this.images = Arrays.asList(images);
}

@Override
public View getView(int position, View convertView, ViewGroup parent) {
    View view = super.getView(position, convertView, parent);
	Drawable img;
	Resources res = parent.getResources();
	img = res.getDrawable(images.get(position));
	//You need to setBounds before setCompoundDrawables , or it couldn't display
	img.setBounds(0, 0, 37, 40);
	TextView textView = (TextView) view.findViewById(android.R.id.text1);
    textView.setBackgroundColor(Color.WHITE);
    textView.setCompoundDrawables(img, null, null, null); 
   // textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
    //textView.setMaxHeight(maxHeight);
    textView.setCompoundDrawablePadding(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 12, getContext().getResources().getDisplayMetrics()));
    return view;
}

}
