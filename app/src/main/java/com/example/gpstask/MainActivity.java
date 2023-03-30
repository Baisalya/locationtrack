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
    private static  int UPDATE_INTERVAL = 60000; // Update every 1 minute
    private long lastUpdateTime = 0;

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView speedTextView;
   private LocationManager locationManager;
  //  private DatabaseReference FirbaseDatabase;
    private String phoneNo;
    private RadioGroup timeintervaloption;
    private RadioButton radioButton5,radioButton15,radioButton30,radioButton45,radioButton60;
    private Intent serviceIntent;
    private Handler handler = new Handler();
    private MediaType mediaType;
   // private RequestBody body;

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
         phoneNo = sharedPreferences.getString("phone_number", "");
        urphoneno.setText("Tracking no is:"+phoneNo);
        //Radio Button
        timeintervaloption=findViewById(R.id.timeinterval);
        radioButton5=findViewById(R.id.radiobutton5);
        radioButton15=findViewById(R.id.radiobutton15);
        radioButton30=findViewById(R.id.radiobutton30);
        radioButton45=findViewById(R.id.radiobutton45);
        radioButton60=findViewById(R.id.radiobutton60);

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
//
        Toast.makeText(this,"Your Location will update in every"+ String.valueOf(UPDATE_INTERVAL)+"m/s", Toast.LENGTH_SHORT).show();

/*
        String requestBodyString = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
                "Content-Disposition: form-data; name=\"lt\"\r\n\r\n" +
                latitudeTextView + "\r\n" +
                "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
                "Content-Disposition: form-data; name=\"lg\"\r\n\r\n" +
                longitudeTextView + "\r\n" +
                "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
                "Content-Disposition: form-data; name=\"sp\"\r\n\r\n" +
                speedTextView + "\r\n" +
                "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
                "Content-Disposition: form-data; name=\"ph\"\r\n\r\n" +
                phoneNo + "\r\n" +
                "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
                "Content-Disposition: form-data; name=\"dv\"\r\n\r\n" +
                "dvValue" + "\r\n" +
                "------WebKitFormBoundary7MA4YWxkTrZu0gW--";*/

   /*     Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
        serviceIntent.putExtra("phone_number", phoneNumber);
        startService(serviceIntent);*/


        //FirbaseDatabase = FirebaseDatabase.getInstance().getReference();

      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
        //
       /* serviceIntent=new Intent(MainActivity.this,LocationService.class);
        serviceIntent.putExtra("phone_number", phoneNo);*/
        //startservice();
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        startservice();
    }
    private void startservice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
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
                    speedTextView.setText("Speed: " + speed + " m/s");
                    //Toast.makeText(MainActivity.this, phoneNo, Toast.LENGTH_SHORT).show();
                    // Update api every UPDATE_INTERVAL milliseconds
                    if (System.currentTimeMillis() % UPDATE_INTERVAL == 0  || lastUpdateTime == 0){
                        lastUpdateTime = System.currentTimeMillis();
                        // Create location data object
                       // LocationModel locationData = new LocationModel(latitude, longitude, speed);

                        // Get phone number from Intent
                       // String phoneNumber = getIntent().getStringExtra("phone_number");
                        //Toast.makeText(MainActivity.this, phoneNo, Toast.LENGTH_SHORT).show();
                        // Build request body
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
                    }
                }
            });

        }
    }
}