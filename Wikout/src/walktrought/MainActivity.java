package walktrought;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.CirclePageIndicator;
import com.wikout.R;

public class MainActivity extends FragmentActivity {

	public double latitudeSplash = 0, longitudeSplash = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.themed_circles);

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

		// Bind the title indicator to the adapter
		CirclePageIndicator circle = (CirclePageIndicator) findViewById(R.id.indicator);
		circle.setViewPager(pager);
		Bundle splash = getIntent().getExtras();
		latitudeSplash = splash.getDouble("latitudSplash");
		longitudeSplash = splash.getDouble("longitudSplash");

	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			switch (pos) {

			case 0:
				return FirstFragment.newInstance();
			case 1:
				return SecondFragment.newInstance();
			case 2:
				return ThirdFragment.newInstance();
			case 3:
				return FourthFragment.newInstance(latitudeSplash,
						longitudeSplash);
			default:
				return FourthFragment.newInstance(latitudeSplash,
						longitudeSplash);
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	}
}
