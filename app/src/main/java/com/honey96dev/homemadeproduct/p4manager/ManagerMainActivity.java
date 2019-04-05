package com.honey96dev.homemadeproduct.p4manager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.honey96dev.homemadeproduct.R;

public class ManagerMainActivity extends AppCompatActivity {

    private ManagerSectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    BottomNavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_main);

        mSectionsPagerAdapter = new ManagerSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        mNavigationView.setSelectedItemId(R.id.navigation_product_list);
                        break;
                    case 1:
                        mNavigationView.setSelectedItemId(R.id.navigation_order_list);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_product_list:
                        mViewPager.setCurrentItem(0);
                        return true;
                    case R.id.navigation_order_list:
                        mViewPager.setCurrentItem(1);
                        return true;
                }
                return false;
            }
        });
    }
}