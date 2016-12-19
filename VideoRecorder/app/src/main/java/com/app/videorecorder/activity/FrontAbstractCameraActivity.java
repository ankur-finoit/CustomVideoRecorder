package com.app.videorecorder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.app.videorecorder.AppConstants;
import com.app.videorecorder.MainApplication;
import com.app.videorecorder.R;
import com.app.videorecorder.activity.BaseActivity;
import com.app.videorecorder.camera.CustomCamera;

/**
 * Created by Ankur Parashar
 */
public class FrontAbstractCameraActivity extends BaseActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoList.clear();
        audioList.clear();
        videoList.addAll(getIntent().getStringArrayListExtra(AppConstants.VIDEO_LIST));
        audioList.addAll(getIntent().getStringArrayListExtra(AppConstants.AUDIO_LIST));
        statusBarIndicator.setData(MainApplication.getStatusBarData());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.cancel:
                statusBarIndicator.removeLastSegment();
                break;

            case R.id.close:
                onBackPressed();
                break;

            case R.id.timer:
                displayPopupWindow(view);
                break;

            case R.id.check:
                saveImageOrVideoSegments();
                break;

            case R.id.stop_capture:
                mRecordButton.setEnabled(true);
                findViewById(R.id.stop_capture).setVisibility(View.GONE);
                startStopRecording();
                break;

            case R.id.face_camera:
                switchCamera();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void switchCamera() {
        MainApplication.setStatusBarData(statusBarIndicator.collectData());
        Intent intent = new Intent();
        intent.putStringArrayListExtra(AppConstants.VIDEO_LIST, videoList);
        intent.putStringArrayListExtra(AppConstants.AUDIO_LIST, audioList);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (statusBarIndicator != null) {
            statusBarIndicator.resetEverything();
            statusBarIndicator = null;
        }

        mCameraHandler.invalidateHandler();     // paranoia
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.openCamera(this, CustomCamera.FRONT);
        setmGLView();
    }

}
