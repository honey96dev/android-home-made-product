package com.honey96dev.homemadeproduct.p4manager

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.honey96dev.homemadeproduct.R

class ManagerProductListActivity : AppCompatActivity() {
    //    public final static String STORE_ID_KEY = "STORE_ID_KEY";

    internal var mStoreID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        mStoreID = getIntent().getStringExtra(STORE_ID_KEY);
        setContentView(R.layout.activity_manager_product_list)
    }
}
