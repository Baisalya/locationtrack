package com.example.gpstask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent locationServiceIntent = new Intent(context, LocationService.class);
        context.startService(locationServiceIntent);
    }


}