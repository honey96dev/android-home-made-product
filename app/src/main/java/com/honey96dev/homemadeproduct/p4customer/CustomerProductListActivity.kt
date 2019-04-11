package com.honey96dev.homemadeproduct.p4customer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.p4manager.CustomerProductListFragment

class CustomerProductListActivity : AppCompatActivity() {

    internal var mStoreID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mStoreID = intent.getStringExtra(CustomerProductListFragment.STORE_ID_KEY)

        setContentView(R.layout.activity_customer_product_list)
    }
}
