package com.example.gpstask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.Notification;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;





import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.FirebaseDatabase;
import android.app.Service;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION = 123;
    private static final int GPS_CHECK_INTERVAL = 5000; // Check every 5 seconds
    private static int UPDATE_INTERVAL = 60000; // Update every 1 minute
    private long lastUpdateTime = 0;

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView speedTextView;
    private LocationManager locationManager;
    //  private DatabaseReference FirbaseDatabase;
    private String phoneNo;
    private long updateInterval;
    private RadioGroup timeintervaloption;
    private RadioButton radioButton5, radioButton15, radioButton10, radioButtonoff, radioButton60;
    private Intent serviceIntent;
    private SharedPreferences sharedPreferences;

    private Handler handler = new Handler();
    private MediaType mediaType;
    // private RequestBody body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startbgservice();

        latitudeTextView = (TextView) findViewById(R.id.lattitude);
        longitudeTextView = (TextView) findViewById(R.id.longitutde);
        speedTextView = (TextView) findViewById(R.id.speedText);
        TextView urphoneno = findViewById(R.id.ContactNumber);
        // Get an instance of SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get the saved "phoneno" value from SharedPreferences

        //Radio Button
        timeintervaloption = findViewById(R.id.timeinterval);
        radioButton5 = findViewById(R.id.radiobutton5);
        radioButton10 = findViewById(R.id.radiobutton10);
        radioButton15 = findViewById(R.id.radiobutton15);
        radioButtonoff = findViewById(R.id.radiobuttonoff);
        EditText phoneno = findViewById(R.id.phoneno);
        Button submit = findViewById(R.id.submit);
        // Set the default option to 15 min

        final String[] selectedoption = {sharedPreferences.getString("selectedoption", "15")};
        // Set the selected radio button based on the saved "selectedoption" value
//Set the selected radio button based on the saved "selectedoption" value
        switch (selectedoption[0]) {
            case "5":
                radioButton5.setChecked(true);
                UPDATE_INTERVAL = 60000;
                break;
            case "10":
                radioButton10.setChecked(true);
                UPDATE_INTERVAL = 600000;
                break;
            case "Off":
                radioButtonoff.setChecked(true);
                break;
            default:
                radioButton15.setChecked(true); // Default option
                UPDATE_INTERVAL = 900000;
                break;
        }
//On Save Button Click
        //On Save Button Click
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = phoneno.getText().toString();

                if (phone.isEmpty() || phone.length() != 10) {
                    phoneno.setError("Please enter a valid 10-digit phone number.");
                    return;
                }

                //Get the selected radio button value
                int selectedId = timeintervaloption.getCheckedRadioButtonId();
                switch (selectedId) {
                    case R.id.radiobutton5:
                        selectedoption[0] = "5";
                        UPDATE_INTERVAL = 100000;
                        startbgservice();

                        break;
                    case R.id.radiobutton10:
                        selectedoption[0] = "10";
                        UPDATE_INTERVAL = 600000;
                        startbgservice();
                        break;
                    case R.id.radiobuttonoff:
                        selectedoption[0] = "Off";
                        break;
                    default:
                        selectedoption[0] = "15"; // Default option
                        UPDATE_INTERVAL = 900000;
                        startbgservice();
                        break;
                }

                //Save the selected radio button value and phone number to SharedPreferences
                editor.putString("selectedoption", selectedoption[0]);
                editor.putInt("updateInterval", UPDATE_INTERVAL);
                editor.putString("phone_number", phone);
                editor.apply();
                //updateInterval = sharedPreferences.getLong("updateInterval", 900000);
                // String lala= Long.toString(updateInterval);
                //Toast.makeText(MainActivity.this, lala, Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, phone + " Interval: " + selectedoption[0], Toast.LENGTH_SHORT).show();
            }
        });

        //Toast.makeText(this,"Your Location will update in every"+ String.valueOf(UPDATE_INTERVAL)+"m/s", Toast.LENGTH_SHORT).show();
        phoneNo = sharedPreferences.getString("phone_number", "");
        updateInterval = sharedPreferences.getLong("updateIntervals", 900000);
        //String lala= Long.toString(updateInterval);
        //Toast.makeText(this, lala, Toast.LENGTH_SHORT).show();
        urphoneno.setText("Tracking no is:" + phoneNo);





        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }

    }

    private void startbgservice() {
        Intent intent = new Intent(this, LocationService.class);
       startService(intent);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkGpsStatus();
            handler.postDelayed(this, GPS_CHECK_INTERVAL);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
 /*   @Override
    protected void onDestroy() {
        super.onDestroy();
        startservice();
    }*/
    /*private void startservice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }*/
    private void checkGpsStatus() {
        String selectedOption = sharedPreferences.getString("selectedoption", "15");
        if (selectedOption.equals("Off")) {
            // If the selected option is "Off", skip the GPS check
            Intent intent = new Intent(this, LocationService.class);
            stopService(intent);
            return;
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(MainActivity.this, "Please enable GPS", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            // Intent i2=new Intent("android.location.GPS_ENABLED_CHANGED");
            //i2.putExtra("enabled",true);
            //sendBroadcast(i2);
            startActivity(intent);
        } else {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling ActivityCompat#requestPermissions here to request the missing permissions
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    float speed = location.getSpeed();

                    latitudeTextView.setText("Latitude: " + latitude);
                    longitudeTextView.setText("Longitude: " + longitude);
                    speedTextView.setText("Speed: " + speed + " m/s");
                    //Toast.makeText(MainActivity.this, phoneNo, Toast.LENGTH_SHORT).show();
               /*      Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(MainActivity.this, "Sending", Toast.LENGTH_LONG).show();
                            RequestBody body = new FormBody.Builder()
                                    .add("ph", phoneNo)
                                    .add("lt", Double.toString(latitude))
                                    .add("lg", Double.toString(longitude))
                                    .add("sp", Float.toString(speed))
                                    .add("dv", "dvValue")
                                    .build();
                            // Build request
                            Request request = new Request.Builder()
                                    .url("https://api.tranzol.com/apiv1/PostLocation")
                                    .post(body)
                                    .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                                    .addHeader("clientid", "TRANZOLGPS")
                                    .addHeader("clientsecret", "TRANZOLBO436535345SS2238RC")
                                    .build();

                            // Send request asynchronously
                            OkHttpClient client = new OkHttpClient();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    // Handle successful response
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    // Handle error
                                }

                            });
                            handler.postDelayed(this, updateInterval);
                        }
                    };
                    handler.postDelayed(runnable, updateInterval);*/
                    // Update api every UPDATE_INTERVAL milliseconds

   /*  if (System.currentTimeMillis() - lastUpdateTime >= UPDATE_INTERVAL || lastUpdateTime == 0){
                        lastUpdateTime = System.currentTimeMillis();
                        // Create location data object
                       // LocationModel locationData = new LocationModel(latitude, longitude, speed);

                        // Get phone number from Intent
                       // String phoneNumber = getIntent().getStringExtra("phone_number");
                        //Toast.makeText(MainActivity.this, phoneNo, Toast.LENGTH_SHORT).show();
                        // Build request body

                    }*/

                }
            });

        }
    }
}
