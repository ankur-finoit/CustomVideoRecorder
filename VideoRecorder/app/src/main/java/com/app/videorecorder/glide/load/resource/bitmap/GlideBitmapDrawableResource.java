package com.app.videorecorder.glide.load.resource.bitmap;

import com.app.videorecorder.glide.load.engine.bitmap_recycle.BitmapPool;
import com.app.videorecorder.glide.load.resource.drawable.DrawableResource;
import com.app.videorecorder.glide.util.Util;

/**
 * A resource wrapper for {@link com.app.videorecorder.glide.load.resource.bitmap.GlideBitmapDrawable}.
 */
public class GlideBitmapDrawableResource extends DrawableResource<GlideBitmapDrawable> {
    private final BitmapPool bitmapPool;

    public GlideBitmapDrawableResource(GlideBitmapDrawable drawable, BitmapPool bitmapPool) {
        super(drawable);
        this.bitmapPool = bitmapPool;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(drawable.getBitmap());
    }

    @Override
    public void recycle() {
        bitmapPool.put(drawable.getBitmap());
    }
}
