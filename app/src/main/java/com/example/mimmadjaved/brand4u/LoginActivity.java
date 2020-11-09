package com.example.mimmadjaved.brand4u;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mimmadjaved.brand4u.Common.Common;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText txt_username,txt_pass;
    Button loginBtn;

    String m_deviceId;
    JSONParser jsonParser = new JSONParser();
    SharedPreferences sharedpreferences;
    String Token;
    String HttpURL = "https://www.b4u.com.pk/mobile_app/login_sami.php";
    ProgressDialog progressDialog;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_ID = "user_id";
    private static final String TAG_NAME = "name";

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String ID = "idKey";
    public static final String NAME = "nameKey";
    final  static String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txt_username=findViewById(R.id.txt_username);
        txt_pass=findViewById(R.id.txt_pass);
        loginBtn=findViewById(R.id.loginBTN);

        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");




        requestPermission();
        Token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, " token: " +Token);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        m_deviceId = Common.getIMEIDeviceId(LoginActivity.this);

        if (!TextUtils.isEmpty(sharedpreferences.getString(ID,"")) && !TextUtils.isEmpty(sharedpreferences.getString(NAME,""))){
            Intent ii = new Intent(LoginActivity.this, attendance.class);
            finish();
            startActivity(ii);

        }

        //new AttemptLogin().execute();

       loginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               if (TextUtils.isEmpty(txt_username.getText().toString())){
                   Toast.makeText(LoginActivity.this, "Enter Username", Toast.LENGTH_SHORT).show();
               }else if (TextUtils.isEmpty(txt_pass.getText().toString())){
                   Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
               }else{

                   if (Common.isNetworkAvailable(LoginActivity.this)){
                       new AttemptLogin().execute();
                   }else{
                       Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                   }



               }
           }
       });
    }
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
    private void requestPermission() {
        Dexter.withActivity(LoginActivity.this)
                .withPermissions(
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
    class AttemptLogin extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();

        }

        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user", txt_username.getText().toString()));
                params.add(new BasicNameValuePair("pass", txt_pass.getText().toString()));

                JSONObject json = jsonParser.makeHttpRequest(HttpURL, "POST", params);
                // checking log for json response
                Log.d("Login attempt", json.toString());

                int success = json.getInt(TAG_SUCCESS);
                String id = json.getString(TAG_ID);
                String name = json.getString(TAG_NAME);
                if (success == 1) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(ID, id);
                    editor.putString(NAME, name);
                    editor.commit();


                    Intent ii = new Intent(LoginActivity.this, attendance.class);
                    finish();
                    startActivity(ii);

                    return json.getString(TAG_MESSAGE);


                } else {

                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String message2) {

            progressDialog.dismiss();
            if (message2 != null) {
                Toast.makeText(LoginActivity.this, message2, Toast.LENGTH_LONG).show();
            }else if(message2 == null)
            {
                Toast.makeText(LoginActivity.this, "Invalid Users", Toast.LENGTH_LONG).show();

            }

        }
    }
}