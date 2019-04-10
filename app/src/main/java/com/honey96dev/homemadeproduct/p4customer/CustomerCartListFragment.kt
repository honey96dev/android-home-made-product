package com.honey96dev.homemadeproduct.p4customer

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

class CustomerCartListFragment : Fragment() {
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

        return inflater.inflate(R.layout.fragment_customer_cart_list, container, false)
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
        fun newInstance(): CustomerCartListFragment {
            return CustomerCartListFragment()
        }
    }
}