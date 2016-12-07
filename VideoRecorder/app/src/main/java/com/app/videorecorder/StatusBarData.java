package com.app.videorecorder;

import android.content.Context;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by emp118 on 11/29/2016.
 */

public class StatusBarData{

    public int incrementMultiplier;

    public boolean isFinished;
    public boolean markLastSegment;
    public boolean maskWhiteSegment;

    public float stopX;

    public long timeRemaining;

    public Float lastItem;

    public ArrayList<Long> timeRemainingList = new ArrayList<>();
    public ArrayList<Float> verticalLinePositions = new ArrayList<>();

}
