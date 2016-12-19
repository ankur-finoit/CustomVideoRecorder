package com.app.videorecorder.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Ankur Parashar
 */
public class StatusBarIndicator extends View {

    public interface StatusBarIndicatorListener {
        void onSegmentDeleted(int index, int verticalLineSize);

        void showCancelButton(boolean show);

        void showSaveSegment();
    }

    public static final long VIDEO_DURATION = 15000;

    private CountDownTimer countDownTimer;
    private int incrementMultiplier;
    private ArrayList<Float> verticalLinePositions = new ArrayList<>();
    private ArrayList<Float> horizontalLinePositions= new ArrayList<>();
    private long timeRemaining = VIDEO_DURATION;
    public Paint horizontalPaint, verticalPaint;
    public boolean isFinished;
    private float stopX;
    private boolean markLastSegment;
    private Float lastItem;
    private ArrayList<Long> timeRemainingList = new ArrayList<>();
    public boolean maskWhiteSegment;
    private StatusBarIndicatorListener listener;
    private Context context;

    public StatusBarIndicator(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public StatusBarIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public StatusBarIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusBarIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {
        horizontalPaint = new Paint();
        horizontalPaint.setColor(Color.WHITE);
        horizontalPaint.setStrokeWidth(convertDpToPx(getContext(), 10));

        verticalPaint = new Paint();
        verticalPaint.setColor(Color.BLACK);
        verticalPaint.setStrokeWidth(3);
    }

    private float convertDpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public StatusBarData collectData(){

        StatusBarData data = new StatusBarData();
        data.incrementMultiplier = incrementMultiplier;
        data.isFinished = isFinished;
        data.markLastSegment = markLastSegment;
        data.maskWhiteSegment = maskWhiteSegment;
        data.stopX = stopX;
        data.timeRemaining = timeRemaining;
        data.lastItem = lastItem;
        data.timeRemainingList.addAll(timeRemainingList);
        data.verticalLinePositions.addAll(verticalLinePositions);
        return data;
    }

    public void setData( StatusBarData data)
    {
        incrementMultiplier = data.incrementMultiplier;
        isFinished = data.isFinished;
        markLastSegment = data.markLastSegment;
        maskWhiteSegment = data.maskWhiteSegment;
        stopX = data.stopX;
        timeRemaining = data.timeRemaining;
        lastItem = data.lastItem;
        timeRemainingList.addAll(data.timeRemainingList);
        verticalLinePositions.addAll(data.verticalLinePositions);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        horizontalPaint.setColor(Color.BLACK);
        canvas.drawLine(0, 0, getWidth(), 0, horizontalPaint);
        float increment = (getWidth() / 150f) + 0.08f;          //7.2799997

        if (!isFinished) {
            stopX = increment * incrementMultiplier;                // 0.0 - 1077.44
        } else {
            stopX = getWidth();
        }

        horizontalPaint.setColor(Color.WHITE);
        canvas.drawLine(0, 0, stopX, 0, horizontalPaint);

        for(int i=0; i<verticalLinePositions.size(); i++){
            canvas.drawLine(verticalLinePositions.get(i), 0, verticalLinePositions.get(i), convertDpToPx(getContext(), 10)/2, verticalPaint);
        }

        if(timeRemainingList.size()>=2)
        {
            if (timeRemainingList.get(timeRemainingList.size() - 2) - timeRemainingList.get(timeRemainingList.size() - 1) >=2000) {
                horizontalPaint.setColor(Color.WHITE);
                canvas.drawLine(verticalLinePositions.get(verticalLinePositions.size() - 2), 0,verticalLinePositions.get(verticalLinePositions.size() - 1), 0, horizontalPaint);
                listener.showCancelButton(true);
            }
            else {
                horizontalPaint.setColor(Color.BLACK);
                canvas.drawLine(verticalLinePositions.get(verticalLinePositions.size() - 2), 0, verticalLinePositions.get(verticalLinePositions.size() - 1), 0, horizontalPaint);
            }
        }

        if (markLastSegment) {
            markLastSegment = false;
            float lastX = 0;
            if (verticalLinePositions.size() > 0) {
                lastX = verticalLinePositions.get(verticalLinePositions.size() - 1);
            }
            float penultimateX;
            try {
                penultimateX = verticalLinePositions.get(verticalLinePositions.size() - 2);
            } catch (Exception e) {
                penultimateX = 0;
            }
            horizontalPaint.setColor(Color.RED);
            canvas.drawLine(penultimateX, 0, lastX, 0, horizontalPaint);
            horizontalPaint.setColor(Color.BLACK);
            canvas.drawLine(verticalLinePositions.get(verticalLinePositions.size() - 1), 0, getWidth(), 0, horizontalPaint);
        }
    }

    public void startRecording() {
        lastItem = null;
        countDownTimer = new CountDownTimer(timeRemaining, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;                                    // 14995 - 189
                incrementMultiplier = (int) (VIDEO_DURATION - timeRemaining) / 100;    // 0 - 148
                invalidate();
            }

            @Override
            public void onFinish() {
                if (isFinished) {
                    return;
                }
                if (timeRemainingList.get(timeRemainingList.size() - 2) - timeRemainingList.get(timeRemainingList.size() - 1) >=2000) {
                    verticalLinePositions.add((float) getWidth());
                    timeRemainingList.add(0l);
                    isFinished = true;
                    listener.showSaveSegment();
                }

            }
        };
        countDownTimer.start();
    }

