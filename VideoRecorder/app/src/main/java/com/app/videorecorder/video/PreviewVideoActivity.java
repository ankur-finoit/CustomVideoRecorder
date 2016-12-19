package com.app.videorecorder.video;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.videorecorder.activity.AbstractCameraActivity;
import com.app.videorecorder.AppConstants;
import com.app.videorecorder.R;


/**
 * Created by Siddharth Singh on 8/2/2016.
 */

/* This activity is used to show the merged Video*/
public class PreviewVideoActivity extends AppCompatActivity {

    private SurfaceVideoView mPreviewVideoTextureVideoView;
    private String pathVideo;
    private String pathtoGif;
    private ImageButton mPlayImageButton;
    private Button mStartAgain;
    private LinearLayout mPlayBtnContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview_video);

        //initToolbar("Delivery Options",true);
        pathVideo= (String) getIntent().getCharSequenceExtra(AppConstants.VIDEO_PATH);
        pathtoGif= (String) getIntent().getCharSequenceExtra(AppConstants.GIF_PATH);
        initViews();
        setVisibility(pathVideo);


        mStartAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(PreviewVideoActivity.this, AbstractCameraActivity.class);
                startActivity(intent);

            }
        });
        mPlayBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreviewVideoTextureVideoView.isPlaying()) {
                    mPreviewVideoTextureVideoView.pause();
                    mPlayImageButton.setVisibility(ImageButton.VISIBLE);
                } else {
                    mPreviewVideoTextureVideoView.start();
                    mPlayImageButton.setVisibility(ImageButton.GONE);
                }
            }
        });

        mPreviewVideoTextureVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayImageButton.setVisibility(ImageButton.VISIBLE);
            }
        });

    }


    public void initViews()
    {
        mPreviewVideoTextureVideoView = (SurfaceVideoView) findViewById(R.id.video);
        mPlayImageButton = (ImageButton) findViewById(R.id.play_buttn);
        mPlayBtnContainer= (LinearLayout) findViewById(R.id.play_btn_container);
        mStartAgain= (Button) findViewById(R.id.start_camera);

    }

    public void setVisibility(String path)
    {
        if(path==null)
        {
            mPlayBtnContainer.setVisibility(View.VISIBLE);
            mPreviewVideoTextureVideoView.setVisibility(View.GONE);
            Toast.makeText(this,getString(R.string.video_not_merged_properly),Toast.LENGTH_LONG).show();
        }

        else
        {
            mPreviewVideoTextureVideoView.setVideoPath("file://"+pathVideo);
            mPreviewVideoTextureVideoView.setVideoPath(pathVideo);
            mPreviewVideoTextureVideoView.seekTo(100);
            mPreviewVideoTextureVideoView.start();

        }

    }

}
