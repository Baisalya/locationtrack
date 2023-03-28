package com.example.gpstask;

//import java.security.Provider;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.Manifest;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationService extends Service {

    private static final int GPS_CHECK_INTERVAL = 5000; // Check every 5 seconds
    private static final int UPDATE_INTERVAL = 60000; // Update every 1 minute

    private LocationManager locationManager;
    private DatabaseReference FirbaseDatabase;
    private String phoneNumber;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            checkGpsStatus();
            mHandler.postDelayed(this, GPS_CHECK_INTERVAL);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        FirbaseDatabase = FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        phoneNumber = intent.getStringExtra("phone_number");
        mHandler.post(mRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkGpsStatus() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(LocationService.this, "Please enable GPS", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            // Intent i2=new Intent("android.location.GPS_ENABLED_CHANGED");
            //i2.putExtra("enabled",true);
            //sendBroadcast(i2);
            startActivity(intent);
            // TODO: Handle GPS is not enabled
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Handle location permission not granted
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    float speed = location.getSpeed();

                    // Update Firebase every minute
                    if (System.currentTimeMillis() % UPDATE_INTERVAL == 0) {
                        // new location object
                        LocationModel locationData = new LocationModel(latitude, longitude, speed);

                        // Firebase database reference
                        DatabaseReference databaseReference = FirbaseDatabase;

                        // Set value to database reference
                        databaseReference.child("location").child(phoneNumber).setValue(locationData);
                    }
                }
            });
        }
    }
}

