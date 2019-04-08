package com.honey96dev.homemadeproduct.p4manager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.honey96dev.homemadeproduct.G;
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

public class ManagerStoreListFragment extends Fragment {
    Handler updateUIHandler = null;
    final static int MESSAGE_UPDATE_TEXT_CHILD_THREAD =1;
    final static int MESSAGE_SHOW_TOAST_THREAD =2;

    ArrayList<ManagerStoreListAdapter.ManagerStore> mProducts = new ArrayList();
    ManagerStoreListAdapter mStoresAdapter = null;
    RecyclerView mStoresView = null;
    FloatingActionButton mAddStoreFab = null;
    GetStoresTask mGetStoresTask = null;

    AlertDialog mAddDialog = null;

    public ManagerStoreListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ManagerStoreListFragment newInstance() {
        ManagerStoreListFragment fragment = new ManagerStoreListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manager_store_list, container, false);

        mStoresAdapter = new ManagerStoreListAdapter(getContext(), mProducts);

        mStoresView = (RecyclerView) rootView.findViewById(R.id.product_recycler_view);
        mStoresView.setHasFixedSize(true);
        mStoresView.setLayoutManager(new LinearLayoutManager(getContext()));
        mStoresView.setItemAnimator(new ScaleUpAndDownItemAnimator());
        mStoresView.setAdapter(mStoresAdapter);

        mAddStoreFab = (FloatingActionButton) rootView.findViewById(R.id.add_store_fab);
        mAddStoreFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder addStoreDialogBuilder = new AlertDialog.Builder(getContext());
                // Set title, icon, can not cancel properties.
                addStoreDialogBuilder.setTitle(R.string.title_add_store);
                addStoreDialogBuilder.setIcon(R.drawable.ic_fiber_new_accent_24dp);
                addStoreDialogBuilder.setCancelable(true);

                // Init popup dialog view and it's ui controls.
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());

                // Inflate the popup dialog from a layout xml file.
                View dialogView = layoutInflater.inflate(R.layout.dialog_manager_add_store, null);

                // Get user input edittext and button ui controls in the popup dialog.
                final EditText nameView = (EditText) dialogView.findViewById(R.id.name_edit_text);
                final EditText descriptionView = (EditText) dialogView.findViewById(R.id.description_edit_text);
                final EditText iconView = (EditText) dialogView.findViewById(R.id.icon_edit_text);
                Button cancelButton = dialogView.findViewById(R.id.cancel_button);
                Button saveButton = dialogView.findViewById(R.id.save_button);

                // Set the inflated layout view object to the AlertDialog builder.
                addStoreDialogBuilder.setView(dialogView);
                addStoreDialogBuilder.setCancelable(false);

                // Create AlertDialog and show.
                mAddDialog = addStoreDialogBuilder.create();
                mAddDialog.show();

                // When user click the save user data button in the popup dialog.
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nameView.setError(null);
                        descriptionView.setError(null);
                        iconView.setError(null);

                        String name = nameView.getText().toString();
                        String description = descriptionView.getText().toString();
                        String icon = iconView.getText().toString();

                        boolean cancel = false;
                        View focusView = null;

                        if (TextUtils.isEmpty(icon)) {
                            iconView.setError(getString(R.string.error_field_required));
                            focusView = iconView;
                            cancel = true;
                        }

                        if (TextUtils.isEmpty(description)) {
                            descriptionView.setError(getString(R.string.error_field_required));
                            focusView = descriptionView;
                            cancel = true;
                        }

                        if (TextUtils.isEmpty(name)) {
                            nameView.setError(getString(R.string.error_field_required));
                            focusView = nameView;
                            cancel = true;
                        }

                        if (cancel) {
                            // There was an error; don't attempt login and focus the first
                            // form field with an error.
                            focusView.requestFocus();
                        } else {
                            AddStoreTask addStoreTask = new AddStoreTask(name, description, icon);
                            addStoreTask.execute();
                            mAddDialog.cancel();
                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAddDialog.cancel();
                    }
                });
            }
        });

        mGetStoresTask = new GetStoresTask();
        mGetStoresTask.execute();

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

    class GetStoresTask extends AsyncTask<String, Void, String> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params){
            String stringUrl = String.format("%s/get_stores.php?stores", G.SERVER_URL);
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
        protected void onPostExecute(String responseText){
            super.onPostExecute(responseText);

            if (responseText == null) {
                return;
            }
            mProducts.clear();

            try {
                Log.e("products-api", responseText);
                JSONObject json = new JSONObject(responseText);
                JSONArray stores = json.getJSONArray("stores");
                JSONObject item;
                int cnt = stores.length();
                for (int i = 0; i < cnt; i++) {
                    item = stores.getJSONObject(i);
                    mProducts.add(new ManagerStoreListAdapter.ManagerStore(
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

            mStoresAdapter.notifyDataSetChanged();
        }
    }


    class AddStoreTask extends AsyncTask<String, Void, String> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        String mName;
        String mDescription;
        String mIcon;
        AddStoreTask(String name, String description, String icon) {
            mName = name;
            mDescription = description;
            mIcon = icon;
        }

        @Override
        protected String doInBackground(String... params){
            String stringUrl = String.format("%s/add_store.php?" +
                    "userid=%s&name=%s&description=%s&icon=%s",
                    G.SERVER_URL, G.userInfo.UserID, mName, mDescription, mIcon);
            String result;
            String inputLine;

            try {
                //============add=============
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

                Log.e("add-api", result);

                JSONObject json = new JSONObject(result);
                result = json.getString("result");
                if (!result.equals("success")) {
                    return null;
                }

                //============reload store list=============
                stringUrl = String.format("%s/get_stores.php?stores", G.SERVER_URL);
                //Create a URL object holding our url
                myUrl = new URL(stringUrl);
                //Create a connection
                connection =(HttpURLConnection)myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(mMethod);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setRequestProperty("Accept","application/json");

                connection.connect();

                streamReader = new InputStreamReader(connection.getInputStream());
                reader = new BufferedReader(streamReader);
                stringBuilder = new StringBuilder();
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
            } catch (JSONException e) {
                e.printStackTrace();
                result = null;
            }
            mAddDialog.cancel();
            Toast.makeText(getContext(), R.string.message_store_added, Toast.LENGTH_LONG).show();
            return result;
        }
        protected void onPostExecute(String responseText){
            super.onPostExecute(responseText);
            if (responseText == null) {
                return;
            }
            mProducts.clear();

            try {
                Log.e("products-api", responseText);
                JSONObject json = new JSONObject(responseText);
                JSONArray stores = json.getJSONArray("stores");
                JSONObject item;
                int cnt = stores.length();
                for (int i = 0; i < cnt; i++) {
                    item = stores.getJSONObject(i);
                    mProducts.add(new ManagerStoreListAdapter.ManagerStore(
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
            mStoresAdapter.notifyDataSetChanged();
        }
    }
}