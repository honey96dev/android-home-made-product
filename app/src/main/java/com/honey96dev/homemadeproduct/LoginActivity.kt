package com.honey96dev.homemadeproduct

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
class LoginActivity : AppCompatActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    internal var mAuthTask: UserLoginTask? = null

    // UI references.
    internal var mUsernameView: EditText? = null
    internal var mPasswordView: EditText? = null
    internal var mUsernameSignInButton: Button? = null
    internal var mProgressView: View? = null
    internal var mLoginFormView: View? = null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        mUsernameView = findViewById<View>(R.id.username) as AutoCompleteTextView

        mPasswordView = findViewById<View>(R.id.password) as EditText
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        mUsernameSignInButton = findViewById<View>(R.id.username_sign_in_button) as Button

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)

        mUsernameView!!.setText("usertest")
        mPasswordView!!.setText("123456")
    }

    fun onUsernameSignInButtonClicked(v: View) {
        attemptLogin()
    }

    fun onForgetPasswordButtonClicked(v: View) {

    }

    fun onCreateAccountButtonClicked(v: View) {

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    internal fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        mUsernameView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val username = mUsernameView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

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
            mAuthTask = UserLoginTask(username, password)
            mAuthTask!!.execute()
        }
    }

    internal fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
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
    inner class UserLoginTask internal constructor(internal val mUsername: String, internal val mPassword: String) : AsyncTask<String, Void, String>() {
        internal var METHOD_GET = "GET"
        internal val READ_TIMEOUT = 15000
        internal val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): String? {
            val stringUrl = java.lang.String.format("http://173.199.122.197/login.php?username=%s&password=%s", mUsername, mPassword)
            var result: String?
            var inputLine: String
            //        try {
            //            Thread.sleep(2000);
            //        } catch (InterruptedException e) {
            //            e.printStackTrace();
            //        }
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
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true

                Log.e("url", connection.url.toString())
                connection.connect()

                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                inputLine = reader.use(BufferedReader::readText)
//                while (inputLine != null) {
//                    stringBuilder.append(inputLine)
//                    inputLine = reader.readLine()
//                }

                reader.close()
                streamReader.close()

//                result = stringBuilder.toString()
                result = inputLine
            } catch (e: IOException) {
                e.printStackTrace()
                result = null
            }

            return result
        }

        override fun onPostExecute(responseText: String) {
            mAuthTask = null
            showProgress(false)
            Log.e("login-result", responseText)

            try {
                val json = JSONObject(responseText)
                val result = json.getString("result")
                if (result == "success") {
                    val user = json.getJSONObject("user")
                    val type = user.getString("Type").toLowerCase()
                    if (type != "productive_family" && type != "client") {
                        Snackbar.make(mUsernameSignInButton!!, R.string.error_invalid_user_type, Snackbar.LENGTH_LONG).show()
                        return
                    }
                    G.userInfo.UserID = user.getString("UserID")
                    G.userInfo.Username = user.getString("Username")
                    G.userInfo.FirstName = user.getString("FirstName")
                    G.userInfo.LastName = user.getString("LastName")
                    G.userInfo.Email = user.getString("Email")
                    G.userInfo.Password = user.getString("Password")
                    G.userInfo.City = user.getString("City")
                    G.userInfo.Phone = user.getString("Phone")
                    G.userInfo.Type = user.getString("Type")

                    val intent = Intent(this@LoginActivity, CustomerProductListActivity::class.java)
                    startActivity(intent)
                    mPasswordView!!.setText("")
                } else {
                    Snackbar.make(mUsernameSignInButton!!, json.getString("msg"), Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }
}

