package com.asmaa.rma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by mamdouhelnakeeb on 7/8/17.
 */

public class Register extends BaseActivity {

    EditText nameET, mobileET, passET;
    Button registerBtn;
    private ProgressDialog mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        nameET = (EditText) findViewById(R.id.nameET);
        mobileET = (EditText) findViewById(R.id.mobileET);
        passET = (EditText) findViewById(R.id.passET);


        registerBtn = (Button) findViewById(R.id.registerButton);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

    }

    private void registerUser(){

        String name = nameET.getText().toString();
        String mobile = mobileET.getText().toString();
        String password = passET.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
            return;
        }
        if (mobile.isEmpty()) {
            Toast.makeText(this, "Please enter a mobile number", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "register.php?"
                + "name=" + name
                + "&"
                + "mobile=" + mobile
                + "&"
                + "password=" + password;

        Log.d("url", url);

        showSpinner();

        getJSON.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Something went wrong
                mSpinner.hide();
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
                                    Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();

                                    openPlaceCallActivity();
                                }
                            });



                        } else {

                            // Error occurred in registration. Get the error
                            final String errorMsg = jObj.getString("message");

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(Register.this, errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    // Request not successful
                }
            }
        });
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
}
