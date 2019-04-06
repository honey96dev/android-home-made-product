package com.honey96dev.homemadeproduct.p4manager;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ManagerProductListFragment extends Fragment {
    public final static String STORE_ID_KEY = "STORE_ID_KEY";
    Handler updateUIHandler = null;
    final static int MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1;
    final static int MESSAGE_SHOW_ADD_PRODUCT_TOAST_THREAD = 2;

    String mStoreID = null;
    ArrayList<ManagerProductListAdapter.ManagerProduct> mProducts = new ArrayList();
    ManagerProductListAdapter mProductAdapter = null;
    RecyclerView mProductsView = null;
    FloatingActionButton mAddStoreFab = null;
    GetProductsTask mGetProductsTask = null;

    AlertDialog mAddDialog = null;

    public ManagerProductListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ManagerProductListFragment newInstance() {
        ManagerProductListFragment fragment = new ManagerProductListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manager_store_list, container, false);

        createUpdateUiHandler();

        mProductAdapter = new ManagerProductListAdapter(getContext(), mProducts);

        mProductsView = (RecyclerView) rootView.findViewById(R.id.product_recycler_view);
        mProductsView.setHasFixedSize(true);
        mProductsView.setLayoutManager(new LinearLayoutManager(getContext()));
        mProductsView.setItemAnimator(new ScaleUpAndDownItemAnimator());
        mProductsView.setAdapter(mProductAdapter);

        mAddStoreFab = (FloatingActionButton) rootView.findViewById(R.id.add_store_fab);
        mAddStoreFab.setOnClickListener(new View.OnClickListener() {
            Calendar mCalendar;
            EditText mDateView;

            @Override
            public void onClick(View v) {
                AlertDialog.Builder addProductDialogBuilder = new AlertDialog.Builder(getContext());
                // Set title, icon, can not cancel properties.
                addProductDialogBuilder.setTitle(R.string.title_add_product);
                addProductDialogBuilder.setIcon(R.drawable.ic_fiber_new_accent_24dp);
                addProductDialogBuilder.setCancelable(true);

                // Init popup dialog view and it's ui controls.
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());

                // Inflate the popup dialog from a layout xml file.
                View dialogView = layoutInflater.inflate(R.layout.dialog_manager_add_product, null);

                // Get user input edittext and button ui controls in the popup dialog.
                final EditText nameView = (EditText) dialogView.findViewById(R.id.name_edit_text);
                final EditText descriptionView = (EditText) dialogView.findViewById(R.id.description_edit_text);
                final EditText img1View = (EditText) dialogView.findViewById(R.id.img1_edit_text);
                final EditText priceView = (EditText) dialogView.findViewById(R.id.price_edit_text);
                mDateView = (EditText) dialogView.findViewById(R.id.date_edit_text);
                Button cancelButton = dialogView.findViewById(R.id.cancel_button);
                Button saveButton = dialogView.findViewById(R.id.save_button);

                updateLabel(new Date());
                // Set the inflated layout view object to the AlertDialog builder.
                addProductDialogBuilder.setView(dialogView);
                addProductDialogBuilder.setCancelable(false);

                // Create AlertDialog and show.
                mAddDialog = addProductDialogBuilder.create();
                mAddDialog.show();

//                mDateView.readon

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

                // When user click the save user data button in the popup dialog.
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nameView.setError(null);
                        descriptionView.setError(null);
                        img1View.setError(null);
                        mDateView.setError(null);

                        String name = nameView.getText().toString();
                        String description = descriptionView.getText().toString();
                        String img1 = img1View.getText().toString();
                        double price = 0;
                        try {
                            price = Double.valueOf(priceView.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        String date = mDateView.getText().toString();

                        boolean cancel = false;
                        View focusView = null;

                        if (!isDateStringValid(date)) {
                            mDateView.setError(getString(R.string.error_field_invalid_date));
                            focusView = mDateView;
                            cancel = true;
                        }

                        if (TextUtils.isEmpty(img1)) {
                            img1View.setError(getString(R.string.error_field_required));
                            focusView = img1View;
                            cancel = true;
                        }

                        if (price <= 0) {
                            priceView.setError(getString(R.string.error_field_must_positive));
                            focusView = priceView;
                            cancel = true;
                        }

                        if (TextUtils.isEmpty(img1)) {
                            img1View.setError(getString(R.string.error_field_required));
                            focusView = img1View;
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
                            AddProductTask addProductTask = new AddProductTask(
                                    name, description, img1, price, date);
                            addProductTask.execute();
//                            mAddDialog.cancel();
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
            void updateLabel(final Date date) {
                String myFormat = G.DATE_FORMAT; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                mDateView.setText(sdf.format(date));
            }

            boolean isDateStringValid(String dateString) {
                DateFormat format = new SimpleDateFormat(G.DATE_FORMAT);
                format.setLenient(false);

                try {
                    format.parse(dateString);
                    return true;
                } catch (ParseException e) {
                    return false;
                }
            }
        });

        mStoreID = ((ManagerMainActivity) getActivity()).mStoreID;
        mGetProductsTask = new GetProductsTask(mStoreID);
        mGetProductsTask.execute();

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
                        case MESSAGE_SHOW_ADD_PRODUCT_TOAST_THREAD:
                            Toast.makeText(getContext(), R.string.message_store_added, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };
        }
    }


    class GetProductsTask extends AsyncTask<String, Void, String> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;
        String mStoreID = null;

        GetProductsTask(String storeID) {
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
            mProducts.clear();

            try {
                Log.e("products-api", responseText);
                JSONObject json = new JSONObject(responseText);
                JSONArray products = json.getJSONArray("products");
                JSONObject item;
                int cnt = products.length();
                for (int i = 0; i < cnt; i++) {
                    item = products.getJSONObject(i);
                    mProducts.add(new ManagerProductListAdapter.ManagerProduct(
                            item.getString("id"),
//                            item.getString("storeID"),
                            mStoreID,
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
                    ));
                }
//                products.getJSONObject()
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mProductAdapter.notifyDataSetChanged();
        }
    }


    class AddProductTask extends AsyncTask<String, Void, String> {
        String mMethod = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        String mName;
        String mDescription;
        String mImg1;
        double mPrice;
        String mDate;

        AddProductTask(String name, String description, String img1, double price, String date) {
            mName = name;
            mDescription = description;
            mImg1 = img1;
            mPrice = price;
            mDate = date;
        }

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = String.format("http://173.199.122.197/add_product.php?" +
                            "storeid=%s&name=%s&description=%s&img1=%s&price=%f&date=%s",
                    G.userInfo.StoreID, mName, mDescription, mImg1, mPrice, mDate);
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

                Log.e("add-api", result);

                JSONObject json = new JSONObject(result);
                result = json.getString("result");
                if (!result.equals("success")) {
                    return null;
                }

                //============reload store list=============
                stringUrl = String.format("http://173.199.122.197/get_stores.php?stid=%s", G.userInfo.StoreID);
                //Create a URL object holding our url
                myUrl = new URL(stringUrl);
                //Create a connection
                connection = (HttpURLConnection) myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(mMethod);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setRequestProperty("Accept", "application/json");

                connection.connect();

                streamReader = new InputStreamReader(connection.getInputStream());
                reader = new BufferedReader(streamReader);
                stringBuilder = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                result = stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                result = null;
            } catch (JSONException e) {
                e.printStackTrace();
                result = null;
            }
            mAddDialog.cancel();

            Message message = new Message();
            message.what = MESSAGE_SHOW_ADD_PRODUCT_TOAST_THREAD;
//            message.arg1 = 1;
            updateUIHandler.sendMessage(message);

            return result;
        }

        protected void onPostExecute(String responseText) {
            super.onPostExecute(responseText);
            if (responseText == null) {
                return;
            }
            mProducts.clear();

            try {
                Log.e("products-api", responseText);
                JSONObject json = new JSONObject(responseText);
                JSONArray products = json.getJSONArray("products");
                JSONObject item;
                int cnt = products.length();
                for (int i = 0; i < cnt; i++) {
                    item = products.getJSONObject(i);
                    mProducts.add(new ManagerProductListAdapter.ManagerProduct(
                            item.getString("id"),
//                            item.getString("storeID"),
                            mStoreID,
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
                    ));
                }
//                stores.getJSONObject()
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mProductAdapter.notifyDataSetChanged();
        }
    }
}
