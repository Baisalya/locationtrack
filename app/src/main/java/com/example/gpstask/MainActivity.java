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
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;





import android.Manifest;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.FirebaseDatabase;
import android.app.Service;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION = 123;
    private static final int GPS_CHECK_INTERVAL = 5000; // Check every 5 seconds
    private static  int UPDATE_INTERVAL = 60000; // Update every 1 minute
    private static final int MINUTE_IN_MILLIS = 60000;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView speedTextView;
    private LocationManager locationManager;
    private DatabaseReference FirbaseDatabase;
    private String phoneNumber;
    private RadioGroup timeintervaloption;
    private RadioButton radioButton5,radioButton15,radioButton30,radioButton45,radioButton60;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeTextView = (TextView) findViewById(R.id.lattitude);
        longitudeTextView = (TextView) findViewById(R.id.longitutde);
        speedTextView = (TextView) findViewById(R.id.speedText);
        TextView urphoneno = findViewById(R.id.ContactNumber);
        // Get an instance of SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get the saved "phoneno" value from SharedPreferences
        String phoneNo = sharedPreferences.getString("phone_number", "");
        urphoneno.setText("Tracking no is:"+phoneNo);
        //Radio Button
        timeintervaloption=findViewById(R.id.timeinterval);
        radioButton5=findViewById(R.id.radiobutton5);
        radioButton15=findViewById(R.id.radiobutton15);
        radioButton30=findViewById(R.id.radiobutton30);
        radioButton45=findViewById(R.id.radiobutton60);
        // Set the default option to 15 seconds
       // radioButton15.setChecked(true);
        String selectedoption = sharedPreferences.getString("selectedoption", "15");
        // Set the selected radio button based on the saved "selectedoption" value
        switch (selectedoption) {
            case "5":
                radioButton5.setChecked(true);
                UPDATE_INTERVAL = 300000;
                break;
            case "30":
                radioButton30.setChecked(true);
                UPDATE_INTERVAL = 1800000;
                break;
            case "45":
                radioButton45.setChecked(true);
                UPDATE_INTERVAL = 2700000;
                break;
            case "60":
                radioButton60.setChecked(true);
                UPDATE_INTERVAL = 3600000;
                break;
            default:
                radioButton15.setChecked(true); // Default option
                break;
        }
        timeintervaloption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String selectedoption = "";
                switch (i) {
                    case R.id.radiobutton5:
                        UPDATE_INTERVAL = 300000;
                        selectedoption = "5";
                        break;
                    case R.id.radiobutton30:
                        UPDATE_INTERVAL = 1800000;
                        selectedoption = "30";
                        break;
                    case R.id.radiobutton45:
                        UPDATE_INTERVAL = 2700000 ;
                        selectedoption = "45";
                        break;
                    case R.id.radiobutton60:
                        UPDATE_INTERVAL = 3600000;
                        selectedoption = "60";
                        break;
                    default:
                        UPDATE_INTERVAL = 900000; // Default option
                        selectedoption = "15";
                        break;
                }
                editor.putString("selectedoption", selectedoption);
                editor.apply();
            }
        });





   /*     Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
        serviceIntent.putExtra("phone_number", phoneNumber);
        startService(serviceIntent);*/


        FirbaseDatabase = FirebaseDatabase.getInstance().getReference();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
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

    private void checkGpsStatus() {
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
                    speedTextView.setText("Speed: " + speed + " m/s" );

                    // Update Firebase  selcted on dropdown minute
                    if (System.currentTimeMillis() % UPDATE_INTERVAL == 0) {
                        // new location object
                        LocationModel locationData = new LocationModel(latitude, longitude, speed);

                        //  phone number from Intent
                        String phoneNumber = getIntent().getStringExtra("phone_number");

                        //  Firebase database reference
                        DatabaseReference databaseReference = FirbaseDatabase;

                        // Set value to database reference
                        databaseReference.child("location").child(phoneNumber).setValue(locationData);
                    }
                }
            });
        }
    }
}