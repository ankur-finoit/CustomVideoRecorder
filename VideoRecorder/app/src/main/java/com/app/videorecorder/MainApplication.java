package com.app.videorecorder;

import android.app.Application;

import com.app.videorecorder.view.StatusBarData;

/**
 * Created by Ankur Parashar on 11/29/2016.
 */
public class MainApplication extends Application {

    private static StatusBarData mStatusBarData;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static StatusBarData getStatusBarData() {
        return mStatusBarData;
    }

    public static void setStatusBarData(StatusBarData mStatusBarData) {
        MainApplication.mStatusBarData = mStatusBarData;
    }

}
