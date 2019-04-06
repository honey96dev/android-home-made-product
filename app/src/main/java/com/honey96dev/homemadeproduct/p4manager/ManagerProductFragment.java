package com.honey96dev.homemadeproduct.p4manager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.honey96dev.homemadeproduct.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ManagerProductFragment extends Fragment {
    public final static String PRODUCT_ID_KEY = "PRODUCT_ID_KEY";
    Handler updateUIHandler = null;
    final static int MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD = 1;
    final static int MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD = 2;

    String mProductID = null;
    GetProductTask mGetProductTask = null;

    ImageView mImageView;
    EditText mNameView;
    EditText mDescriptionView;
    EditText mImg1View;
    EditText mPriceView;
    EditText mDateView;

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

        Intent intent = ((ManagerProductActivity) getActivity()).getIntent();
        mProductID = intent.getStringExtra(PRODUCT_ID_KEY);
        mGetProductTask = new GetProductTask(mProductID);
        mGetProductTask.execute();

        return rootView;
    }

    void createUpdateUiHandler()
    {
        if(updateUIHandler == null)
        {
            updateUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // Means the message is sent from child thread.
                    switch (msg.what) {
                        case MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD:

                            break;
                        case MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_product_saved, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };
        }
    }


    class GetProductTask extends AsyncTask<String, Void, String> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;
        String mStoreID = null;

        GetProductTask(String storeID) {
            super();
            this.mStoreID = storeID;
        }

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = String.format("http://173.199.122.197/get_stores.php?stid=%s", this.mStoreID);
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
                JSONObject item;
//                products.getJSONObject()
            } catch (JSONException e) {
                e.printStackTrace();
            }
//
//            Message message = new Message();
//            message.what = MESSAGE_SHOW_SAVE_PRODUCT_TOAST_THREAD;
//            updateUIHandler.sendMessage(message);
        }
    }
}
