package com.innocomm.innodpcagent;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.innocomm.innoservice.InnoManager;

public class Application extends android.app.Application {
    private static Application sApplication;
    private static final String TAG = "Application";
    public static final String KEY_DISABLE_STATUSBAR = "disable_statusbar";
    public static final String KEY_HIDE_APPS = "hide_apps";
    public InnoManager mInnoManager;


    public static final String[] hideList = new String[]{
            "com.android.email",
            "com.android.music",
            "com.android.phone",
            "com.android.browser",
            "com.android.mms",
            "com.android.calendar",
            "com.android.contacts",
            "com.android.gallery3d",
            "com.android.dialer",
            "com.android.deskclock",
            "com.android.quicksearchbox",
            "com.android.documentsui",
            //"com.android.settings"

    };

    public static Application getInstance() {
        return sApplication;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate ");
        sApplication = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mInnoManager = InnoManager.getInstance();

        CrashHandler mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext(), getClass());
    }

    private SharedPreferences prefs;

    public void setPref(String key, boolean val) {
        prefs.edit().putBoolean(key, val).commit();
    }

    public boolean getPref(String key, boolean defaultVal) {
        return prefs.getBoolean(key, defaultVal);
    }
}

