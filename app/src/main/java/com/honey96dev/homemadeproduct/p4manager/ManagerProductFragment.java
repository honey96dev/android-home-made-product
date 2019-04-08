package com.honey96dev.homemadeproduct.p4manager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.honey96dev.homemadeproduct.G;
import com.honey96dev.homemadeproduct.R;
import com.honey96dev.homemadeproduct.Uri2Path;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.HttpUrl;

import static android.app.Activity.RESULT_OK;

public class ManagerProductFragment extends Fragment {
    public final static String PRODUCT_ID_KEY = "PRODUCT_ID_KEY";
    Handler updateUIHandler = null;
    final static int MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD = 1;
    final static int MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD = 2;
    final static int MESSAGE_SHOW_INVALID_FILE_TOAST_THREAD = 3;
    final static int MESSAGE_SHOW_FILE_NOT_FOUND_TOAST_THREAD = 4;
    final static int MESSAGE_SHOW_SERVER_ERROR_TOAST_THREAD = 5;
    final static int MESSAGE_SHOW_IO_ERROR_TOAST_THREAD = 6;
    final static int MESSAGE_SHOW_ERROR_TOAST_THREAD = 7;

    final static int REQUEST_GET_SINGLE_FILE = 1;

    String mProductID = null;
    GetProductTask mGetProductTask = null;

    ImageView mImageView;
    EditText mNameView;
    EditText mDescriptionView;
    EditText mImg1View;
    EditText mPriceView;
    EditText mDateView;
    EditText mQuantityView;
    EditText mLikesView;
    Calendar mCalendar;
    Dialog mUploadingDialog;

    //    String mImage;
    String mName;
    String mDescription;
    String mImg1;
    double mPrice;
    String mDate;
    String mQuantity;
    String mLikes;
    Uri mImageUri;

    public ManagerProductFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ManagerProductFragment newInstance() {
        ManagerProductFragment fragment = new ManagerProductFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manager_product, container, false);

