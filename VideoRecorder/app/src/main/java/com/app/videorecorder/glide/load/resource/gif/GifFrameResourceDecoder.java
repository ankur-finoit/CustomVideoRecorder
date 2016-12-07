package com.app.videorecorder.glide.load.resource.gif;

import android.graphics.Bitmap;

import com.app.videorecorder.glide.gifdecoder.GifDecoder;
import com.app.videorecorder.glide.load.ResourceDecoder;
import com.app.videorecorder.glide.load.engine.Resource;
import com.app.videorecorder.glide.load.engine.bitmap_recycle.BitmapPool;
import com.app.videorecorder.glide.load.resource.bitmap.BitmapResource;

class GifFrameResourceDecoder implements ResourceDecoder<GifDecoder, Bitmap> {
    private final BitmapPool bitmapPool;

    public GifFrameResourceDecoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<Bitmap> decode(GifDecoder source, int width, int height) {
        Bitmap bitmap = source.getNextFrame();
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    @Override
    public String getId() {
        return "GifFrameResourceDecoder.com.app.videorecorder.glide.load.resource.gif";
    }
}
