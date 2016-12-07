package com.app.videorecorder.glide.load.resource.gifbitmap;

import android.graphics.Bitmap;

import com.app.videorecorder.glide.load.ResourceEncoder;
import com.app.videorecorder.glide.load.engine.Resource;
import com.app.videorecorder.glide.load.resource.gif.GifDrawable;

import java.io.OutputStream;

/**
 * A {@link com.app.videorecorder.glide.load.ResourceEncoder} that can encode either an {@link Bitmap} or
 * {@link com.app.videorecorder.glide.load.resource.gif.GifDrawable}.
 */
public class GifBitmapWrapperResourceEncoder implements ResourceEncoder<GifBitmapWrapper> {
    private final ResourceEncoder<Bitmap> bitmapEncoder;
    private final ResourceEncoder<GifDrawable> gifEncoder;
    private String id;

    public GifBitmapWrapperResourceEncoder(ResourceEncoder<Bitmap> bitmapEncoder,
            ResourceEncoder<GifDrawable> gifEncoder) {
        this.bitmapEncoder = bitmapEncoder;
        this.gifEncoder = gifEncoder;
    }

    @Override
    public boolean encode(Resource<GifBitmapWrapper> resource, OutputStream os) {
        final GifBitmapWrapper gifBitmap = resource.get();
        final Resource<Bitmap> bitmapResource = gifBitmap.getBitmapResource();

        if (bitmapResource != null) {
            return bitmapEncoder.encode(bitmapResource, os);
        } else {
            return gifEncoder.encode(gifBitmap.getGifResource(), os);
        }
    }

    @Override
    public String getId() {
        if (id == null) {
            id = bitmapEncoder.getId() + gifEncoder.getId();
        }
        return id;
    }
}
