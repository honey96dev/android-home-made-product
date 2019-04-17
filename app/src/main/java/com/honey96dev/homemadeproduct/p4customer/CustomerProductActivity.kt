package com.honey96dev.homemadeproduct.p4customer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.honey96dev.homemadeproduct.R

class CustomerProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_product)
    }

    //    @Override
    //    public void onBackPressed() {
    //        new AlertDialog.Builder(this)
    //                .setTitle(R.string.title_confirm)
    //                .setMessage(R.string.message_confirm_back)
    //                .setNegativeButton(android.R.string.no, null)
    //                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    //
    //                    public void onClick(DialogInterface arg0, int arg1) {
    //                        CustomerProductActivity.super.onBackPressed();
    //                    }
    //                }).create().show();
    //    }
}
