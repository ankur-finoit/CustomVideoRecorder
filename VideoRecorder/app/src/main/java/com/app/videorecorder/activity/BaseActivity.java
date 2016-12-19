package com.app.videorecorder.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.videorecorder.AppConstants;
import com.app.videorecorder.R;
import com.app.videorecorder.task.VideoParserAsyncTask;
import com.app.videorecorder.camera.CustomCamera;
import com.app.videorecorder.Utils.FileUtils;
import com.app.videorecorder.video.AspectFrameLayout;
import com.app.videorecorder.video.CameraSurfaceRenderer;
import com.app.videorecorder.video.PreviewVideoActivity;
import com.app.videorecorder.video.TextureMovieEncoder;
import com.app.videorecorder.view.StatusBarIndicator;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by 'Ankur Parashar' on 12/15/2016.
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,
        StatusBarIndicator.StatusBarIndicatorListener, VideoParserAsyncTask.OnVideoParserAsyncTask, SurfaceTexture.OnFrameAvailableListener {

    public static final String TAG = "CAMERA";
    public static final String UNKNOWN_TAG = "unknown msg";
    public static final String CAMERA_HANDLER_MSG = "CameraHandler.handleMessage: activity is null";

    protected CustomCamera mCamera;

    protected GLSurfaceView mGLView;
    protected CameraSurfaceRenderer mRenderer;

    protected BackCameraHandler mCameraHandler;

    protected boolean mRecordingEnabled;

    protected static TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();

    protected ProgressDialog mProgressDialog;

    protected PopupWindow popup;

    protected TextView mFlashButton;
    protected ImageView mSwitchCameraButton;
    protected ImageView mCloseButton;
    protected ImageView mDoneButton;
    protected ImageView mRecordButton;
    protected ImageView mTimerButton;
    protected ImageView mDeleteButton;
    protected StatusBarIndicator statusBarIndicator;
    protected TextView mCounter;

    private CountDownTimer countDownTimer;
    private FileUtils fileUtils;

    protected ArrayList<String> videoList;
    protected ArrayList<String> audioList;
    protected MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        hideSystemUI();

        videoList = new ArrayList<>();
        audioList = new ArrayList<>();

        setContentView(R.layout.activity_take_photo);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prepareUI();
        fileUtils= new FileUtils();
        mCamera = new CustomCamera();
        // Define a handler that receives camera-control messages from other threads.  All calls
        // to Camera must be made on the same thread.  Note we create this before the renderer
        // thread, so we know the fully-constructed object will be visible.
        mCameraHandler = new BackCameraHandler(this);
        mCamera.init();
        setmCamera();
        initGl();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause -- releasing camera");
        super.onPause();
        mCamera.releaseBackCamera();
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                // Tell the renderer that it's about to be paused so it can clean up.
                mRenderer.notifyPausing();
            }
        });
        mGLView.onPause();
        Log.d(TAG, "onPause complete");
    }


    /*Set all the UI views */
    private void prepareUI() {
        mFlashButton = (TextView) findViewById(R.id.flash_icon);
        mCloseButton = (ImageView) findViewById(R.id.close);
        mDeleteButton = (ImageView) findViewById(R.id.cancel);

        mTimerButton = (ImageView) findViewById(R.id.timer);
        mRecordButton = (ImageView) findViewById(R.id.capture);
        mSwitchCameraButton = (ImageView) findViewById(R.id.face_camera);
        mDoneButton = (ImageView) findViewById(R.id.check);

        statusBarIndicator = (StatusBarIndicator) findViewById(R.id.statusBarIndicator);
        mCounter = (TextView) findViewById(R.id.counter);
        findViewById(R.id.stop_capture).setOnClickListener(this);

        mFlashButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);

        mTimerButton.setOnClickListener(this);
        mRecordButton.setOnTouchListener(this);
        mSwitchCameraButton.setOnClickListener(this);
        mDoneButton.setOnClickListener(this);
        statusBarIndicator.setListener(this);

    }

    /*Use this method to intialize the camera's GLSurface View*/
    protected void initGl(){
        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        mRenderer = new CameraSurfaceRenderer(mCameraHandler, sVideoEncoder);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /*This method is used to set the aspect ratio of camera and is also used to start the recording of frames*/
    protected void setmGLView(){

        AspectFrameLayout layout = (AspectFrameLayout) findViewById(R.id.aspact_frame_layout);
        double targetAspect = (double) mCamera.getmCameraPreviewWidth() / mCamera.getmCameraPreviewHeight();
        layout.setAspectRatio(targetAspect);

        mGLView.onResume();
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setCameraPreviewSize(mCamera.getmCameraPreviewHeight(), mCamera.getmCameraPreviewWidth());
            }
        });

    }

    protected void setmCamera(){

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
                    mSwitchCameraButton.setVisibility(View.GONE);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
                    mFlashButton.setVisibility(View.GONE);

    }

    /*This function is used to show the dialog box at the starting of camera*/
    public void showDialogStyle(int step) {

        final Dialog dialog;
        if (step == 1) {
            dialog = new Dialog(this, R.style.CustomDialogTheme1);
            dialog.setContentView(R.layout.dialog_video_step_guid_1);
            dialog.setCanceledOnTouchOutside(false);

            dialog.findViewById(R.id.btn_send_offer).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                   // showDialogStyle(2);
                    hideRecordVideoView();
                    hideSystemUI();
                }
            });

            dialog.show();

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    hideRecordVideoView();
                }
            });

        }
        /*else if (step == 2) {

            dialog = new Dialog(this, R.style.CustomDialogTheme2);
            dialog.setContentView(R.layout.dialog_video_step_guid_2);
            dialog.setCanceledOnTouchOutside(false);

            dialog.findViewById(R.id.btn_send_offer).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    hideRecordVideoView();
                    hideSystemUI();
                }
            });

            dialog.show();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    hideRecordVideoView();
                }
            });

        }*/

    }

    /*This method is used to show the fading text on the start of the camera*/
    private void hideRecordVideoView() {

        final TextView recordVideo = (TextView) findViewById(R.id.record_video);
        recordVideo.setVisibility(View.VISIBLE);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(recordVideo, "alpha", 1f, .2f);
        fadeOut.setDuration(2000);

        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.playSequentially(fadeOut);
        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                recordVideo.setVisibility(View.GONE);
            }
        });
        mAnimationSet.start();

    }



    /**
     * Method to show progress dialog
     */
    public void showProgressDialog() {

        if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCanceledOnTouchOutside(false);
        }
                mProgressDialog.setMessage(getString(R.string.merging));
                mProgressDialog.show();

    }

    /**
     * Method to hide progress dialog
     */
    public void hideProgressDialog() {

        if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();

    }


    /*This method is used to start the recording automatically by click the timer*/
    private void recordPredefinedSegment(long time) {

        mTimerButton.setEnabled(false);
        mRecordButton.setEnabled(false);

        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsUntilVideo = (int) millisUntilFinished / 1000;
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Camera.Parameters p = mCamera.getCamera().getParameters();
                    if(CustomCamera.BACK==Camera.CameraInfo.CAMERA_FACING_BACK)
                    {
                        if (p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        } else {
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        }
                    }
                    mCamera.getCamera().setParameters(p);
                }
                mCounter.setText(Integer.toString(secondsUntilVideo));
                mCounter.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                findViewById(R.id.stop_capture).setVisibility(View.VISIBLE);
                mCounter.setVisibility(View.INVISIBLE);

                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                        Camera.Parameters p = mCamera.getCamera().getParameters();
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.getCamera().setParameters(p);
                }
                startStopRecording();
            }
        };
        countDownTimer.start();

    }

