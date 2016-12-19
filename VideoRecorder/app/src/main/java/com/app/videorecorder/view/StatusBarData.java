package com.app.videorecorder.view;

import java.util.ArrayList;

/**
 * Created by Ankur Parashar on 11/29/2016.
 */
public class StatusBarData {

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
