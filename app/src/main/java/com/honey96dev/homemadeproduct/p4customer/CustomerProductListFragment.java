package com.honey96dev.homemadeproduct.p4customer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honey96dev.homemadeproduct.R;
import com.honey96dev.homemadeproduct.tools.ScaleUpAndDownItemAnimator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CustomerProductListFragment extends Fragment {
    Handler updateUIHandler = null;
    final static int MESSAGE_UPDATE_TEXT_CHILD_THREAD =1;
    final static int MESSAGE_SHOW_TOAST_THREAD =2;

    ArrayList<CustomerProductListAdapter.CustomerProduct> mProducts = new ArrayList<CustomerProductListAdapter.CustomerProduct>();
    CustomerProductListAdapter mProductsAdapter;
    RecyclerView mProductsView;
    CustomerProductsTask mCustomerProductsTask;

    public CustomerProductListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CustomerProductListFragment newInstance() {
        CustomerProductListFragment fragment = new CustomerProductListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_customer_product_list, container, false);

        mProductsAdapter = new CustomerProductListAdapter(getContext(), mProducts);

        mProductsView = (RecyclerView) rootView.findViewById(R.id.product_recycler_view);
        mProductsView.setHasFixedSize(true);
        mProductsView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProductsView.setItemAnimator(new ScaleUpAndDownItemAnimator());
        mProductsView.setAdapter(mProductsAdapter);



        mCustomerProductsTask = new CustomerProductsTask();
        mCustomerProductsTask.execute();

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
                        case MESSAGE_UPDATE_TEXT_CHILD_THREAD:
                            break;
                        case MESSAGE_SHOW_TOAST_THREAD:
                            break;
                    }
                }
            };
        }
    }

    class CustomerProductsTask extends AsyncTask<String, Void, String> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params){
            String stringUrl = String.format("http://173.199.122.197/get_stores.php?stores");
            String result;
            String inputLine;

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection)myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(mMethod);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setRequestProperty("Accept","application/json");

                connection.connect();

                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                result = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                result = null;
            }
            return result;
        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            mProducts.clear();

            try {
                Log.e("products-api", result);
                JSONObject json = new JSONObject(result);
                JSONArray stores = json.getJSONArray("stores");
                JSONObject item;
                int cnt = stores.length();
                for (int i = 0; i < cnt; i++) {
                    item = stores.getJSONObject(i);
                    mProducts.add(new CustomerProductListAdapter.CustomerProduct(
                            item.getString("id"),
                            item.getString("name"),
                            item.getString("description"),
                            item.getString("icon"),
                            item.getString("likes")
                    ));
                }
//                stores.getJSONObject()
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mProductsAdapter.notifyDataSetChanged();
        }
    }
}