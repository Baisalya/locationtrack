package com.example.gpstask;

//import java.security.Provider;

import static android.content.Intent.getIntent;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;



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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.Manifest;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class LocationService extends Service implements LocationListener {
    private static final int PERMISSION_REQUEST_CODE =123 ;
    private LocationManager locationManager;
    private PendingIntent locationPendingIntent;
    private static final long LOCATION_UPDATE_INTERVAL = 60000; // 1 min
    private static final int NOTIFICATION_ID = 1;
    private SharedPreferences sharedPreferences;
    private static final String NOTIFICATION_CHANNEL_ID = "LocationUpdatesChannel";
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_ENDPOINT = "https://api.tranzol.com/apiv1/PostLocation";
    private Handler handler = new Handler();
    @Override
    public void onCreate() {
        super.onCreate();
         sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Intent locationIntent = new Intent(this, LocationReceiver.class);
        locationIntent.setAction("LOCATION_UPDATE");
        PendingIntent locationPendingIntent = PendingIntent.getBroadcast(this, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, 0, this);

        // Create the notification channel (for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Location Updates", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        // Create the notification and show it
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("TrackMe Updates")
                .setContentText("Your location is being sent periodically.")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String phoneNo = sharedPreferences.getString("phone_number", "");
        // Post the location to the server
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float speed = location.getSpeed();
        //
        // Schedule the API request to be sent in the next time interval
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Prepare the request body with the location data
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

                // Execute the request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Notification notification = new NotificationCompat.Builder(LocationService.this, NOTIFICATION_CHANNEL_ID)
                                .setContentTitle("Failed")

                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .build();
                        startForeground(NOTIFICATION_ID, notification);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // Do something with the response...
                        Notification notification = new NotificationCompat.Builder(LocationService.this, NOTIFICATION_CHANNEL_ID)
                                .setContentTitle("Passed")

                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .build();
                        startForeground(NOTIFICATION_ID, notification);

                    }
                });
            }
        }, LOCATION_UPDATE_INTERVAL);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("lattitude: "+latitude+"Longititude: "+longitude)
                .setContentText("your"+phoneNo+" sent changed.")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}








