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
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast

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
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ManagerProductListFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mStoreID: String? = null
    internal var mProducts: ArrayList<ManagerProductListAdapter.ManagerProduct> = ArrayList()
    internal var mProductAdapter: ManagerProductListAdapter? = null
    internal var mProductsView: RecyclerView? = null
    internal var mAddStoreFab: FloatingActionButton? = null
    internal var mGetProductsTask: GetProductsTask? = null

    internal var mAddDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_manager_product_list, container, false)

        createUpdateUiHandler()

        mProductAdapter = ManagerProductListAdapter(context!!, mProducts)

        mProductsView = rootView.findViewById<View>(R.id.product_recycler_view) as RecyclerView
        mProductsView!!.setHasFixedSize(true)
        mProductsView!!.layoutManager = LinearLayoutManager(context)
        mProductsView!!.itemAnimator = ScaleUpAndDownItemAnimator()
        mProductsView!!.adapter = mProductAdapter

        mAddStoreFab = rootView.findViewById<View>(R.id.add_store_fab) as FloatingActionButton
        mAddStoreFab!!.setOnClickListener(object : View.OnClickListener {
            internal var mCalendar: Calendar? = null
            internal var mDateView: EditText? = null

            override fun onClick(v: View) {
                val addProductDialogBuilder = AlertDialog.Builder(context!!)
                // Set title, icon, can not cancel properties.
                addProductDialogBuilder.setTitle(R.string.title_add_product)
                addProductDialogBuilder.setIcon(R.drawable.ic_fiber_new_accent_24dp)
                addProductDialogBuilder.setCancelable(true)

                // Init popup dialog view and it's ui controls.
                val layoutInflater = LayoutInflater.from(context)

                // Inflate the popup dialog from a layout xml file.
                val dialogView = layoutInflater.inflate(R.layout.dialog_manager_add_product, null)

                // Get user input edittext and button ui controls in the popup dialog.
                val nameView = dialogView.findViewById<View>(R.id.name_edit_text) as EditText
                val descriptionView = dialogView.findViewById<View>(R.id.description_edit_text) as EditText
                val img1View = dialogView.findViewById<View>(R.id.img1_edit_text) as EditText
                val priceView = dialogView.findViewById<View>(R.id.price_edit_text) as EditText
                mDateView = dialogView.findViewById<View>(R.id.date_edit_text) as EditText
                val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
                val saveButton = dialogView.findViewById<Button>(R.id.save_button)

                updateLabel(Date())
                // Set the inflated layout view object to the AlertDialog builder.
                addProductDialogBuilder.setView(dialogView)
                addProductDialogBuilder.setCancelable(false)

                // Create AlertDialog and show.
                mAddDialog = addProductDialogBuilder.create()
                mAddDialog!!.show()

                //                mDateView!!.readon

                mCalendar = Calendar.getInstance()

                val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    // TODO Auto-generated method stub
                    mCalendar!!.set(Calendar.YEAR, year)
                    mCalendar!!.set(Calendar.MONTH, monthOfYear)
                    mCalendar!!.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateLabel(mCalendar!!.time)
                }

                mDateView!!.setOnClickListener {
                    // TODO Auto-generated method stub
                    DatePickerDialog(context!!, date, mCalendar!!.get(Calendar.YEAR),
                            mCalendar!!.get(Calendar.MONTH),
                            mCalendar!!.get(Calendar.DAY_OF_MONTH)).show()
                }

                // When user click the save user data button in the popup dialog.
                saveButton.setOnClickListener {
                    nameView.error = null
                    descriptionView.error = null
                    img1View.error = null
                    mDateView!!.error = null

                    val name = nameView.text.toString()
                    val description = descriptionView.text.toString()
                    val img1 = img1View.text.toString()
                    var price = 0.0
                    try {
                        price = java.lang.Double.valueOf(priceView.text.toString())
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }

                    val date = mDateView!!.text.toString()

                    var cancel = false
                    var focusView: View? = null

                    if (!isDateStringValid(date)) {
                        mDateView!!.error = getString(R.string.error_field_invalid_date)
                        focusView = mDateView
                        cancel = true
                    }

                    if (TextUtils.isEmpty(img1)) {
                        img1View.error = getString(R.string.error_field_required)
                        focusView = img1View
                        cancel = true
                    }

                    if (price <= 0) {
                        priceView.error = getString(R.string.error_field_must_positive)
                        focusView = priceView
                        cancel = true
                    }

                    if (TextUtils.isEmpty(img1)) {
                        img1View.error = getString(R.string.error_field_required)
                        focusView = img1View
                        cancel = true
                    }

                    if (TextUtils.isEmpty(description)) {
                        descriptionView.error = getString(R.string.error_field_required)
                        focusView = descriptionView
                        cancel = true
                    }

                    if (TextUtils.isEmpty(name)) {
                        nameView.error = getString(R.string.error_field_required)
                        focusView = nameView
                        cancel = true
                    }

                    if (cancel) {
                        // There was an error; don't attempt login and focus the first
                        // form field with an error.
                        focusView!!.requestFocus()
                    } else {
                        val addProductTask = AddProductTask(
                                name, description, img1, price, date)
                        addProductTask.execute()
                        //                            mAddDialog.cancel();
                    }
                }

                cancelButton.setOnClickListener { mAddDialog!!.cancel() }
            }

            internal fun updateLabel(date: Date) {
                val myFormat = G.DATE_FORMAT //In which you need put here
                val sdf = SimpleDateFormat(myFormat, Locale.US)

                mDateView!!.setText(sdf.format(date))
            }

            internal fun isDateStringValid(dateString: String): Boolean {
                val format = SimpleDateFormat(G.DATE_FORMAT)
                format.isLenient = false

                try {
                    format.parse(dateString)
                    return true
                } catch (e: ParseException) {
                    return false
                }

            }
        })

        mStoreID = (activity as ManagerMainActivity).mStoreID
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
                    mProducts.add(ManagerProductListAdapter.ManagerProduct(
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


    internal inner class AddProductTask(var mName: String, var mDescription: String, var mImg1: String, var mPrice: Double, var mDate: String) : AsyncTask<String, Void, String>() {
        var mMethod = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): String? {
            var stringUrl = String.format("%s/add_product.php?" + "storeid=%s&name=%s&description=%s&img1=%s&price=%f&date=%s",
                    G.SERVER_URL, G.userInfo.StoreID, mName, mDescription, mImg1, mPrice, mDate)
            var result: String?
            var inputLine: String

            try {
                //============add=============
                var myUrl = URL(stringUrl)
                //Create a connection
                var connection = myUrl.openConnection() as HttpURLConnection
                //Set methods and timeouts
                connection.requestMethod = mMethod
                connection.readTimeout = READ_TIMEOUT
                connection.connectTimeout = CONNECTION_TIMEOUT
                connection.setRequestProperty("Accept", "application/json")

                connection.connect()

                var streamReader = InputStreamReader(connection.inputStream)
                var reader = BufferedReader(streamReader)
                var stringBuilder = StringBuilder()
                inputLine = reader.use(BufferedReader::readText)
                stringBuilder.append(inputLine)

                reader.close()
                streamReader.close()

                result = stringBuilder.toString()

                Log.e("add-api", result)

                val json = JSONObject(result)
                result = json.getString("result")
                if (result != "success") {
                    return null
                }

                //============reload store list=============
                stringUrl = String.format("%s/get_stores.php?stid=%s", G.SERVER_URL, G.userInfo.StoreID)
                //Create a URL object holding our url
                myUrl = URL(stringUrl)
                //Create a connection
                connection = myUrl.openConnection() as HttpURLConnection
                //Set methods and timeouts
                connection.requestMethod = mMethod
                connection.readTimeout = READ_TIMEOUT
                connection.connectTimeout = CONNECTION_TIMEOUT
                connection.setRequestProperty("Accept", "application/json")

                connection.connect()

                streamReader = InputStreamReader(connection.inputStream)
                reader = BufferedReader(streamReader)
                stringBuilder = StringBuilder()
                inputLine = reader.use(BufferedReader::readText)
                stringBuilder.append(inputLine)

                reader.close()
                streamReader.close()

                result = stringBuilder.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                result = null
            } catch (e: JSONException) {
                e.printStackTrace()
                result = null
            }

            mAddDialog!!.cancel()

            val message = Message()
            message.what = MESSAGE_SHOW_ADD_PRODUCT_TOAST_THREAD
            //            message.arg1 = 1;
            updateUIHandler!!.sendMessage(message)

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
                    mProducts.add(ManagerProductListAdapter.ManagerProduct(
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
                //                stores.getJSONObject()
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
        fun newInstance(): ManagerProductListFragment {
            return ManagerProductListFragment()
        }
    }
}
