package com.app.videorecorder;

import android.app.Application;

/**
 * Created by emp118 on 11/29/2016.
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
