package com.honey96dev.homemadeproduct.p4manager

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

class ManagerStoreListFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mProducts: ArrayList<ManagerStoreListAdapter.ManagerStore> = ArrayList()
    internal var mStoresAdapter: ManagerStoreListAdapter? = null
    internal var mStoresView: RecyclerView? = null
    internal var mAddStoreFab: FloatingActionButton? = null
    internal var mGetStoresTask: GetStoresTask? = null

    internal var mAddDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_manager_store_list, container, false)

        mStoresAdapter = ManagerStoreListAdapter(context!!, mProducts)

        mStoresView = rootView.findViewById<View>(R.id.product_recycler_view) as RecyclerView
        mStoresView!!.setHasFixedSize(true)
        mStoresView!!.layoutManager = LinearLayoutManager(context)
        mStoresView!!.itemAnimator = ScaleUpAndDownItemAnimator()
        mStoresView!!.adapter = mStoresAdapter

        mAddStoreFab = rootView.findViewById<View>(R.id.add_store_fab) as FloatingActionButton
        mAddStoreFab!!.setOnClickListener {
            val addStoreDialogBuilder = AlertDialog.Builder(context!!)
            // Set title, icon, can not cancel properties.
            addStoreDialogBuilder.setTitle(R.string.title_add_store)
            addStoreDialogBuilder.setIcon(R.drawable.ic_fiber_new_accent_24dp)
            addStoreDialogBuilder.setCancelable(true)

            // Init popup dialog view and it's ui controls.
            val layoutInflater = LayoutInflater.from(context)

            // Inflate the popup dialog from a layout xml file.
            val dialogView = layoutInflater.inflate(R.layout.dialog_manager_add_store, null)

            // Get user input edittext and button ui controls in the popup dialog.
            val nameView = dialogView.findViewById<View>(R.id.name_edit_text) as EditText
            val descriptionView = dialogView.findViewById<View>(R.id.description_edit_text) as EditText
            val iconView = dialogView.findViewById<View>(R.id.icon_edit_text) as EditText
            val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
            val saveButton = dialogView.findViewById<Button>(R.id.save_button)

            // Set the inflated layout view object to the AlertDialog builder.
            addStoreDialogBuilder.setView(dialogView)
            addStoreDialogBuilder.setCancelable(false)

            // Create AlertDialog and show.
            mAddDialog = addStoreDialogBuilder.create()
            mAddDialog!!.show()

            // When user click the save user data button in the popup dialog.
            saveButton.setOnClickListener {
                nameView.error = null
                descriptionView.error = null
                iconView.error = null

                val name = nameView.text.toString()
                val description = descriptionView.text.toString()
                val icon = iconView.text.toString()

                var cancel = false
                var focusView: View? = null

                if (TextUtils.isEmpty(icon)) {
                    iconView.error = getString(R.string.error_field_required)
                    focusView = iconView
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
                    val addStoreTask = AddStoreTask(name, description, icon)
                    addStoreTask.execute()
                    mAddDialog!!.cancel()
                }
            }

            cancelButton.setOnClickListener { mAddDialog!!.cancel() }
        }

        mGetStoresTask = GetStoresTask()
        mGetStoresTask!!.execute()

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
                        MESSAGE_SHOW_TOAST_THREAD -> {
                        }
                    }
                }
            }
        }
    }

    internal inner class GetStoresTask : AsyncTask<String, Void, String>() {
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

        override fun onPostExecute(responseText: String?) {
            super.onPostExecute(responseText)

            if (responseText == null) {
                return
            }
            mProducts.clear()

            try {
                Log.e("products-api", responseText)
                val json = JSONObject(responseText)
                val stores = json.getJSONArray("stores")
                var item: JSONObject
                val cnt = stores.length()
                for (i in 0 until cnt) {
                    item = stores.getJSONObject(i)
                    mProducts.add(ManagerStoreListAdapter.ManagerStore(
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

            mStoresAdapter!!.notifyDataSetChanged()
        }
    }


    internal inner class AddStoreTask(var mName: String, var mDescription: String, var mIcon: String) : AsyncTask<String, Void, String>() {
        var mMethod = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): String? {
            var stringUrl = String.format("%s/add_store.php?" + "userid=%s&name=%s&description=%s&icon=%s",
                    G.SERVER_URL, G.userInfo.UserID, mName, mDescription, mIcon)
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
                stringUrl = String.format("%s/get_stores.php?stores", G.SERVER_URL)
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
            Toast.makeText(context, R.string.message_store_added, Toast.LENGTH_LONG).show()
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
                val stores = json.getJSONArray("stores")
                var item: JSONObject
                val cnt = stores.length()
                for (i in 0 until cnt) {
                    item = stores.getJSONObject(i)
                    mProducts.add(ManagerStoreListAdapter.ManagerStore(
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

            mStoresAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        internal val MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1
        internal val MESSAGE_SHOW_TOAST_THREAD = 2

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(): ManagerStoreListFragment {
            return ManagerStoreListFragment()
        }
    }
}