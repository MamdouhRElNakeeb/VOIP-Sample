package com.asmaa.rma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sinch.android.rtc.SinchError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends BaseActivity implements SinchService.StartFailedListener {

    private Button mLoginButton, registerBtn;
    private EditText mobileET;
    private EditText passET;
    private ProgressDialog mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);

        /*
        if (prefs.getBoolean("logged", false)){

            openPlaceCallActivity();
        }
*/
        registerBtn = (Button) findViewById(R.id.registerButton);
        mobileET = (EditText) findViewById(R.id.mobileET);
        passET = (EditText) findViewById(R.id.passET);

        mLoginButton = (Button) findViewById(R.id.loginButton);
        mLoginButton.setEnabled(false);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainActivity = new Intent(getBaseContext(), Register.class);
                startActivity(mainActivity);
            }
        });
    }

    @Override
    protected void onServiceConnected() {
        mLoginButton.setEnabled(true);
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
    }

    @Override
    public void onStarted() {
        openPlaceCallActivity();
    }

    private void loginClicked() {
        String mobile = mobileET.getText().toString();

        if (mobile.isEmpty()) {
            Toast.makeText(this, "Please enter a mobile number", Toast.LENGTH_LONG).show();
            return;
        }

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(mobile);
            showSpinner();
        } else {
            openPlaceCallActivity();
        }
    }

    private void openPlaceCallActivity() {
        Intent mainActivity = new Intent(this, PlaceCallActivity.class);
        startActivity(mainActivity);
    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Logging in");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }

    private void loginUser(){

        String mobile = mobileET.getText().toString();
        String password = passET.getText().toString();

        if (mobile.isEmpty()) {
            Toast.makeText(this, "Please enter a mobile number", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "login.php?"
                + "mobile=" + mobile
                + "&"
                + "password=" + password;

        Log.d("url", url);

        showSpinner();

        getJSON.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("response", responseStr);

                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            // User successfully stored in MySQL
                            // Now store the user in sqlite
                            final String message = jObj.getString("message");
                            String name = jObj.getString("name");
                            final String mobile = jObj.getString("mobile");

                            SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putString("name", name);
                            editor.putString("mobile", mobile);
                            editor.putBoolean("logged", true);

                            editor.apply();

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {

                                    mSpinner.hide();
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                    if (!getSinchServiceInterface().isStarted()) {
                                        getSinchServiceInterface().startClient(mobile);
                                    } else {
                                        openPlaceCallActivity();
                                    }
                                }
                            });

                        } else {

                            // Error occurred in registration. Get the error
                            final String errorMsg = jObj.getString("message");

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    // Request not successful

                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {

                            mSpinner.hide();

                        }
                    });
                }
            }
        });
    }


}
