package com.app.videorecorder.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;

import com.app.videorecorder.Utils.CameraUtils;
import com.app.videorecorder.video.TextureMovieEncoder;

/**
 * Created by 'Ankur Parashar' on 12/15/2016.
 */
public class CustomCamera  {

    private Camera mCamera;
    public static int faceofCamera;

    protected boolean mRecordingEnabled;

    protected int mCameraPreviewWidth, mCameraPreviewHeight;

    protected static TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();

    public static final int FRONT = 0x01;
    public static final int BACK = 0x02;

    public Camera getCamera(){
        return mCamera;
    }

    public int getmCameraPreviewWidth(){
        return mCameraPreviewWidth;
    }

    public int getmCameraPreviewHeight(){
        return mCameraPreviewHeight;
    }

    public void init() {

        mRecordingEnabled = sVideoEncoder.isRecording();
    }

    public void releaseBackCamera() {

        if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
        }

    }

    public  void openCamera(Activity activity, int face) {

        faceofCamera=face;
        int width = activity.getResources().getDisplayMetrics().widthPixels;
        int height = activity.getResources().getDisplayMetrics().heightPixels;

        if(face == BACK)
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        else
        {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }


        if (mCamera == null) {
                Log.d("CC", "No front-facing camera found; opening default");
                mCamera = Camera.open();    // opens first back-facing camera
        }

        if (mCamera == null) {
                throw new RuntimeException("Unable to open camera");
        }

        int rotate = CameraUtils.getCameraOrientation(activity, Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera.setDisplayOrientation(rotate);

        Camera.Parameters params = mCamera.getParameters();

        params.setPictureSize(params.getPreviewSize().width, params.getPreviewSize().height);

        final Camera.Size result = CameraUtils.getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), width, height);

        // params.setPreviewSize(result.width, result.height);
        /*params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
          params.setRecordingHint(true);*/

        mCamera.setParameters(params);

        int[] fpsRange = new int[2];
        Camera.Size mCameraPreviewSize = params.getPreviewSize();
        params.getPreviewFpsRange(fpsRange);
        mCameraPreviewWidth = mCameraPreviewSize.width;
        mCameraPreviewHeight = mCameraPreviewSize.height;

    }

}
