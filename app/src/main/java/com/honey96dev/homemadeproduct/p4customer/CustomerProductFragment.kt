package com.honey96dev.homemadeproduct.p4customer

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.tools.G
import com.squareup.picasso.Picasso
import okhttp3.HttpUrl
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CustomerProductFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mProductID: String? = null
    internal var mGetProductTask: GetProductTask? = null

    internal var mImageView: ImageView? = null
    internal var mNameView: EditText? = null
    internal var mDescriptionView: EditText? = null
    internal var mPriceView: EditText? = null
    internal var mDateView: EditText? = null
    internal var mQuantityView: EditText? = null
    internal var mLikesView: EditText? = null
    internal var mCartQuantityView: EditText? = null

    //    String mImage;
    internal var mName: String? = null
    internal var mDescription: String? = null
    internal var mImg1: String? = null
    internal var mPrice: Double = 0.toDouble()
    internal var mDate: String? = null
    internal var mQuantity: String? = null
    internal var mLikes: String? = null
    internal var mCartQuantity: Double? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_customer_product, container, false)

        createUpdateUiHandler()
        mImageView = rootView.findViewById<View>(R.id.image_view) as ImageView
        mNameView = rootView.findViewById<View>(R.id.name_edit_text) as EditText
        mDescriptionView = rootView.findViewById<View>(R.id.description_edit_text) as EditText
        mPriceView = rootView.findViewById<View>(R.id.price_edit_text) as EditText
        mDateView = rootView.findViewById<View>(R.id.date_edit_text) as EditText
        mQuantityView = rootView.findViewById<View>(R.id.quantity_edit_text) as EditText
        mLikesView = rootView.findViewById<View>(R.id.likes_edit_text) as EditText
        mCartQuantityView = rootView.findViewById(R.id.cart_quantity_edit_text)
        val cartButton = rootView.findViewById<View>(R.id.save_button) as Button

        cartButton.setOnClickListener {
            mCartQuantity = java.lang.Double.valueOf(mCartQuantityView!!.text.toString())
            Thread(Runnable {
                val cartTask = CartProductTask(
                        mCartQuantity!!)
                cartTask.execute()
            }).start()
        }

        val intent = (activity as CustomerProductActivity).intent
        mProductID = intent.getStringExtra(PRODUCT_ID_KEY)
        mGetProductTask = GetProductTask(mProductID!!)
        mGetProductTask!!.execute()

        return rootView
    }

    internal fun createUpdateUiHandler() {
        if (updateUIHandler == null) {
            updateUIHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    // Means the message is sent from child thread.
                    when (msg.what) {
                        MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD -> {
                            try {
                                Picasso.get().load(mImg1)
                                        .into(mImageView)
                                if (!mImg1!!.isEmpty()) {
                                    mImageView!!.background = null
                                }
                            } catch (e: Exception) {
                                mImageView!!.background = context!!.getDrawable(R.drawable.image_border)
                                e.printStackTrace()
                            }

                            mNameView!!.setText(mName)
                            mDescriptionView!!.setText(mDescription)
                            mPriceView!!.setText(mPrice.toString())
                            mDateView!!.setText(mDate)
                            mQuantityView!!.setText(mQuantity)
                            mLikesView!!.setText(mLikes)
                        }
                        MESSAGE_SHOW_CART_PRODUCT_TOAST_THREAD -> Toast.makeText(context, R.string.message_product_saved, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    internal inner class GetProductTask(productID: String) : AsyncTask<String, Void, String>() {
        var mMethod = "GET"
        var mStoreID: String? = null
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        init {
            this.mStoreID = productID
        }

        override fun doInBackground(vararg params: String): String? {
            val stringUrl = String.format("%s/get_stores.php?pdid=%s", G.SERVER_URL, this.mStoreID)
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

            try {
                Log.e("products-api", responseText)
                val json = JSONObject(responseText)
                val products = json.getJSONArray("products")
                if (products.length() > 0) {
                    val item = products.getJSONObject(0)
                    mImg1 = item.getString("img1")
                    mName = item.getString("name")
                    mDescription = item.getString("description")
                    mPrice = item.getDouble("price")
                    mDate = item.getString("date")
                    mQuantity = item.getString("quantity")
                    mLikes = item.getString("quantity")
                }
                //                products.getJSONObject()
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            //
            val message = Message()
            message.what = MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD
            updateUIHandler!!.sendMessage(message)
        }
    }


    internal inner class CartProductTask(var mQuantity: Double) : AsyncTask<String, Void, Boolean>() {
        var mMethod = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): Boolean? {
            val url = HttpUrl.Builder()
                    .scheme(G.SERVER_SCHEME)
                    .host(G.SERVER_IP)
                    .addPathSegment("add_cart.php")
                    .addQueryParameter("item_id", mProductID)
                    .addQueryParameter("user_id", G.userInfo.UserID)
                    .addQueryParameter("quantity", java.lang.String.valueOf(mQuantity))
                    .build()
            var stringUrl = url.toString()
            Log.e("add-cart-url", stringUrl)
            var result: String?
            var inputLine: String

            try {
                //============add=============
                val myUrl = URL(stringUrl)
                //Create a connection
                val connection = myUrl.openConnection() as HttpURLConnection
                //Set methods and timeouts
                connection.requestMethod = mMethod
                connection.readTimeout = READ_TIMEOUT
                connection.connectTimeout = CONNECTION_TIMEOUT
                //                connection.setRequestProperty("Accept", "application/json");

                connection.connect()

                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                inputLine = reader.use(BufferedReader::readText)
                stringBuilder.append(inputLine)

                reader.close()
                streamReader.close()

                result = stringBuilder.toString()

                Log.e("add-cart", result)

                val json = JSONObject(result)
                result = json.getString("result")
                return if (result != "success") {
                    false
                } else true

            } catch (e: IOException) {
                e.printStackTrace()
                result = null
            } catch (e: JSONException) {
                e.printStackTrace()
                result = null
            }

            return false
        }

        override fun onPostExecute(responseText: Boolean?) {
            super.onPostExecute(responseText)
            if (responseText == true) {
                val message = Message()
                message.what = MESSAGE_SHOW_CART_PRODUCT_TOAST_THREAD
                //            message.arg1 = 1;
                updateUIHandler!!.sendMessage(message)
            }
        }
    }

    companion object {
        val PRODUCT_ID_KEY = "PRODUCT_ID_KEY"
        internal val MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD = 1
        internal val MESSAGE_SHOW_CART_PRODUCT_TOAST_THREAD = 2

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(): CustomerProductFragment {
            return CustomerProductFragment()
        }
    }
}
