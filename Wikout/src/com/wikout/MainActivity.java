package com.wikout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity {

	public double latitudeSplash = 0, longitudeSplash = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);     

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        
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
            switch(pos) {

            case 0: return FirstFragment.newInstance("FirstFragment, Instance 1");
            case 1: return SecondFragment.newInstance("SecondFragment, Instance 1");
            case 2: return FirstFragment.newInstance("FirstFragment, Instance 1");
            case 3: return ThirdFragment.newInstance("ThirdFragment, Instance 2",latitudeSplash, longitudeSplash);
            default: return ThirdFragment.newInstance("ThirdFragment, Default",latitudeSplash, longitudeSplash);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }       
    }
}


