package com.honey96dev.homemadeproduct

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout

class CustomerProductListActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: CustomerSectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    internal var mNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_product_list)

        mSectionsPagerAdapter = CustomerSectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.viewPager)
        mViewPager!!.adapter = mSectionsPagerAdapter
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                when (i) {
                    0 -> mNavigationView!!.selectedItemId = R.id.navigation_product_list
                    1 -> mNavigationView!!.selectedItemId = R.id.navigation_cart_list
                }
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })

        mNavigationView = findViewById(R.id.navigation)
        mNavigationView!!.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_product_list -> {
                    mViewPager!!.currentItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_cart_list -> {
                    mViewPager!!.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        //
        //        LinearLayout.LayoutParams parameter =  (LinearLayout.LayoutParams) mViewPager.getLayoutParams();
        //        parameter.setMargins(parameter.leftMargin, parameter.topMargin, parameter.rightMargin, mNavigationView.getHeight()); // left, top, right, bottom
        //        mViewPager.setLayoutParams(parameter);
    }
}