        createUpdateUiHandler();
        mImageView = (ImageView) rootView.findViewById(R.id.image_view);
        mNameView = (EditText) rootView.findViewById(R.id.name_edit_text);
        mDescriptionView = (EditText) rootView.findViewById(R.id.description_edit_text);
        mImg1View = (EditText) rootView.findViewById(R.id.img1_edit_text);
        mPriceView = (EditText) rootView.findViewById(R.id.price_edit_text);
        mDateView = (EditText) rootView.findViewById(R.id.date_edit_text);
        mQuantityView = (EditText) rootView.findViewById(R.id.quantity_edit_text);
        mLikesView = (EditText) rootView.findViewById(R.id.likes_edit_text);
        Button saveButton = (Button) rootView.findViewById(R.id.save_button);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_SINGLE_FILE);
            }
        });

        mCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(mCalendar.getTime());
            }

        };

        mDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), date, mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mUploadingDialog = ProgressDialog.show(getContext(),getString(R.string.title_saving),null,false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //creating new thread to handle Http Operations
//                        String imagePath = getRealPathFromURI(mImageUri);
                        String imagePath = null;
                        if (mImageUri != null) {
                            imagePath = Uri2Path.getPath(getContext(), mImageUri);
                        }
                        if (!saveProduct(imagePath)) {
                            Message message = new Message();
                            message.what = MESSAGE_SHOW_ERROR_TOAST_THREAD;
                            updateUIHandler.sendMessage(message);
                            return;
                        }

                        Message message = new Message();
                        message.what = MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD;
                        updateUIHandler.sendMessage(message);
//                        saveProduct(mImageUri);

                        SaveProductTask addProductTask = new SaveProductTask(
                                mName, mDescription, mImg1, mPrice, mDate);
                        addProductTask.execute();
                    }
                }).start();
            }
        });

        Intent intent = ((ManagerProductActivity) getActivity()).getIntent();
        mProductID = intent.getStringExtra(PRODUCT_ID_KEY);
        mGetProductTask = new GetProductTask(mProductID);
        mGetProductTask.execute();

        return rootView;
    }

    void createUpdateUiHandler() {
        if (updateUIHandler == null) {
            updateUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // Means the message is sent from child thread.
                    switch (msg.what) {
                        case MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD:
                            try {
                                Picasso.get().load(mImg1)
                                        .into(mImageView);
                                if (!mImg1.isEmpty()) {
                                    mImageView.setBackground(null);
                                }
                            } catch (Exception e) {
                                mImageView.setBackground(getContext().getDrawable(R.drawable.image_border));
                                e.printStackTrace();
                            }
                            mNameView.setText(mName);
                            mDescriptionView.setText(mDescription);
                            mImg1View.setText(mImg1);
                            mPriceView.setText(String.valueOf(mPrice));
                            mDateView.setText(mDate);
                            mQuantityView.setText(mQuantity);
                            mLikesView.setText(mLikes);
                            break;
                        case MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_product_saved, Toast.LENGTH_LONG).show();
                            break;
                        case MESSAGE_SHOW_INVALID_FILE_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_invalid_file, Toast.LENGTH_LONG).show();
                            break;
                        case MESSAGE_SHOW_FILE_NOT_FOUND_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_file_not_found, Toast.LENGTH_LONG).show();
                            break;
                        case MESSAGE_SHOW_SERVER_ERROR_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_server_error, Toast.LENGTH_LONG).show();
                            break;
                        case MESSAGE_SHOW_IO_ERROR_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_io_error, Toast.LENGTH_LONG).show();
                            break;
                        case MESSAGE_SHOW_ERROR_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_error, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };
        }
    }

    void updateLabel(final Date date) {
        String myFormat = G.DATE_FORMAT; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mDateView.setText(sdf.format(date));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_SINGLE_FILE) {
                    mImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(mImageUri);
                    if (path != null) {
                        File f = new File(path);
                        mImageUri = Uri.fromFile(f);
                    }
                    // Set the image in ImageView
                    mImageView.setImageURI(mImageUri);
                    mImg1View.setText(mImageUri.toString());
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    //    public int saveProduct(final Uri imageUri) {
    public boolean saveProduct(final String imageUri) {
        if (imageUri != null) {

            try {
                String sourceFileUri = imageUri;

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);

                if (sourceFile.isFile()) {

                    try {
                        String upLoadServerUri = String.format("%s/save_product.php", G.SERVER_URL);

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(upLoadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();

                        if (serverResponseCode == 200) {

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);
                            InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());
                            BufferedReader reader = new BufferedReader(streamReader);
                            StringBuilder stringBuilder = new StringBuilder();
                            String inputLine;
                            while ((inputLine = reader.readLine()) != null) {
                                stringBuilder.append(inputLine);
                            }

                            reader.close();
                            streamReader.close();

                            String result = stringBuilder.toString();

                            JSONObject json = new JSONObject(result);
                            result = json.getString("result");
                            if (!result.equals("success")) {
                                return false;
                            }
                            mImg1 = json.getString("msg");
                            Log.e("save-api-result", result);
                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                        return true;
                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();

                    }
                    // dialog.dismiss();

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
            }
            return false;
        }

        return false;
    }

    class GetProductTask extends AsyncTask<String, Void, String> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;
        String mStoreID = null;

        GetProductTask(String productID) {
            super();
            this.mStoreID = productID;
        }

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = String.format("%s/get_stores.php?pdid=%s", G.SERVER_URL, this.mStoreID);
            Log.e("product-api", stringUrl);
            String result;
            String inputLine;

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(mMethod);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setRequestProperty("Accept", "application/json");

                connection.connect();

                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                result = stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                result = null;
            }
            return result;
        }

        protected void onPostExecute(String responseText) {
            super.onPostExecute(responseText);

            if (responseText == null) {
                return;
            }

            try {
                Log.e("products-api", responseText);
                JSONObject json = new JSONObject(responseText);
                JSONArray products = json.getJSONArray("products");
                if (products.length() > 0) {
                    JSONObject item = products.getJSONObject(0);
                    mImg1 = item.getString("img1");
                    mName = item.getString("name");
                    mDescription = item.getString("description");
                    mPrice = item.getDouble("price");
                    mDate = item.getString("date");
                    mQuantity = item.getString("quantity");
                    mLikes = item.getString("quantity");

                    String myFormat = G.DATE_FORMAT2; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    Date date = sdf.parse(mDate);
                    mCalendar.set(date.getYear() + 1900, date.getMonth(), date.getDate());

                    myFormat = G.DATE_FORMAT; //In which you need put here
                    sdf = new SimpleDateFormat(myFormat, Locale.US);

                    mDate = sdf.format(date);
                }
//                products.getJSONObject()
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
//
            Message message = new Message();
            message.what = MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD;
            updateUIHandler.sendMessage(message);
        }
    }


    class SaveProductTask extends AsyncTask<String, Void, Boolean> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        String mName;
        String mDescription;
        String mImg1;
        double mPrice;
        String mDate;

        SaveProductTask(String name, String description, String img1, double price, String date) {
            mName = name;
            mDescription = description;
            mImg1 = img1;
            mPrice = price;
            mDate = date;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String stringUrl = String.format("%s/save_product.php?" +
                            "pid=%s&name=%s&description=%s&img1=%s&price=%f&date=%s",
                    G.SERVER_URL, mProductID, mName, mDescription, mImg1, mPrice, mDate);
            HttpUrl url = new HttpUrl.Builder()
                    .scheme(G.SERVER_SCHEME)
                    .host(G.SERVER_IP)
                    .addPathSegment("save_product.php")
                    .addQueryParameter("pid", mProductID)
                    .addQueryParameter("name", mName)
                    .addQueryParameter("description", mDescription)
                    .addQueryParameter("img1", mImg1)
                    .addQueryParameter("price", String.valueOf(mPrice))
                    .addQueryParameter("date", mDate)
                    .build();
            stringUrl = url.toString();
            String result;
            String inputLine;

            try {
                //============add=============
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(mMethod);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
//                connection.setRequestProperty("Accept", "application/json");

                connection.connect();

                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                result = stringBuilder.toString();

                Log.e("add-api", result);

                JSONObject json = new JSONObject(result);
                result = json.getString("result");
                if (!result.equals("success")) {
                    return false;
                }

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                result = null;
            } catch (JSONException e) {
                e.printStackTrace();
                result = null;
            }

            return false;
        }

        protected void onPostExecute(Boolean responseText) {
            super.onPostExecute(responseText);
            if (responseText == true) {
                Message message = new Message();
                message.what = MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD;
//            message.arg1 = 1;
                updateUIHandler.sendMessage(message);
            }
        }
    }
}
