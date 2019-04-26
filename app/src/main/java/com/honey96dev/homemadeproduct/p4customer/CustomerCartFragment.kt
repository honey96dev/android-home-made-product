package com.honey96dev.homemadeproduct.p4customer

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.honey96dev.homemadeproduct.tools.G
import com.honey96dev.homemadeproduct.R
import com.squareup.picasso.Picasso

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException

import okhttp3.HttpUrl

class CustomerCartFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mCartID: String? = null
    internal var mGetCartTask: GetCartTask? = null

    internal var mImageView: ImageView? = null
    internal var mNameView: EditText? = null
    internal var mDescriptionView: EditText? = null
    internal var mPriceView: EditText? = null
    internal var mDateView: EditText? = null
    internal var mQuantityView: EditText? = null

    //    String mImage;
    internal var mName: String? = null
    internal var mDescription: String? = null
    internal var mImg1: String? = null
    internal var mPrice: Double = 0.toDouble()
    internal var mDate: String? = null
    internal var mQuantity: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_customer_cart, container, false)

        createUpdateUiHandler()
        mImageView = rootView.findViewById<View>(R.id.image_view) as ImageView
        mNameView = rootView.findViewById<View>(R.id.name_edit_text) as EditText
        mDescriptionView = rootView.findViewById<View>(R.id.description_edit_text) as EditText
        mPriceView = rootView.findViewById<View>(R.id.price_edit_text) as EditText
        mDateView = rootView.findViewById<View>(R.id.date_edit_text) as EditText
        mQuantityView = rootView.findViewById<View>(R.id.quantity_edit_text) as EditText
        val cancelButton = rootView.findViewById<View>(R.id.cancel_button) as Button
        val orderButton = rootView.findViewById<View>(R.id.order_button) as Button

        cancelButton.setOnClickListener {
            activity!!.finish()
        }

        orderButton.setOnClickListener {
            Thread(Runnable {
                val cartTask = OrderCartTask()
                cartTask.execute()
            }).start()
        }

        val intent = (activity as CustomerCartActivity).intent
        mCartID = intent.getStringExtra(PRODUCT_ID_KEY)
        mGetCartTask = GetCartTask()
        mGetCartTask!!.execute()

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
                                if (mImg1!!.isNotBlank()) {
                                    Picasso.get().load(mImg1)
                                            .into(mImageView)
                                    mImageView!!.background = null
                                } else {
                                    mImageView!!.setImageBitmap(null)
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
                        }
                        MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD -> Toast.makeText(context, R.string.message_product_saved, Toast.LENGTH_LONG).show()
                        MESSAGE_SHOW_INVALID_FILE_TOAST_THREAD -> Toast.makeText(context, R.string.message_invalid_file, Toast.LENGTH_LONG).show()
                        MESSAGE_SHOW_FILE_NOT_FOUND_TOAST_THREAD -> Toast.makeText(context, R.string.message_file_not_found, Toast.LENGTH_LONG).show()
                        MESSAGE_SHOW_SERVER_ERROR_TOAST_THREAD -> Toast.makeText(context, R.string.message_server_error, Toast.LENGTH_LONG).show()
                        MESSAGE_SHOW_IO_ERROR_TOAST_THREAD -> Toast.makeText(context, R.string.message_io_error, Toast.LENGTH_LONG).show()
                        MESSAGE_SHOW_ERROR_TOAST_THREAD -> Toast.makeText(context, R.string.message_error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    internal inner class GetCartTask() : AsyncTask<String, Void, String>() {
        var mMethod = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000


        override fun doInBackground(vararg params: String): String? {
            val url = HttpUrl.Builder()
                    .scheme(G.SERVER_SCHEME)
                    .host(G.SERVER_IP)
                    .addPathSegment("get_cart.php")
                    .addQueryParameter("cart_id", mCartID)
                    .build()
            var stringUrl = url.toString()
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
                val item = json.getJSONObject("data")
                mImg1 = item.getString("img1")
                mName = item.getString("name")
                mDescription = item.getString("description")
                mPrice = item.getDouble("price")
                mDate = item.getString("date")
                mQuantity = item.getString("quantity")
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


    internal inner class OrderCartTask() : AsyncTask<String, Void, Boolean>() {
        var mMethod = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): Boolean? {
            val url = HttpUrl.Builder()
                    .scheme(G.SERVER_SCHEME)
                    .host(G.SERVER_IP)
                    .addPathSegment("order_cart.php")
                    .addQueryParameter("cart_id", mCartID)
                    .build()
            var stringUrl = url.toString()
            Log.e("save-product-url", stringUrl)
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

                Log.e("add-api", result)

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
                message.what = MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD
                //            message.arg1 = 1;
                updateUIHandler!!.sendMessage(message)
            }
        }
    }

    companion object {
        val PRODUCT_ID_KEY = "PRODUCT_ID_KEY"
        internal val MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD = 1
        internal val MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD = 2
        internal val MESSAGE_SHOW_INVALID_FILE_TOAST_THREAD = 3
        internal val MESSAGE_SHOW_FILE_NOT_FOUND_TOAST_THREAD = 4
        internal val MESSAGE_SHOW_SERVER_ERROR_TOAST_THREAD = 5
        internal val MESSAGE_SHOW_IO_ERROR_TOAST_THREAD = 6
        internal val MESSAGE_SHOW_ERROR_TOAST_THREAD = 7

        internal val REQUEST_GET_SINGLE_FILE = 1

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(): CustomerCartFragment {
            return CustomerCartFragment()
        }
    }
}
