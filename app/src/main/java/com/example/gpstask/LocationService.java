package com.example.gpstask;

//import java.security.Provider;

import static android.content.Intent.getIntent;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationService extends Service {

    private static final int UPDATE_INTERVAL = 60000; // Update every 1 minute
    private LocationManager locationManager;
    //private DatabaseReference firebaseDatabase;
    private String phoneNumber;
    private Handler handler = new Handler();
    private Runnable locationRunnable = new LocationRunnable();
      private OkHttpClient client;
    @Override
    public void onCreate() {
        super.onCreate();
      //  firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phone_number", "");
         client = new OkHttpClient();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(locationRunnable, UPDATE_INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(locationRunnable); // remove callbacks to prevent leaks
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float speed = location.getSpeed();
            Toast.makeText(LocationService.this,phoneNumber, Toast.LENGTH_SHORT).show();
            String lt= Double.toString(latitude) ;
            String lg= Double.toString(longitude);
            String sp=Float.toString(speed);
            RequestBody body = new FormBody.Builder()
                    .add("ph",phoneNumber)
                    .add("lt",lt)
                    .add("lg",lg)
                    .add("sp",sp)
                    .add("dv","dv")
                    .build();
            Request request = new Request.Builder()
                    .url("https://api.tranzol.com/apiv1/PostLocation")
                    .post(body)
                    .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .addHeader("clientid", "TRANZOLGPS")
                    .addHeader("clientsecret", "TRANZOLBO436535345SS2238RC")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                }

                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle the error
                }
            });
           // firebaseDatabase.child(phoneNumber).child("latitude").setValue(latitude);
          //  firebaseDatabase.child(phoneNumber).child("longitude").setValue(longitude);
           // firebaseDatabase.child(phoneNumber).child("speed").setValue(speed);
        }
    };

    private class LocationRunnable implements Runnable {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling ActivityCompat#requestPermissions here to request the missing permissions
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}



