package com.honey96dev.homemadeproduct.p4manager

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.honey96dev.homemadeproduct.tools.G
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.p4customer.CustomerProductListActivity
import com.honey96dev.homemadeproduct.tools.ScaleUpAndDownItemAnimator

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomerProductListFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mStoreID: String? = null
    internal var mProducts: ArrayList<CustomerProductListAdapter.CustomerProduct> = ArrayList()
    internal var mProductAdapter: CustomerProductListAdapter? = null
    internal var mProductsView: RecyclerView? = null
    internal var mGetProductsTask: GetProductsTask? = null

    internal var mAddDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_manager_product_list, container, false)

        createUpdateUiHandler()

        mProductAdapter = CustomerProductListAdapter(context!!, mProducts)

        mProductsView = rootView.findViewById<View>(R.id.product_recycler_view) as RecyclerView
        mProductsView!!.setHasFixedSize(true)
        mProductsView!!.layoutManager = LinearLayoutManager(context)
        mProductsView!!.itemAnimator = ScaleUpAndDownItemAnimator()
        mProductsView!!.adapter = mProductAdapter

        mStoreID = (activity as CustomerProductListActivity).mStoreID
        mGetProductsTask = GetProductsTask(mStoreID!!)
        mGetProductsTask!!.execute()

        return rootView
    }

    override fun onResume() {
        super.onResume()

        mGetProductsTask = GetProductsTask(mStoreID!!)
        mGetProductsTask!!.execute()
    }

    internal fun createUpdateUiHandler() {
        if (updateUIHandler == null) {
            updateUIHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    // Means the message is sent from child thread.
                    when (msg.what) {
                        MESSAGE_UPDATE_TEXT_CHILD_THREAD -> {
                        }
                        MESSAGE_SHOW_ADD_PRODUCT_TOAST_THREAD -> Toast.makeText(context, R.string.message_store_added, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    internal inner class GetProductsTask(storeID: String) : AsyncTask<String, Void, String>() {
        var mMethod = "GET"
        var mStoreID: String? = null
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        init {
            this.mStoreID = storeID
        }

        override fun doInBackground(vararg params: String): String? {
            val stringUrl = String.format("%s/get_stores.php?stid=%s", G.SERVER_URL, this.mStoreID)
            Log.e("product-api", stringUrl)
            var result: String?
            var inputLine: String

            try {
                //Create a URL object holding our url
                val myUrl = URL(stringUrl)
                //Create a connection
                val connection = myUrl.openConnection() as HttpURLConnection
                //Set methods and timeouts
                connection.requestMethod = mMethod
                connection.readTimeout = READ_TIMEOUT
                connection.connectTimeout = CONNECTION_TIMEOUT
                connection.setRequestProperty("Accept", "application/json")

                connection.connect()

                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                inputLine = reader.use(BufferedReader::readText)
                stringBuilder.append(inputLine)

                reader.close()
                streamReader.close()

                result = stringBuilder.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                result = null
            }

            return result
        }

        override fun onPostExecute(responseText: String?) {
            super.onPostExecute(responseText)

            if (responseText == null) {
                return
            }
            mProducts.clear()

            try {
                Log.e("products-api", responseText)
                val json = JSONObject(responseText)
                val products = json.getJSONArray("products")
                var item: JSONObject
                val cnt = products.length()
                for (i in 0 until cnt) {
                    item = products.getJSONObject(i)
                    mProducts.add(CustomerProductListAdapter.CustomerProduct(
                            item.getString("id"),
                            //                            item.getString("storeID"),
                            mStoreID!!,
                            item.getString("name"),
                            item.getString("description"),
                            item.getString("img1"),
                            item.getString("img2"),
                            item.getString("img3"),
                            item.getString("img4"),
                            item.getString("img5"),
                            item.getString("img6"),
                            item.getString("price"),
                            item.getString("date")
                    ))
                }
                //                products.getJSONObject()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mProductAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        val STORE_ID_KEY = "STORE_ID_KEY"
        internal val MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1
        internal val MESSAGE_SHOW_ADD_PRODUCT_TOAST_THREAD = 2

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(): CustomerProductListFragment {
            return CustomerProductListFragment()
        }
    }
}
