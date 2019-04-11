package com.honey96dev.homemadeproduct.user

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.honey96dev.homemadeproduct.tools.G
import com.honey96dev.homemadeproduct.R
import com.honey96dev.homemadeproduct.p4customer.CustomerMainActivity
import com.honey96dev.homemadeproduct.p4manager.ManagerMainActivity
import com.honey96dev.homemadeproduct.p4manager.ManagerProductListFragment

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        mUsernameView = findViewById(R.id.username_edit_text)

        mPasswordView = findViewById(R.id.password_edit_text)
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        mUsernameSignInButton = findViewById<View>(R.id.username_sign_in_button) as Button

        mLoginFormView = findViewById(R.id.signup_form)
        mProgressView = findViewById(R.id.signup_progress)

        mUsernameView!!.setText("userclient")
        mPasswordView!!.setText("123456")
    }

    fun onUsernameSignInButtonClicked(v: View) {
        attemptLogin()
    }

    fun onForgetPasswordButtonClicked(v: View) {

    }

    fun onCreateAccountButtonClicked(v: View) {
        val intent = Intent(baseContext, SignupActivity::class.java)
        startActivity(intent)
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
        var mSuccess: Boolean = false
        internal var METHOD_GET = "GET"
        internal val READ_TIMEOUT = 15000
        internal val CONNECTION_TIMEOUT = 15000

        override fun doInBackground(vararg params: String): String? {
            val stringUrl = String.format("%s/login.php?username=%s&password=%s", G.SERVER_URL, mUsername, mPassword)
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
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true

                Log.e("url", connection.url.toString())
                connection.connect()

                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
//                inputLine = reader.readLine()
//                while (inputLine != null) {
//                    stringBuilder.append(inputLine)
//                    inputLine = reader.readLine()
//                }
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

            if (responseText == null) {
                Snackbar.make(mUsernameSignInButton!!, R.string.error_invalid_credential, Snackbar.LENGTH_SHORT).show()
                return
            }
            mAuthTask = null
            showProgress(false)
            Log.e("login-result", responseText)

            try {
                val json = JSONObject(responseText)
                val result = json.getString("result")
                if (result == "success") {
                    val user = json.getJSONObject("user")
                    val type = user.getString("Type").toLowerCase()
                    if (type != "productive_family" && type != "customer") {
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
                    G.userInfo.Type = type
                    G.userInfo.StoreID = user.getString("StoreID")

                    mSuccess = true
                    //                    mUsernameView.setEnabled(false);
                    //                    mPasswordView.setEnabled(false);
                    //                    mUsernameSignInButton.setEnabled(false);


                    val intent: Intent
                    if (type == "productive_family") {
                        intent = Intent(baseContext, ManagerMainActivity::class.java)
                        intent.putExtra(ManagerProductListFragment.STORE_ID_KEY, G.userInfo.StoreID)
                    } else {
                        intent = Intent(baseContext, CustomerMainActivity::class.java)
                    }
                    startActivity(intent)
                    mPasswordView!!.setText("")
                    //                    finish();
                    ////                    mfor.setEnabled(false);
                    ////                    mUsernameView.setEnabled(false);
                    //                    Snackbar snackbar = Snackbar.make(mUsernameSignInButton, R.string.success_sign_in, Snackbar.LENGTH_SHORT);
                    //                    snackbar.addCallback(new Snackbar.Callback() {
                    //                        @Override
                    //                        public void onDismissed(Snackbar snackbar, int event) {
                    //                            Intent intent;
                    //                            if (type.equals("productive_family")) {
                    //                                intent = new Intent(getBaseContext(), ManagerMainActivity.class);
                    //                                intent.putExtra(ManagerProductListFragment.STORE_ID_KEY, G.userInfo.StoreID);
                    //                            } else {
                    //                                intent = new Intent(getBaseContext(), CustomerMainActivity.class);
                    //                            }
                    //                            startActivity(intent);
                    //                            mPasswordView.setText("");
                    //                            finish();
                    //                        }
                    //                    });
                    //                    snackbar.show();
                    //                    finish();
                } else {
                    mSuccess = false
                    showProgress(false)
                    Snackbar.make(mUsernameSignInButton!!, json.getString("msg"), Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onCancelled() {
            mAuthTask = null
            mSuccess = false
            showProgress(false)
        }
    }

    override fun onBackPressed() {
        if (mAuthTask != null) {
            return
        }
        AlertDialog.Builder(this)
                .setTitle(R.string.title_confirm)
                .setMessage(R.string.message_confirm_back)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes) { arg0, arg1 -> super@LoginActivity.onBackPressed() }.create().show()
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