package com.honey96dev.homemadeproduct.user

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.tools.G
import okhttp3.HttpUrl
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * A login screen that offers login via email/password.
 */
class SignupActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    internal var mSignupTask: UserSignupTask? = null

    // UI references.
    internal var mUsernameView: EditText? = null
    internal var mFirstNameView: EditText? = null
    internal var mLastnameView: EditText? = null
    internal var mEmailView: EditText? = null
    internal var mPasswordView: EditText? = null
    internal var mPassword2View: EditText? = null
    internal var mCityView: EditText? = null
    internal var mPhoneView: EditText? = null
    internal var mUserTypeView: Spinner? = null
    internal var mSignupButton: Button? = null
    internal var mProgressView: View? = null
    internal var mLoginFormView: View? = null

    internal var mUserType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        // Set up the login form.
        mUsernameView = findViewById<View>(R.id.username_edit_text) as EditText

        mFirstNameView = findViewById<View>(R.id.firstname_edit_text) as EditText

        mLastnameView = findViewById<View>(R.id.lastname_edit_text) as EditText

        mEmailView = findViewById<View>(R.id.email_edit_text) as EditText

        mPasswordView = findViewById<View>(R.id.password_edit_text) as EditText

        mPassword2View = findViewById<View>(R.id.password2_edit_text) as EditText

        mCityView = findViewById<View>(R.id.city_edit_text) as EditText

        mPhoneView = findViewById<View>(R.id.phone_edit_text) as EditText

        mUserTypeView = findViewById<View>(R.id.usertype_spinner) as Spinner
        mUserTypeView!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mUserType = parent.getItemAtPosition(position).toString().toLowerCase()
                if (mUserType == "store manager") {
                    mUserType = "productive_family"
                }
                Log.e("spinner", mUserType)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                mUserType = ""
            }
        }

        mSignupButton = findViewById<View>(R.id.sign_up_button) as Button

        mLoginFormView = findViewById(R.id.signup_form)
        mProgressView = findViewById(R.id.signup_progress)

        mUserType = ""
    }

    fun onSignUpButtonClicked(v: View) {
        attemptLogin()
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    internal fun attemptLogin() {
        if (mSignupTask != null) {
            return
        }

        // Reset errors.
        mUsernameView!!.error = null
        mFirstNameView!!.error = null
        mLastnameView!!.error = null
        mEmailView!!.error = null
        mPasswordView!!.error = null
        mPassword2View!!.error = null
        mCityView!!.error = null
        mPhoneView!!.error = null

        // Store values at the time of the login attempt.
        val username = mUsernameView!!.text.toString()
        val firstname = mFirstNameView!!.text.toString()
        val lastname = mLastnameView!!.text.toString()
        val email = mEmailView!!.text.toString()
        val password = mPasswordView!!.text.toString()
        val password2 = mPassword2View!!.text.toString()
        val city = mCityView!!.text.toString()
        val phone = mPhoneView!!.text.toString()
        //        String userType = mUserTypeView.get

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(mUserType)) {
            mSignupButton!!.error = getString(R.string.error_user_type_required)
            focusView = mUserTypeView
            cancel = true
        }

        if (TextUtils.isEmpty(phone)) {
            mPhoneView!!.error = getString(R.string.error_field_required)
            focusView = mPhoneView
            cancel = true
        } else if (!isMobileValid(phone)) {
            mPhoneView!!.error = getString(R.string.error_invalid_phone)
            focusView = mPhoneView
            cancel = true
        }

        if (TextUtils.isEmpty(city)) {
            mCityView!!.error = getString(R.string.error_field_required)
            focusView = mCityView
            cancel = true
        }

        if (TextUtils.isEmpty(password2)) {
            mPassword2View!!.error = getString(R.string.error_field_required)
            focusView = mPassword2View
            cancel = true
        } else if (password2 != password) {
            mPassword2View!!.error = getString(R.string.error_password2_not_match)
            focusView = mPassword2View
            cancel = true
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView!!.error = getString(R.string.error_field_required)
            focusView = mPasswordView
            cancel = true
        } else if (!isPasswordValid(password)) {
            mPasswordView!!.error = getString(R.string.error_invalid_password)
            focusView = mPasswordView
            cancel = true
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView!!.error = getString(R.string.error_field_required)
            focusView = mEmailView
            cancel = true
        } else if (!isEmailValid(email)) {
            mEmailView!!.error = getString(R.string.error_invalid_email)
            focusView = mEmailView
            cancel = true
        }

        if (TextUtils.isEmpty(lastname)) {
            mLastnameView!!.error = getString(R.string.error_field_required)
            focusView = mLastnameView
            cancel = true
        }

        if (TextUtils.isEmpty(firstname)) {
            mFirstNameView!!.error = getString(R.string.error_field_required)
            focusView = mFirstNameView
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView!!.error = getString(R.string.error_field_required)
            focusView = mUsernameView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mSignupTask = UserSignupTask(username, firstname, lastname, email,
                    password, city, phone, mUserType!!)
            mSignupTask!!.execute()
        }
    }

    internal fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    internal fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    internal fun isMobileValid(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    internal fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
            mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserSignupTask internal constructor(internal val mUsername: String, internal val mFirstName: String, internal val mLastName: String, internal val mEmail: String,
                                                    internal val mPassword: String, internal val mCity: String, internal val mPhone: String, internal val mUserType: String) : AsyncTask<String, Void, String>() {
        var mSuccess: Boolean = false
        internal var METHOD_GET = "GET"
        internal val READ_TIMEOUT = 15000
        internal val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): String? {
            val url = HttpUrl.Builder()
                    .scheme(G.SERVER_SCHEME)
                    .host(G.SERVER_IP)
                    .addPathSegment("register.php")
                    .addQueryParameter("username", mUsername)
                    .addQueryParameter("firstName", mFirstName)
                    .addQueryParameter("lastName", mLastName)
                    .addQueryParameter("email", mEmail)
                    .addQueryParameter("password", mPassword)
                    .addQueryParameter("city", mCity)
                    .addQueryParameter("phone", mPhone)
                    .addQueryParameter("userType", mUserType)
                    .build()
            val stringUrl = url.toString()
            //            String stringUrl = String.format("%s/signup.php?username=%s&password=%s", G.SERVER_URL, mUsername, mPassword);
            var result: String?
            var inputLine: String

            try {
                //Create a URL object holding our url
                val myUrl = URL(stringUrl)
                //Create a connection
                val connection = myUrl.openConnection() as HttpURLConnection
                //Set methods and timeouts
                connection.requestMethod = METHOD_GET
                connection.readTimeout = READ_TIMEOUT
                connection.connectTimeout = CONNECTION_TIMEOUT
                //            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                //                connection.setRequestProperty("Accept", "application/json");
                connection.doOutput = true
                connection.doInput = true

                Log.e("url", connection.url.toString())
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
            showProgress(false)

            mSignupTask = null

            if (responseText == null) {
                Snackbar.make(mSignupButton!!, R.string.error_invalid_credential, Snackbar.LENGTH_SHORT).show()
                return
            }
            showProgress(false)
            Log.e("signup-result", responseText)

            try {
                val json = JSONObject(responseText)
                val result = json.getString("result")
                if (result == "success") {
                    mSuccess = true

                    val snackbar = Snackbar.make(mSignupButton!!, json.getString("msg"), Snackbar.LENGTH_LONG)
                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar?, event: Int) {
                            finish()
                        }
                    })
                    snackbar.show()
                } else {
                    mSuccess = false
                    showProgress(false)
                    Snackbar.make(mSignupButton!!, json.getString("msg"), Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onCancelled() {
            mSignupTask = null
            mSuccess = false
            showProgress(false)
        }
    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        internal val REQUEST_READ_CONTACTS = 0

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        internal val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }
}