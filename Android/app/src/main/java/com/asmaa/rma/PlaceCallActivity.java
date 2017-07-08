package com.asmaa.rma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.calling.Call;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.Response;

public class PlaceCallActivity extends BaseActivity {

    private Button mCallButton;
    private EditText userNoET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        userNoET = (EditText) findViewById(R.id.userNo);
        mCallButton = (Button) findViewById(R.id.callButton);
        mCallButton.setEnabled(false);
        mCallButton.setOnClickListener(buttonClickListener);

        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(buttonClickListener);
    }

    @Override
    protected void onServiceConnected() {
        TextView userName = (TextView) findViewById(R.id.loggedInName);
        userName.setText(getSharedPreferences("UserDetails", MODE_PRIVATE).getString("name", ""));
        mCallButton.setEnabled(true);
    }

    private void stopButtonClicked() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }

        SharedPreferences prefs = getSharedPreferences("UserDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("logged", false);

        finish();
    }

    private void callButtonClicked() {
        String userNo = userNoET.getText().toString();
        if (userNo.isEmpty()) {
            Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Call call = getSinchServiceInterface().callUser(userNo);
            if (call == null) {
                // Service failed for some reason, show a Toast and abort
                Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before "
                        + "placing a call.", Toast.LENGTH_LONG).show();
                return;
            }
            String callId = call.getCallId();
            Intent callScreen = new Intent(PlaceCallActivity.this, CallScreenActivity.class);
            callScreen.putExtra(SinchService.CALL_ID, callId);
            startActivity(callScreen);
        } catch (MissingPermissionException e) {
            ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }

    private OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.callButton:
                    callButtonClicked();
                    break;

                case R.id.stopButton:
                    stopButtonClicked();
                    break;

            }
        }
    };

    private void callUser(){

        String userNo = userNoET.getText().toString();
        if (userNo.isEmpty()) {
            Toast.makeText(this, "Please enter a mobile to call", Toast.LENGTH_LONG).show();
            return;
        }

        GetJSON getJSON = new GetJSON();

        String url = AppConsts.API_URL + "call.php?"
                + "mobile=" + userNo;

        Log.d("url", url);


        getJSON.get(url, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Something went wrong
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

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


                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {

                                    Toast.makeText(PlaceCallActivity.this, message, Toast.LENGTH_SHORT).show();
                                    try {
                                        Call call = getSinchServiceInterface().callUser(mobile);
                                        if (call == null) {
                                            // Service failed for some reason, show a Toast and abort
                                            Toast.makeText(PlaceCallActivity.this, "Service is not started. Try stopping the service and starting it again before "
                                                    + "placing a call.", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        String callId = call.getCallId();
                                        Intent callScreen = new Intent(PlaceCallActivity.this, CallScreenActivity.class);
                                        callScreen.putExtra(SinchService.CALL_ID, callId);
                                        startActivity(callScreen);
                                    } catch (MissingPermissionException e) {
                                        ActivityCompat.requestPermissions(PlaceCallActivity.this, new String[]{e.getRequiredPermission()}, 0);
                                    }
                                }
                            });

                        } else {

                            // Error occurred in registration. Get the error
                            final String errorMsg = jObj.getString("message");

                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(PlaceCallActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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
}
