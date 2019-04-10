package com.honey96dev.homemadeproduct.p4manager

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
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
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.honey96dev.homemadeproduct.tools.G
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.tools.Uri2Path
import com.squareup.picasso.Picasso

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import okhttp3.HttpUrl

import android.app.Activity.RESULT_OK

class ManagerProductFragment : Fragment() {
    internal var updateUIHandler: Handler? = null

    internal var mProductID: String? = null
    internal var mGetProductTask: GetProductTask? = null

    internal var mImageView: ImageView? = null
    internal var mNameView: EditText? = null
    internal var mDescriptionView: EditText? = null
    internal var mImg1View: EditText? = null
    internal var mPriceView: EditText? = null
    internal var mDateView: EditText? = null
    internal var mQuantityView: EditText? = null
    internal var mLikesView: EditText? = null
    internal var mCalendar: Calendar? = null
    internal var mUploadingDialog: Dialog? = null

    //    String mImage;
    internal var mName: String? = null
    internal var mDescription: String? = null
    internal var mImg1: String? = null
    internal var mPrice: Double = 0.toDouble()
    internal var mDate: String? = null
    internal var mQuantity: String? = null
    internal var mLikes: String? = null
    internal var mImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_manager_product, container, false)

        createUpdateUiHandler()
        mImageView = rootView.findViewById<View>(R.id.image_view) as ImageView
        mNameView = rootView.findViewById<View>(R.id.name_edit_text) as EditText
        mDescriptionView = rootView.findViewById<View>(R.id.description_edit_text) as EditText
        mImg1View = rootView.findViewById<View>(R.id.img1_edit_text) as EditText
        mPriceView = rootView.findViewById<View>(R.id.price_edit_text) as EditText
        mDateView = rootView.findViewById<View>(R.id.date_edit_text) as EditText
        mQuantityView = rootView.findViewById<View>(R.id.quantity_edit_text) as EditText
        mLikesView = rootView.findViewById<View>(R.id.likes_edit_text) as EditText
        val saveButton = rootView.findViewById<View>(R.id.save_button) as Button

        mImageView!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_SINGLE_FILE)
        }

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

        saveButton.setOnClickListener {
            //                mUploadingDialog = ProgressDialog.show(getContext(),getString(R.string.title_saving),null,false);

            try {
                mName = mNameView!!.text.toString()
                mDescription = mDescriptionView!!.text.toString()
                mImg1 = mImg1View!!.text.toString()
                mPrice = java.lang.Double.valueOf(mPriceView!!.text.toString())
                mDate = mDateView!!.text.toString()
                mQuantity = mQuantityView!!.text.toString()
                mLikes = mLikesView!!.text.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Thread(Runnable {
                //creating new thread to handle Http Operations
                //                        String imagePath = getRealPathFromURI(mImageUri);
                var imagePath: String? = null
                if (mImg1!!.startsWith("file")) {
                    if (mImageUri != null) {
                        imagePath = Uri2Path.getPath(context!!, mImageUri!!)
                    }
                    if (!saveProduct(imagePath)) {
                        val message = Message()
                        message.what = MESSAGE_SHOW_ERROR_TOAST_THREAD
                        updateUIHandler!!.sendMessage(message)
                        return@Runnable
                    }
                    val message = Message()
                    message.what = MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD
                    updateUIHandler!!.sendMessage(message)
                }

                //                        saveProduct(mImageUri);

                val addProductTask = SaveProductTask(
                        mName!!, mDescription!!, mImg1!!, mPrice, mDate!!)
                addProductTask.execute()
            }).start()
        }

        val intent = (activity as ManagerProductActivity).intent
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
                            mImg1View!!.setText(mImg1)
                            mPriceView!!.setText(mPrice.toString())
                            mDateView!!.setText(mDate)
                            mQuantityView!!.setText(mQuantity)
                            mLikesView!!.setText(mLikes)
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

    internal fun updateLabel(date: Date) {
        val myFormat = G.DATE_FORMAT //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        mDateView!!.setText(sdf.format(date))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_SINGLE_FILE) {
                    mImageUri = data!!.data
                    // Get the path from the Uri
                    val path = getPathFromURI(mImageUri)
                    if (path != null) {
                        val f = File(path)
                        mImageUri = Uri.fromFile(f)
                    }
                    // Set the image in ImageView
                    mImageView!!.setImageURI(mImageUri)
                    mImg1View!!.setText(mImageUri!!.toString())
                }
            }
        } catch (e: Exception) {
            Log.e("FileSelectorActivity", "File select error", e)
        }

    }

    fun getPathFromURI(contentUri: Uri?): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context!!.contentResolver.query(contentUri!!, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor.close()
        return res
    }

    //    public int saveProduct(final Uri imageUri) {
    fun saveProduct(imageUri: String?): Boolean {
        if (imageUri != null) {

            try {

                var conn: HttpURLConnection? = null
                var dos: DataOutputStream? = null
                val lineEnd = "\r\n"
                val twoHyphens = "--"
                val boundary = "*****"
                var bytesRead: Int
                var bytesAvailable: Int
                var bufferSize: Int
                val buffer: ByteArray
                val maxBufferSize = 1 * 1024 * 1024
                val sourceFile = File(imageUri)

                if (sourceFile.isFile) {

                    try {
                        val upLoadServerUri = String.format("%s/save_product.php", G.SERVER_URL)

                        // open a URL connection to the Servlet
                        val fileInputStream = FileInputStream(
                                sourceFile)
                        val url = URL(upLoadServerUri)

                        // Open a HTTP connection to the URL
                        conn = url.openConnection() as HttpURLConnection
                        conn.doInput = true // Allow Inputs
                        conn.doOutput = true // Allow Outputs
                        conn.useCaches = false // Don't use a Cached Copy
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Connection", "Keep-Alive")
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data")
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=$boundary")
                        conn.setRequestProperty("uploaded_file", imageUri)

                        dos = DataOutputStream(conn.outputStream)

                        dos.writeBytes(twoHyphens + boundary + lineEnd)
                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                + imageUri + "\"" + lineEnd)

                        dos.writeBytes(lineEnd)

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available()

                        bufferSize = Math.min(bytesAvailable, maxBufferSize)
                        buffer = ByteArray(bufferSize)

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize)

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize)
                            bytesAvailable = fileInputStream.available()
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize)
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize)

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd)
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd)

                        // Responses from the server (code and message)
                        val serverResponseCode = conn.responseCode
                        val serverResponseMessage = conn
                                .responseMessage

                        if (serverResponseCode == 200) {

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);
                            val streamReader = InputStreamReader(conn.inputStream)
                            val reader = BufferedReader(streamReader)
                            val stringBuilder = StringBuilder()
                            var inputLine: String
                            inputLine = reader.use(BufferedReader::readText)
                            stringBuilder.append(inputLine)

                            reader.close()
                            streamReader.close()

                            var result = stringBuilder.toString()

                            val json = JSONObject(result)
                            result = json.getString("result")
                            if (result != "success") {
                                return false
                            }
                            mImg1 = json.getString("msg")
                            Log.e("save-api-result", result)
                        }

                        // close the streams //
                        fileInputStream.close()
                        dos.flush()
                        dos.close()

                        return true
                    } catch (e: Exception) {

                        // dialog.dismiss();
                        e.printStackTrace()

                    }

                    // dialog.dismiss();

                } // End else block


            } catch (ex: Exception) {
                // dialog.dismiss();

                ex.printStackTrace()
            }

            return false
        }

        return false
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

                    var myFormat = G.DATE_FORMAT2 //In which you need put here
                    var sdf = SimpleDateFormat(myFormat, Locale.US)
                    val date = sdf.parse(mDate)
                    mCalendar!!.set(date.year + 1900, date.month, date.date)

                    myFormat = G.DATE_FORMAT //In which you need put here
                    sdf = SimpleDateFormat(myFormat, Locale.US)

                    mDate = sdf.format(date)
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


    internal inner class SaveProductTask(var mName: String, var mDescription: String, var mImg1: String, var mPrice: Double, var mDate: String) : AsyncTask<String, Void, Boolean>() {
        var mMethod = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): Boolean? {
            var stringUrl = String.format("%s/save_product.php?" + "pid=%s&name=%s&description=%s&img1=%s&price=%f&date=%s",
                    G.SERVER_URL, mProductID, mName, mDescription, mImg1, mPrice, mDate)
            val url = HttpUrl.Builder()
                    .scheme(G.SERVER_SCHEME)
                    .host(G.SERVER_IP)
                    .addPathSegment("save_product.php")
                    .addQueryParameter("pid", mProductID)
                    .addQueryParameter("name", mName)
                    .addQueryParameter("description", mDescription)
                    .addQueryParameter("img1", mImg1)
                    .addQueryParameter("price", mPrice.toString())
                    .addQueryParameter("date", mDate)
                    .build()
            stringUrl = url.toString()
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
        fun newInstance(): ManagerProductFragment {
            return ManagerProductFragment()
        }
    }
}
