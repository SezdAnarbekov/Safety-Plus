package com.syezdsultanov.savetyplus.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.syezdsultanov.savetyplus.Fragments.PageOne;
import com.syezdsultanov.savetyplus.Fragments.PageThree;
import com.syezdsultanov.savetyplus.Fragments.PageTwo;
import com.syezdsultanov.savetyplus.InitPagerAdapter;
import com.syezdsultanov.savetyplus.R;

import java.util.ArrayList;
import java.util.List;

public class SlideLaunch extends FragmentActivity {

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Intent i = new Intent(SlideLaunch.this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SlideLaunch.this.startActivity(i);
            finish();
        }
    };
    private int dotsCount;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("first_time", false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("first_time", true);
            editor.apply();
            setContentView(R.layout.activity_launch);
            List fragments = new ArrayList();
            fragments.add(Fragment.instantiate(this, PageOne.class.getName()));
            fragments.add(Fragment.instantiate(this, PageTwo.class.getName()));
            fragments.add(Fragment.instantiate(this, PageThree.class.getName()));
            PagerAdapter pagerAdapter = new InitPagerAdapter(super.getSupportFragmentManager(), fragments);
            ViewPager pager = super.findViewById(R.id.viewpager);
            LinearLayout sliderDotspanel = findViewById(R.id.SliderDots);
            pager.setAdapter(pagerAdapter);
            dotsCount = pagerAdapter.getCount();
            dots = new ImageView[dotsCount];
            for (int i = 0; i < dotsCount; i++) {
                dots[i] = new ImageView(this);
                dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(8, 0, 8, 0);
                sliderDotspanel.addView(dots[i], params);
            }

            dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    for (int i = 0; i < dotsCount; i++) {
                        dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nonactive_dot));
                    }
                    dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

        } else {
            handler.sendEmptyMessage(0);
        }
    }
}



