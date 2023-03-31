package com.example.gpstask;

//import java.security.Provider;

import static android.content.Intent.getIntent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.telephony.SmsManager;
import android.util.Log;
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

    //private static final int UPDATE_INTERVAL = 60000; // Update every 1 minute
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f; // 10 meters
    private LocationManager locationManager;
    private String phoneNumber;
    private  int UPDATE_INTERVAL;
    private Handler handler = new Handler();
    private Runnable locationRunnable = new LocationRunnable();

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "location_service_channel";
    private NotificationManager notificationManager;
    private OkHttpClient client;
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phone_number", "");
        UPDATE_INTERVAL=sharedPreferences.getInt("updateInterval",60000);
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
                .setContentTitle("TrackMe Service")
                .setContentText("Location service is running in the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);
        // Add an intent to launch the app's main activity
       /* Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        builder.setContentIntent(pendingIntent);*/
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
            // Update the notification with the current latitude and longitude
            NotificationCompat.Builder builder = new NotificationCompat.Builder(LocationService.this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("TrackMe Service")
                    .setContentText("Latitude: " + latitude + ", Longitude: " + longitude)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true);
            Notification notification = builder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);
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
                    .url("https://api.tranzol.com/apiv1/PostLocations")
                    .post(body)
                    .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .addHeader("clientid", "TRANZOLGPS")
                    .addHeader("clientsecret", "TRANZOLBO436535345SS2238RC")
                    .build();
            // Create the message with the location data
            String message = "Location: " + phoneNumber + "\n"
                    + "Latitude: " + latitude + "\n"
                    + "Longitude: " + longitude + "\n"
                    + "Speed: " + speed;
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d("LocationService", "Location data sent successfully"+ body.toString());
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("LocationService", "Failed to send location data");
                    // Handle the error
                }
            });
           // Toast.makeText(LocationService.this, "background service", Toast.LENGTH_LONG).show();

        }
    };

    private class LocationRunnable implements Runnable {
        @Override
        public void run() {
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        UPDATE_INTERVAL, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            } catch (SecurityException e) {
                // Handle the exception
            } catch (Exception e) {
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






