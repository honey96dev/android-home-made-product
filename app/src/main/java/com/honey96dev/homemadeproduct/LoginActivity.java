package com.honey96dev.homemadeproduct;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.honey96dev.homemadeproduct.p4customer.CustomerProductListActivity;
import com.honey96dev.homemadeproduct.p4manager.ManagerMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    UserLoginTask mAuthTask = null;

    // UI references.
    EditText mUsernameView;
    EditText mPasswordView;
    Button mUsernameSignInButton;
    View mProgressView;
    View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mUsernameView.setText("usertest");
        mPasswordView.setText("123456");
    }

    public void onUsernameSignInButtonClicked(View v) {
        attemptLogin();
    }

    public void onForgetPasswordButtonClicked(View v) {

    }

    public void onCreateAccountButtonClicked(View v) {

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute();
        }
    }

    boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {
        public boolean mSuccess;
        String METHOD_GET = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        final String mUsername;
        final String mPassword;

        UserLoginTask(String username, String password) {
            super();
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = String.format("http://173.199.122.197/login.php?username=%s&password=%s", mUsername, mPassword);
            String result;
            String inputLine;
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(METHOD_GET);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
//            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                Log.e("url", connection.getURL().toString());
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

        @Override
        protected void onPostExecute(final String responseText) {
            super.onPostExecute(responseText);
            showProgress(false);

            if (responseText == null) {
                Snackbar.make(mUsernameSignInButton, R.string.error_invalid_credential, Snackbar.LENGTH_SHORT).show();
                return;
            }
            mAuthTask = null;
            showProgress(false);
            Log.e("login-result", responseText);

            try {
                JSONObject json = new JSONObject(responseText);
                String result = json.getString("result");
                if (result.equals("success")) {
                    JSONObject user = json.getJSONObject("user");
                    final String type = user.getString("Type").toLowerCase();
                    if (!type.equals("productive_family") && !type.equals("client")) {
                        Snackbar.make(mUsernameSignInButton, R.string.error_invalid_user_type, Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    G.userInfo.UserID = user.getString("UserID");
                    G.userInfo.Username = user.getString("Username");
                    G.userInfo.FirstName = user.getString("FirstName");
                    G.userInfo.LastName = user.getString("LastName");
                    G.userInfo.Email = user.getString("Email");
                    G.userInfo.Password = user.getString("Password");
                    G.userInfo.City = user.getString("City");
                    G.userInfo.Phone = user.getString("Phone");
                    G.userInfo.Type = type;

                    mSuccess = true;
                    mUsernameView.setEnabled(false);
                    mPasswordView.setEnabled(false);
                    mUsernameSignInButton.setEnabled(false);
//                    mfor.setEnabled(false);
//                    mUsernameView.setEnabled(false);
                    Snackbar snackbar = Snackbar.make(mUsernameSignInButton, R.string.success_sign_in, Snackbar.LENGTH_SHORT);
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            int a;
                            a = 1;
                            Intent intent;
                            if (type.equals("productive_family")) {
                                intent = new Intent(getBaseContext(), ManagerMainActivity.class);
                            } else {
                                intent = new Intent(getBaseContext(), CustomerProductListActivity.class);
                            }
                            startActivity(intent);
                            mPasswordView.setText("");
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {
                            int a;
                            a = 2;
                        }
                    });
                    snackbar.show();
//                    finish();
                } else {
                    mSuccess = false;
                    showProgress(false);
                    Snackbar.make(mUsernameSignInButton, json.getString("msg"), Snackbar.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            mSuccess = false;
            showProgress(false);
        }
    }
}