/*Used to start the audio recording of captured Video*/
    protected void startAudioRecording() {
        fileUtils.makeTempFile(false);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(fileUtils.mCurrentAudioFile.getAbsolutePath());

        try {
                mediaRecorder.prepare();
                mediaRecorder.start();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*Stop the mediaRecorder used for audio recording*/
    protected void stopAudioRecording() {

        try {
                mediaRecorder.stop();
                mediaRecorder.reset();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*Set all the views after the recoding gets stopped also store the path of the video and audio*/
    protected void stopVideoStreamRecording() {
        mRecordButton.setImageResource(R.drawable.ic_camera_norma);
        mCloseButton.setEnabled(true);
        mDeleteButton.setEnabled(true);
        mTimerButton.setEnabled(true);
        mSwitchCameraButton.setEnabled(true);
        statusBarIndicator.maskWhiteSegment = true;
        statusBarIndicator.stopRecording();
        videoList.add(fileUtils.mCurrentFile.getAbsolutePath());
        audioList.add(fileUtils.mCurrentAudioFile.getAbsolutePath());
    }

    /*Enable all the views while the recording starts*/
    protected void startVideoStreamRecording() {
        mRecordButton.setImageResource(R.drawable.ic_camera_orange);
        mCloseButton.setEnabled(false);
        mDeleteButton.setEnabled(false);
        mTimerButton.setEnabled(false);
        mSwitchCameraButton.setEnabled(false);
        statusBarIndicator.startRecording();
    }

    /*Used to start and stop audio as well as video recording*/
    protected void startStopRecording() {

        mRecordingEnabled = !mRecordingEnabled;
        if (mRecordingEnabled) {
                fileUtils.makeTempFile(true);
                mRenderer.setOutputFile(fileUtils.mCurrentFile);
        }
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(mRecordingEnabled);
            }
        });

        if (mRecordingEnabled) {
                startAudioRecording();
                startVideoStreamRecording();
        }
        else {
                stopAudioRecording();
                stopVideoStreamRecording();
        }

    }

    /*Save the Videos and audios made while recording*/
    protected void saveImageOrVideoSegments() {
        new VideoParserAsyncTask(this, videoList, audioList).execute();
    }

/*Popup window which is displayed by clicking on the timer icon*/
    protected void displayPopupWindow(View anchorView) {
        popup = new PopupWindow(this);
        View layout = getLayoutInflater().inflate(R.layout.popup_content, null);
        popup.setContentView(layout);
        TextView fiveSecTimer = (TextView) layout.findViewById(R.id.five_sec_item);
        TextView tenSecTimer = (TextView) layout.findViewById(R.id.ten_sec_item);
        fiveSecTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordPredefinedSegment(5200);
                popup.dismiss();
                hideSystemUI();
            }
        });
        tenSecTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordPredefinedSegment(10200);
                popup.dismiss();
                hideSystemUI();
            }
        });
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        // popup.showAsDropDown(anchorView);
        int[] coords = {0, 0};
        anchorView.getLocationOnScreen(coords);
        RelativeLayout.LayoutParams lp =
                (RelativeLayout.LayoutParams) anchorView.getLayoutParams();
        int x = anchorView.getWidth() + lp.leftMargin;
        int y = coords[1] + (anchorView.getHeight() / 7);
        popup.showAtLocation(layout, 0, x, y);

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                hideSystemUI();
            }
        });
    }

    /*This method is used to hide all the system UI like status bar*/
    public void hideSystemUI() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLView.requestRender();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == mRecordButton) {
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    startStopRecording();
                    break;
            }
        }
        return true;
    }

    /*Used to delete a particular segment while recording*/
    @Override
    public void onSegmentDeleted(int index, int verticalLineSize) {

        mDoneButton.setVisibility(View.INVISIBLE);
        mDeleteButton.setVisibility(index == 0 ? View.INVISIBLE : View.VISIBLE);
        mFlashButton.setEnabled(true);
        mTimerButton.setEnabled(true);
        mRecordButton.setEnabled(true);
        mSwitchCameraButton.setEnabled(true);

        if (videoList.size() > 0) {
                index = videoList.size() - 1;
                File file = new File(videoList.get(index));
                file.delete();
                videoList.remove(index);
        }

        if (audioList.size() > 0) {
                index = audioList.size() - 1;
                File file = new File(audioList.get(index));
                file.delete();
                audioList.remove(index);
        }

    }

    @Override
    public void showCancelButton(boolean show) {
        mDeleteButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void showSaveSegment() {

        startStopRecording();

        mDeleteButton.setEnabled(false);
        mFlashButton.setEnabled(false);
        mTimerButton.setEnabled(false);
        mRecordButton.setEnabled(false);
        mSwitchCameraButton.setEnabled(false);
        mDoneButton.setVisibility(View.VISIBLE);
        mRecordButton.setImageResource(R.drawable.ic_camera_norma);
        mDeleteButton.setEnabled(true);

    }

    @Override
    public void onVideoMergingStart() {
        showProgressDialog();
    }

    @Override
    public void onVideoMergingFinished(String pathToMergedVideo) {
        hideProgressDialog();
        Intent intent = new Intent(this, PreviewVideoActivity.class);
        intent.putExtra(AppConstants.VIDEO_PATH, pathToMergedVideo);
        startActivity(intent);
    }

    protected void closeActivity() {
        Iterator<String> iter = videoList.iterator();
        while (iter.hasNext()) {
            String str = iter.next();
            File file = new File(str);
            file.delete();
            iter.remove();
        }
        Iterator<String> audio = audioList.iterator();
        while (audio.hasNext()) {
            String str = audio.next();
            File file = new File(str);
            file.delete();
            audio.remove();
        }
        if (statusBarIndicator != null) {
            statusBarIndicator.resetEverything();
            statusBarIndicator = null;
        }
        mCamera.releaseBackCamera();
        mCameraHandler.invalidateHandler();
        finish();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity();
    }


     public class BackCameraHandler extends Handler {

        public static final int MSG_SET_SURFACE_TEXTURE = 0;
        public static final int MSG_CHANGE_SURFACE_TEXTURE = 1;

        private WeakReference<Activity> mWeakActivity;

        public BackCameraHandler(Activity activity) {
            mWeakActivity = new WeakReference<Activity>(activity);
        }

        public void invalidateHandler() {
            mWeakActivity.clear();
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Log.d("CC", "CameraHandler [" + this + "]: what=" + what);

            Activity activity = mWeakActivity.get();
            if (activity == null) {
                Log.w("CC", CAMERA_HANDLER_MSG);
                return;
            }

            switch (what) {

                case MSG_SET_SURFACE_TEXTURE:
                    handleSetSurfaceTextureBack((SurfaceTexture)inputMessage.obj);
                    break;

                case MSG_CHANGE_SURFACE_TEXTURE:
                    break;

                default:
                    throw new RuntimeException(UNKNOWN_TAG + what);

            }

        }

    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    private void handleSetSurfaceTextureBack(SurfaceTexture st)
    {
        st.setOnFrameAvailableListener(this);
        try {
            if (mCamera != null)
                mCamera.getCamera().setPreviewTexture(st);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        if (mCamera != null)
                mCamera.getCamera().startPreview();

    }

}
