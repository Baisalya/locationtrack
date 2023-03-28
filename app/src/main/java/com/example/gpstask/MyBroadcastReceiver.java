package com.example.gpstask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.location.GPS_ENABLED_CHANGED")) {
            boolean enabled = intent.getBooleanExtra("enabled", false);

        }
    }
}