    public void stopRecording()
    {
        if (countDownTimer != null) {

            countDownTimer.cancel();
            if (verticalLinePositions.size() == 0) {

                verticalLinePositions.add(0f);
                timeRemainingList.add(VIDEO_DURATION);
                verticalLinePositions.add(stopX);
                timeRemainingList.add(timeRemaining);
            }
            else if (verticalLinePositions.get(verticalLinePositions.size() - 1) != stopX || stopX != getWidth()) {
                verticalLinePositions.add(stopX);
                timeRemainingList.add(timeRemaining);
            }

            if(timeRemainingList.get(timeRemainingList.size()-2)-timeRemainingList.get(timeRemainingList.size()-1)<2000 && timeRemainingList.get(timeRemainingList.size()-2)-timeRemainingList.get(timeRemainingList.size()-1)>0)
            {
                isFinished = false;
                int index = timeRemainingList.size() - 1;
                verticalLinePositions.remove(index);
                timeRemainingList.remove(index);
                timeRemaining = timeRemainingList.size() > 0 ? timeRemainingList.get(timeRemainingList.size() - 1) : VIDEO_DURATION;
                listener.onSegmentDeleted(index, timeRemainingList.size());
                if(verticalLinePositions.size()==1)
                {
                    resetEverything();
                }
            }

            if(isFinished)
            {
                int index = timeRemainingList.size() - 1;
                verticalLinePositions.remove(index);
                timeRemainingList.remove(index);
                timeRemaining = timeRemainingList.size() > 0 ? timeRemainingList.get(timeRemainingList.size() - 1) : VIDEO_DURATION;
                if(verticalLinePositions.size()==1)
                {
                    resetEverything();
                }
            }
            maskWhiteSegment = false;
            invalidate();
        }

        // listener.showCancelButton(true);
    }

    public void removeLastSegment() {

        if (lastItem == null) {
           // Toast.makeText(context, "delete last segment", Toast.LENGTH_SHORT).show();
            markLastSegment = true;
            if (verticalLinePositions.size() > 0) {
                lastItem = verticalLinePositions.get(verticalLinePositions.size() - 1);
            }
            invalidate();
        } else {
            isFinished = false;
            int index = verticalLinePositions.indexOf(lastItem);
            verticalLinePositions.remove(lastItem);
            timeRemainingList.remove(index);
            timeRemaining = timeRemainingList.size() > 0 ? timeRemainingList.get(timeRemainingList.size() - 1) : VIDEO_DURATION;
            incrementMultiplier = (int) (VIDEO_DURATION - timeRemaining) / 100;
            invalidate();
            lastItem = null;
            listener.onSegmentDeleted(index, timeRemainingList.size());
            if (verticalLinePositions.size() == 1) {
                resetEverything();
            }
        }
    }

    public void setListener(StatusBarIndicatorListener listener) {
        this.listener = listener;
    }

    public void resetEverything() {
        if (listener != null) {
            listener.showCancelButton(false);
        }
        isFinished = false;
        incrementMultiplier = 0;
        verticalLinePositions.clear();
        timeRemainingList.clear();
        isFinished = false;
        stopX = 0f;
        markLastSegment = false;
        lastItem = 0f;
        countDownTimer = null;
        timeRemaining = VIDEO_DURATION;
        maskWhiteSegment = false;
    }
}
