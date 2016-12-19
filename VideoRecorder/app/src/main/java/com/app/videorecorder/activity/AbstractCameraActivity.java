package com.app.videorecorder.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;

import com.app.videorecorder.AppConstants;
import com.app.videorecorder.MainApplication;
import com.app.videorecorder.R;
import com.app.videorecorder.camera.CustomCamera;

import java.util.ArrayList;

/**
 * Created by Ankur Parashar
 */
public class AbstractCameraActivity extends BaseActivity {

    private static final int FRONT_CAMERA_REQUEST_CODE = 4574;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDialogStyle(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        if (requestCode == FRONT_CAMERA_REQUEST_CODE) {

            if (statusBarIndicator != null) {
                statusBarIndicator.resetEverything();
                statusBarIndicator.setData(MainApplication.getStatusBarData());
            }

            if (data.getExtras().containsKey(AppConstants.VIDEO_LIST)) {
                ArrayList<String> list = data.getExtras().getStringArrayList(AppConstants.VIDEO_LIST);
                ArrayList<String> listAudio = data.getExtras().getStringArrayList(AppConstants.AUDIO_LIST);
                videoList.clear();
                audioList.clear();
                videoList.addAll(list);
                audioList.addAll(listAudio);
                return;
            }

        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.flash_icon:
                handleFlashOnBtn();
                break;

            case R.id.cancel:
                statusBarIndicator.removeLastSegment();
                break;

            case R.id.close:
                closeActivity();
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

    protected void switchCamera() {
        MainApplication.setStatusBarData(statusBarIndicator.collectData());
        Intent intent = new Intent(this,FrontAbstractCameraActivity.class);
        intent.putStringArrayListExtra(AppConstants.VIDEO_LIST, videoList);
        intent.putStringArrayListExtra(AppConstants.AUDIO_LIST, audioList);
        startActivityForResult(intent, FRONT_CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeActivity();
        if(mediaRecorder!=null)
           mediaRecorder.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void handleFlashOnBtn() {

        getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        Camera.Parameters p = mCamera.getCamera().getParameters();

        if (p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mFlashButton.setText(R.string.flash_on);
        }
        else {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlashButton.setText(R.string.flash_off);
        }
        mCamera.getCamera().setParameters(p);

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        mCamera.openCamera(this, CustomCamera.BACK);
        setmGLView();

    }

}
