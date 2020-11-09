package com.example.mimmadjaved.brand4u;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mimmadjaved.brand4u.Common.Common;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.mimmadjaved.brand4u.LoginActivity.MyPREFERENCES;
import static com.example.mimmadjaved.brand4u.LoginActivity.NAME;
import static com.example.mimmadjaved.brand4u.LoginActivity.ID;

public class attendance extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static TextView usertxt;
    private static Button exit;
    private static Button OfficeIn;
    private static Button OfficeOut;
    String m_deviceId;
    private ProgressDialog pDialog;
    SharedPreferences sharedpreferences;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int LOCATION_REQUEST = 500;
    private double fusedLatitude = 0.0;
    private double fusedLongitude = 0.0;
    public String degholder, idholder, nameholder, latholder, longholder;
    JSONParser jsonParser = new JSONParser();
    LocationManager locationManager;
    boolean GpsStatus;
    Context context;
    ProgressDialog pdialog;

    String HttpURLin = "https://www.b4u.com.pk/mobile_app/attend_in.php";
    String TAG_SUCCESS = "success";
    String TAG_MESSAGE = "message";

    String HttpURLout = "http://b4u.com.pk/mobile_app/attend_out.php";
    String TAG_SSUCCESS = "success";
    String TAG_MESSSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        usertxt = (TextView) findViewById(R.id.display_name);
        OfficeIn = (Button) findViewById(R.id.mark_in);
        OfficeOut = (Button) findViewById(R.id.mark_out);

        context = getApplicationContext();

        pdialog = new ProgressDialog(attendance.this);
        pdialog.setTitle("Kindly Wait");
        pdialog.setMessage("Finding Your Current location...");
        pdialog.setCancelable(false);
        pdialog.show();
        CheckGpsStatus();

        mGoogleApiClient = new GoogleApiClient.Builder(attendance.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(attendance.this)
                .addOnConnectionFailedListener(attendance.this)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        m_deviceId = Common.getIMEIDeviceId(attendance.this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        usertxt.setText("Hello! " + sharedpreferences.getString(NAME, ""));
        nameholder = sharedpreferences.getString(NAME, "");
        idholder = sharedpreferences.getString(ID, "");

        OfficeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GpsStatus == true) {

                    if (Common.isNetworkAvailable(attendance.this)){
                        new inattendance().execute();
                    }else{

                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent1);
                }

            }
        });

        OfficeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GpsStatus == true) {
if (Common.isNetworkAvailable(attendance.this)){
    new outattendance().execute();
}else{
    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
}


                } else {
                    Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent1);
                }

            }
        });
    }

    public void CheckGpsStatus() {

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    class inattendance extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(attendance.this);
            pDialog.setMessage("Marking Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("UserId", idholder));
            params.add(new BasicNameValuePair("longitude", longholder));
            params.add(new BasicNameValuePair("latitude", latholder));
            //params.add(new BasicNameValuePair("imei", m_deviceId));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(HttpURLin, "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    return json.getString(TAG_MESSAGE);
                } else {

                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String message1) {
            pDialog.dismiss();
            if (message1 != null) {
                Toast.makeText(attendance.this, message1, Toast.LENGTH_LONG).show();
            }
        }

    }

    class outattendance extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(attendance.this);
            pDialog.setMessage("Marking Please Wait..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("UserId", idholder));
            params.add(new BasicNameValuePair("longitude", longholder));
            params.add(new BasicNameValuePair("latitude", latholder));


            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(HttpURLout, "GET", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SSUCCESS);

                if (success == 1) {

                    return json.getString(TAG_MESSSAGE);
                } else {

                    return json.getString(TAG_MESSSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String message1) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (message1 != null) {
                Toast.makeText(attendance.this, message1, Toast.LENGTH_LONG).show();
            }
        }

    }

    public void onConnected(@Nullable Bundle bundle) {

        requestLocationUpdates();
    }

    private void requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //   mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    public void onLocationChanged(Location location) {


        pdialog.dismiss();
        fusedLatitude = location.getLatitude();
        latholder = String.valueOf(fusedLatitude);
        fusedLongitude = location.getLongitude();
        longholder = String.valueOf(fusedLongitude);
        String current_LatLng = String.valueOf(fusedLatitude) + "," + String.valueOf(fusedLongitude);
        Log.e("rMap", "current" + current_LatLng);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
}
