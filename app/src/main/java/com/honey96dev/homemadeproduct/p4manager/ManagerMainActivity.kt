package com.honey96dev.homemadeproduct.p4manager

import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.honey96dev.homemadeproduct.R

class ManagerMainActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: ManagerSectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    internal var mNavigationView: BottomNavigationView? = null

    var mStoreID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mStoreID = intent.getStringExtra(ManagerProductListFragment.STORE_ID_KEY)

        setContentView(R.layout.activity_manager_main)

        mSectionsPagerAdapter = ManagerSectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.view_pager)
        mViewPager!!.adapter = mSectionsPagerAdapter
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                when (i) {
                    0 -> mNavigationView!!.selectedItemId = R.id.navigation_product_list
                    1 -> mNavigationView!!.selectedItemId = R.id.navigation_order_list
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
                R.id.navigation_order_list -> {
                    mViewPager!!.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setTitle(R.string.title_confirm)
                .setMessage(R.string.message_confirm_back)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes) { arg0, arg1 -> super@ManagerMainActivity.onBackPressed() }.create().show()
    }
}