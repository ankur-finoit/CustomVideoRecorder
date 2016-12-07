package com.app.videorecorder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.opengl.GLES20;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.videorecorder.StatusBarIndicator.StatusBarIndicatorListener;
import com.app.videorecorder.VideoParserAsyncTask.OnVideoParserAsyncTask;
import com.app.videorecorder.video.AspectFrameLayout;
import com.app.videorecorder.video.CameraSurfaceRenderer;
import com.app.videorecorder.video.TextureMovieEncoder;
import com.splunk.mint.Mint;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by BRKA on 4.4.2016..
 */
public class AbstractCameraActivity extends AppCompatActivity implements OnClickListener, OnTouchListener,
        StatusBarIndicatorListener, OnVideoParserAsyncTask, SurfaceTexture.OnFrameAvailableListener {

    private ProgressDialog mProgressDialog;
    private static final int FRONT_CAMERA_REQUEST_CODe = 4574;

    private static final String TAG = AbstractCameraActivity.class.getName();
    private static final boolean VERBOSE = false;

    private GLSurfaceView mGLView;
    private CameraSurfaceRenderer mRenderer;
    private Camera mCamera;
    private AbstractCameraActivity.BackCameraHandler mCameraHandler;
    private boolean mRecordingEnabled;
    private int mCameraPreviewWidth, mCameraPreviewHeight;
    private static TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();

    private ArrayList<String> videoList = new ArrayList<>();
    private ArrayList<String> audioList = new ArrayList<>();
    private CountDownTimer countDownTimer;//, countDownTimerFullSegment;
    private PopupWindow popup;

    protected TextView mFlashButton;
    protected ImageView mSwitchCameraButton;
    protected ImageView mCloseButton;
    protected ImageView mDoneButton;
    protected ImageView mRecordButton;
    protected ImageView mTimerButton;
    protected ImageView mDeleteButton;
    protected StatusBarIndicator statusBarIndicator;
    protected TextView mCounter;

    private File mCurrentFile;
    private File mCurrentAudioFile;
    private MediaRecorder mediaRecorder;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        if (requestCode == FRONT_CAMERA_REQUEST_CODe) {

            if (data.getExtras().containsKey("video_file")) {
                videoList.clear();
                audioList.clear();
                onVideoMergingFinished(data.getExtras().getString("video_file"), data.getExtras().getString("gif_file"));
                return;
            }

            if (data.getExtras().containsKey("video_list")) {

                ArrayList<String> list = data.getExtras().getStringArrayList("video_list");
                ArrayList<String> listAudio = data.getExtras().getStringArrayList("audio_list");
                videoList.clear();
                audioList.clear();
                if (statusBarIndicator != null)
                    statusBarIndicator.setData(MainApplication.getStatusBarData());
                videoList.addAll(list);
                audioList.addAll(listAudio);
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(this, "9b731153");
        hideSystemUI();
        audioList = new ArrayList<>();
        videoList = new ArrayList<>();
        setContentView(R.layout.activity_take_photo);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prepareUI();
        initBackCamera();
    }

    private void makeTempFile(boolean video) {
        File myDir = new File("/sdcard/mov_video");
        myDir.mkdirs();
        try {
            if (video)
                mCurrentFile = File.createTempFile("mov_video", ".mp4", myDir);
            else
                mCurrentAudioFile = File.createTempFile("mov_audio", ".mp4", myDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAudioRecording() {

        makeTempFile(false);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(mCurrentAudioFile.getAbsolutePath());

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAudioRecording() {
        mediaRecorder.stop();
    }

    private void initBackCamera() {

        // Define a handler that receives camera-control messages from other threads.  All calls
        // to Camera must be made on the same thread.  Note we create this before the renderer
        // thread, so we know the fully-constructed object will be visible.
        mCameraHandler = new BackCameraHandler(this);

        mRecordingEnabled = sVideoEncoder.isRecording();

        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL context.
        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        mRenderer = new CameraSurfaceRenderer(mCameraHandler, sVideoEncoder);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    private void openCameraBack() {

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera == null) {
            Log.d(TAG, "No front-facing camera found; opening default");
            mCamera = Camera.open();    // opens first back-facing camera
        }
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        int rotate = getCameraOrientation(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera.setDisplayOrientation(rotate);

        Camera.Parameters params = mCamera.getParameters();

        params.setPictureSize(params.getPreviewSize().width, params.getPreviewSize().height);

        final Camera.Size result = getOptimalPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), width, height);

       // params.setPreviewSize(result.width, result.height);
        /*params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setRecordingHint(true);
*/
        mCamera.setParameters(params);

        int[] fpsRange = new int[2];
        Camera.Size mCameraPreviewSize = params.getPreviewSize();
        params.getPreviewFpsRange(fpsRange);
        mCameraPreviewWidth = mCameraPreviewSize.width;
        mCameraPreviewHeight = mCameraPreviewSize.height;

        AspectFrameLayout layout = (AspectFrameLayout) findViewById(R.id.aspact_frame_layout);
        double targetAspect = (double) mCameraPreviewWidth / mCameraPreviewHeight;
        layout.setAspectRatio(targetAspect);


        mGLView.onResume();
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setCameraPreviewSize(result.width, result.height);
            }
        });
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

    private void startStopRecording() {
        mRecordingEnabled = !mRecordingEnabled;
        if (mRecordingEnabled) {
            makeTempFile(true);
            mRenderer.setOutputFile(mCurrentFile);
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
        } else {
            stopAudioRecording();
            stopVideoStreamRecording();
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

    private void stopVideoStreamRecording() {
        mRecordButton.setImageResource(R.drawable.ic_camera_norma);

        mCloseButton.setEnabled(true);
        mDeleteButton.setEnabled(true);
        mTimerButton.setEnabled(true);
        mSwitchCameraButton.setEnabled(true);
        statusBarIndicator.maskWhiteSegment = true;
        statusBarIndicator.stopRecording();
        videoList.add(mCurrentFile.getAbsolutePath());
        audioList.add(mCurrentAudioFile.getAbsolutePath());
    }

    private void startVideoStreamRecording() {
        mRecordButton.setImageResource(R.drawable.ic_camera_orange);
        mCloseButton.setEnabled(false);
        mDeleteButton.setEnabled(false);
        mTimerButton.setEnabled(false);
        mSwitchCameraButton.setEnabled(false);
        statusBarIndicator.startRecording();
    }

    private void saveImageOrVideoSegments() {
        new VideoParserAsyncTask(this, videoList, audioList).execute();
    }

    @Override
    public void onVideoMergingStart() {
        showProgressDialog();
    }

    @Override
    public void onVideoMergingFinished(String pathToMergedVideo, String pathToCreatedGif) {
        hideProgressDialog();
    }

    @Override
    public void onSegmentDeleted(int index, int size) {
        mDoneButton.setVisibility(View.INVISIBLE);
        mDeleteButton.setVisibility(size == 0 ? View.INVISIBLE : View.VISIBLE);
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

    private void recordPredefinedSegment(long time) {
        mTimerButton.setEnabled(false);
        mRecordButton.setEnabled(false);

        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsUntilVideo = (int) millisUntilFinished / 1000;
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Camera.Parameters p = mCamera.getParameters();
                    if (p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    } else {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                    mCamera.setParameters(p);
                }
                mCounter.setText(Integer.toString(secondsUntilVideo));
                mCounter.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                findViewById(R.id.stop_capture).setVisibility(View.VISIBLE);
                mCounter.setVisibility(View.INVISIBLE);

                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Camera.Parameters p = mCamera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(p);
                }
                startStopRecording();
            }
        };
        countDownTimer.start();
    }

    protected void switchCamera() {

        MainApplication.setStatusBarData(statusBarIndicator.collectData());
        Intent intent = new Intent(this, FrontAbstractCameraActivity.class);
        intent.putStringArrayListExtra("video_list", videoList);
        intent.putStringArrayListExtra("audio_list", audioList);
        startActivityForResult(intent, FRONT_CAMERA_REQUEST_CODe);
    }

    private void closeActivity() {
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
        releaseBackCamera();
        mCameraHandler.invalidateHandler();     // paranoia
        finish();
    }

    private void releaseBackCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeActivity();
        //mediaRecorder.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity();
    }

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

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
            mSwitchCameraButton.setVisibility(View.GONE);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            mFlashButton.setVisibility(View.GONE);

        statusBarIndicator.setListener(this);
        showDialogStyle(1);
    }

    private void showDialogStyle(int step) {
        final Dialog dialog;
        if (step == 1) {
            dialog = new Dialog(this, R.style.CustomDialogTheme1);
            dialog.setContentView(R.layout.dialog_video_step_guid_1);
            dialog.setCanceledOnTouchOutside(false);

            dialog.findViewById(R.id.btn_send_offer).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    showDialogStyle(2);
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

        } else if (step == 2) {

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
        }
    }

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

    private void displayPopupWindow(View anchorView) {
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

    private void handleFlashOnBtn() {
        getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        Camera.Parameters p = mCamera.getParameters();

        if (p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mFlashButton.setText(R.string.flash_on);
        } else {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mFlashButton.setText(R.string.flash_off);
        }
        mCamera.setParameters(p);
    }

    public static class BackCameraHandler extends Handler {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;
        public static final int MSG_CHANGE_SURFACE_TEXTURE = 1;

        private WeakReference<AbstractCameraActivity> mWeakActivity;

        public BackCameraHandler(AbstractCameraActivity activity) {
            mWeakActivity = new WeakReference<AbstractCameraActivity>(activity);
        }

        public void invalidateHandler() {
            mWeakActivity.clear();
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Log.d(TAG, "CameraHandler [" + this + "]: what=" + what);

            AbstractCameraActivity activity = mWeakActivity.get();
            if (activity == null) {
                Log.w(TAG, "CameraHandler.handleMessage: activity is null");
                return;
            }

            switch (what) {
                case MSG_SET_SURFACE_TEXTURE:
                    activity.handleSetSurfaceTextureBack((SurfaceTexture) inputMessage.obj);
                    break;

                case MSG_CHANGE_SURFACE_TEXTURE:
                    break;

                default:
                    throw new RuntimeException("unknown msg " + what);
            }
        }
    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    private void handleSetSurfaceTextureBack(SurfaceTexture st) {
        st.setOnFrameAvailableListener(this);
        try {
            mCamera.setPreviewTexture(st);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        mCamera.startPreview();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        if (VERBOSE) Log.d(TAG, "ST onFrameAvailable");
        mGLView.requestRender();
    }

    protected int getCameraOrientation(int cameraFacing) {
        //set preview orientation
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraFacing, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; //Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; //Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;//Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;//Landscape right
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        openCameraBack();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause -- releasing camera");
        super.onPause();
        releaseBackCamera();
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

    /**
     * Method to show Progress Dialog when requesting server
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.setMessage("Merging Video");
        mProgressDialog.show();

    }

    /**
     * Method to hide progress dialog when received response from API
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

}
