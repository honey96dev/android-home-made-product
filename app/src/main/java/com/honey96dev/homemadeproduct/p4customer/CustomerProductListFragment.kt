package com.honey96dev.homemadeproduct.p4customer

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.honey96dev.homemadeproduct.tools.G
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.tools.ScaleUpAndDownItemAnimator

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

class CustomerProductListFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mProducts = ArrayList<CustomerProductListAdapter.CustomerProduct>()
    internal var mProductsAdapter: CustomerProductListAdapter? = null
    internal var mProductsView: RecyclerView? = null
    internal var mCustomerProductsTask: CustomerProductsTask? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_customer_product_list, container, false)

        mProductsAdapter = CustomerProductListAdapter(context!!, mProducts)

        mProductsView = rootView.findViewById<View>(R.id.product_recycler_view) as RecyclerView
        mProductsView!!.setHasFixedSize(true)
        mProductsView!!.layoutManager = LinearLayoutManager(context)
        mProductsView!!.itemAnimator = ScaleUpAndDownItemAnimator()
        mProductsView!!.adapter = mProductsAdapter



        mCustomerProductsTask = CustomerProductsTask()
        mCustomerProductsTask!!.execute()

        return rootView
    }

    internal fun createUpdateUiHandler() {
        if (updateUIHandler == null) {
            updateUIHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    // Means the message is sent from child thread.
                    when (msg.what) {
                        MESSAGE_UPDATE_TEXT_CHILD_THREAD -> {
                        }
                        MESSAGE_SHOW_ADD_PRODUCT_TOAST_THREAD -> {
                        }
                    }
                }
            }
        }
    }

    internal inner class CustomerProductsTask : AsyncTask<String, Void, String>() {
        var mMethod = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): String? {
            val stringUrl = String.format("%s/get_stores.php?stores", G.SERVER_URL)
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

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            mProducts.clear()

            try {
                Log.e("products-api", result)
                val json = JSONObject(result)
                val stores = json.getJSONArray("stores")
                var item: JSONObject
                val cnt = stores.length()
                for (i in 0 until cnt) {
                    item = stores.getJSONObject(i)
                    mProducts.add(CustomerProductListAdapter.CustomerProduct(
                            item.getString("id"),
                            item.getString("name"),
                            item.getString("description"),
                            item.getString("icon"),
                            item.getString("likes")
                    ))
                }
                //                stores.getJSONObject()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mProductsAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {
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