package com.honey96dev.homemadeproduct.p4manager

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.tools.G
import com.honey96dev.homemadeproduct.tools.ScaleUpAndDownItemAnimator
import okhttp3.HttpUrl
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class CustomerCartListFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mUserID: String? = null
    internal var mCartItems: ArrayList<CustomerCartListAdapter.CustomerCartItem> = ArrayList()
    internal var mCartItemsAdapter: CustomerCartListAdapter? = null
    internal var mCartItemsView: RecyclerView? = null
    internal var mGetCartItemsTask: GetCartItemsTask? = null

    internal var mAddDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_customer_cart_list, container, false)

        createUpdateUiHandler()

        mCartItemsAdapter = CustomerCartListAdapter(context!!, mCartItems)

        mCartItemsView = rootView.findViewById<View>(R.id.cart_recycler_view) as RecyclerView
        mCartItemsView!!.setHasFixedSize(true)
        mCartItemsView!!.layoutManager = LinearLayoutManager(context)
        mCartItemsView!!.itemAnimator = ScaleUpAndDownItemAnimator()
        mCartItemsView!!.adapter = mCartItemsAdapter

        mUserID = G.userInfo.UserID
        mGetCartItemsTask = GetCartItemsTask(mUserID!!)
        mGetCartItemsTask!!.execute()

        return rootView
    }

    override fun onResume() {
        super.onResume()

        mGetCartItemsTask = GetCartItemsTask(mUserID!!)
        mGetCartItemsTask!!.execute()
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


    internal inner class GetCartItemsTask(storeID: String) : AsyncTask<String, Void, String>() {
        var mMethod = "GET"
        var mStoreID: String? = null
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        init {
            this.mStoreID = storeID
        }

        override fun doInBackground(vararg params: String): String? {
//            val stringUrl = String.format("%s/get_cart.php?user_id=%s", G.SERVER_URL, this.mUserID)
            val url = HttpUrl.Builder()
                    .scheme(G.SERVER_SCHEME)
                    .host(G.SERVER_IP)
                    .addPathSegment("get_cart.php")
                    .addQueryParameter("user_id", G.userInfo.UserID)
                    .build()
            val stringUrl = url.toString()
            Log.e("get_cart-api", stringUrl)
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
                val reader = BufferedReader(streamReader!!)
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
            mCartItems.clear()

            try {
                Log.e("get_cart-api", responseText)
                val json = JSONObject(responseText)
                val products = json.getJSONArray("data")
                var item: JSONObject
                val cnt = products.length()
                for (i in 0 until cnt) {
                    item = products.getJSONObject(i)
                    mCartItems.add(CustomerCartListAdapter.CustomerCartItem(
                            item.getString("id"),
                            mStoreID!!,
                            item.getString("name"),
                            item.getString("img1"),
                            item.getDouble("uprice"),
                            item.getDouble("quantity")
                    ))
                }
                //                products.getJSONObject()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mCartItemsAdapter!!.notifyDataSetChanged()
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
        fun newInstance(): CustomerCartListFragment {
            return CustomerCartListFragment()
        }
    }
}
