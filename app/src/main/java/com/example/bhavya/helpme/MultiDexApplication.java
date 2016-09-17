package com.example.bhavya.helpme;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.view.Display;
import android.view.WindowManager;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bhavya on 17/9/16.
 */

public abstract class MultiDexApplication extends android.support.multidex.MultiDexApplication{

    SharedPreferences mPref;
    private static android.support.multidex.MultiDexApplication sInstance;

    public static android.support.multidex.MultiDexApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        initializeInstance();
        mPref = this.getApplicationContext().getSharedPreferences("HelpMe", MODE_PRIVATE);
        onAppCreated();
    }

    private void initializeInstance() {

        // Do your application wise initialization task
        screenConfiguration();

        // set application wise preference
        mPref = this.getApplicationContext().getSharedPreferences("pref_key", MODE_PRIVATE);
    }

    // particularly applicable in library projects
    public abstract void onAppCreated();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void screenConfiguration() {
        Configuration config = getResources().getConfiguration();
        boolean isTab = (config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;


        Point size = new Point();
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceScreenWidth;
        int deviceScreenHeight;

        try {
            display.getSize(size);
            deviceScreenWidth = size.x;
            deviceScreenHeight = size.y;
        } catch (NoSuchMethodError e) {
            deviceScreenWidth = display.getWidth();
            deviceScreenHeight = display.getHeight();
        }
    }

    public boolean isFirstRun() {
        // return true if the app is running for the first time
        return mPref.getBoolean("is_first_run", true);
    }

    public void setRunned() {
        // after a successful run, call this method to set first run false
        SharedPreferences.Editor edit = mPref.edit();
        edit.putBoolean("is_first_run", false);
        edit.commit();
    }

    @Override
    public void onTerminate() {
        // Do your application wise Termination task
        super.onTerminate();
    }

}
