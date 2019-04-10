package com.honey96dev.homemadeproduct.p4manager

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast

import com.honey96dev.homemadeproduct.R

class ManagerOrderListFragment : Fragment() {
    internal var txt1: EditText? = null
    internal var txt2: EditText? = null
    internal var txt3: EditText? = null
    internal var txt4: EditText? = null
    internal var txt5: EditText? = null

    internal var txt1Text = ""
    internal var txt2Text = ""
    internal var txt3Text = ""
    internal var txt4Text = ""
    internal var txt5Text = ""

    private var updateUIHandler: Handler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //        createUpdateUiHandler();
        //        txt1 = (EditText) rootView.findViewById(R.id.txt1);
        //        txt2 = (EditText) rootView.findViewById(R.id.txt2);
        //        txt3 = (EditText) rootView.findViewById(R.id.txt3);
        //        txt4 = (EditText) rootView.findViewById(R.id.txt4);
        //        txt5 = (EditText) rootView.findViewById(R.id.txt5);
        //
        //        Message message = new Message();
        //        message.what = MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD;
        //        updateUIHandler.sendMessage(message);
        //
        //        Button btn = (Button) rootView.findViewById(R.id.btn);
        //        btn.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                Thread thread = new Thread() {
        //                    @Override
        //                    public void run() {
        //                        if (G.SERVER_IP == "" || G.SERVER_PORT == "") {
        //                            Message message = new Message();
        //                            message.what = MESSAGE_SHOW_TOAST_THREAD;
        //                            updateUIHandler.sendMessage(message);
        //                            return;
        //                        }
        //                        String myUrl = String.format("http://%s:%s/test.json", G.SERVER_IP, G.SERVER_PORT);
        //                        //String to place our result in
        //                        String result;
        //                        //Instantiate new instance of our class
        //                        HttpRequest request = new HttpRequest();
        //                        request.setMethod("GET");
        //                        //Perform the doInBackground method, passing in our url
        //                        try {
        //                            result = request.execute(myUrl).get();
        //                            if (result == null) {
        //                                return;
        //                            }
        //
        //                            JSONObject json = new JSONObject(result);
        //                            JSONObject app = json.getJSONObject("AppName");
        //                            JSONObject stats = app.getJSONObject("stats");
        //
        //                            txt1Text = stats.getString("state1");
        //                            txt2Text = stats.getString("state2");
        //                            txt3Text = stats.getString("state3");
        //                            txt4Text = stats.getString("state4");
        //                            txt5Text = stats.getString("state5");
        //
        //                            Message message = new Message();
        //                            message.what = MESSAGE_UPDATE_DISPLAY_PRODUCT_INFO_THREAD;
        //                            updateUIHandler.sendMessage(message);
        //                        } catch (ExecutionException e) {
        //                            e.printStackTrace();
        //                        } catch (InterruptedException e) {
        //                            e.printStackTrace();
        //                        } catch (JSONException e) {
        //                            e.printStackTrace();
        //                        }
        //                    }
        //                };
        //                thread.start();
        //            }
        //        });

        return inflater.inflate(R.layout.fragment_manager_order_list, container, false)
    }

    private fun createUpdateUiHandler() {
        if (updateUIHandler == null) {
            updateUIHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    // Means the message is sent from child thread.
                    when (msg.what) {
                        MESSAGE_UPDATE_TEXT_CHILD_THREAD -> {
                            txt1!!.setText(txt1Text)
                            txt2!!.setText(txt2Text)
                            txt3!!.setText(txt3Text)
                            txt4!!.setText(txt4Text)
                            txt5!!.setText(txt5Text)
                        }
                        MESSAGE_SHOW_TOAST_THREAD -> Toast.makeText(context, "Select Server", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        private val MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1
        private val MESSAGE_SHOW_TOAST_THREAD = 2

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(): ManagerOrderListFragment {
            return ManagerOrderListFragment()
        }
    }
}