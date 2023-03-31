package com.example.gpstask;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREFS_NAME = "TrackMePrefs";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_UPDATE_INTERVAL = "updateInterval";

    private SharedPreferences sharedPreferences;

    PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    void savePhoneNumber(String phoneNumber) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    String getPhoneNumber() {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, null);
    }

    void saveUpdateInterval(int updateInterval) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_UPDATE_INTERVAL, updateInterval);
        editor.apply();
    }

    int getUpdateInterval() {
        return sharedPreferences.getInt(KEY_UPDATE_INTERVAL, 60000);
    }

}
