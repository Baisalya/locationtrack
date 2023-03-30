package com.example.gpstask;

//import java.security.Provider;

import static android.content.Intent.getIntent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
    private String phoneNumber;
    private Handler handler = new Handler();
    private Runnable locationRunnable = new LocationRunnable();
    private OkHttpClient client;
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "location_service_channel";
    private NotificationManager notificationManager;
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phone_number", "");

        client = new OkHttpClient();
        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Location Service")
                .setContentText("Location service is running in the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        Notification notification = builder.build();

        startForeground(NOTIFICATION_ID, notification);

        // Start the location updates
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
            Toast.makeText(LocationService.this, "Location"+phoneNumber, Toast.LENGTH_SHORT).show();
            String lt = Double.toString(latitude);
            String lg = Double.toString(longitude);
            String sp = Float.toString(speed);
            RequestBody body = new FormBody.Builder()
                    .add("ph", phoneNumber)
                    .add("lt", lt)
                    .add("lg", lg)
                    .add("sp", sp)
                    .add("dv", "dv")
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
        }
    };

    private class LocationRunnable implements Runnable {
        @Override
        public void run() {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                // Handle the exception
            }
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


