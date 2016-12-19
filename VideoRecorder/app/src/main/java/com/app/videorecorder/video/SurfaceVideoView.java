package com.app.videorecorder.video;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by EMP256 on 12/15/2016.
 */

/* Custom view class made to show the merged video*/
public class SurfaceVideoView extends VideoView {

    int mVideoWidth;
    int mVideoHeight;
    public SurfaceVideoView(Context context) {
        super(context);
    }

    public SurfaceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if ( mVideoWidth * height  > width * mVideoHeight ) {
                height = width * mVideoHeight / mVideoWidth;
            } else if ( mVideoWidth * height  < width * mVideoHeight ) {
                width = height * mVideoWidth / mVideoHeight;
            } else {

            }
        }
        setMeasuredDimension(width, height);
    }

    public void setVideoHeigthWidth(Uri videoHeigthWidth) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this.getContext(), videoHeigthWidth);
        mVideoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        mVideoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
    }
}
