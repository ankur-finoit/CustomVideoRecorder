package com.app.videorecorder.glide.load.resource.gif;

import android.content.Context;

import com.app.videorecorder.glide.load.Encoder;
import com.app.videorecorder.glide.load.ResourceDecoder;
import com.app.videorecorder.glide.load.ResourceEncoder;
import com.app.videorecorder.glide.load.engine.bitmap_recycle.BitmapPool;
import com.app.videorecorder.glide.load.model.StreamEncoder;
import com.app.videorecorder.glide.load.resource.file.FileToStreamDecoder;
import com.app.videorecorder.glide.provider.DataLoadProvider;

import java.io.File;
import java.io.InputStream;

/**
 * An {@link com.app.videorecorder.glide.provider.DataLoadProvider} that loads an {@link java.io.InputStream} into
 * {@link com.app.videorecorder.glide.load.resource.gif.GifDrawable} that can be used to display an animated GIF.
 */
public class GifDrawableLoadProvider implements DataLoadProvider<InputStream, GifDrawable> {
    private final GifResourceDecoder decoder;
    private final GifResourceEncoder encoder;
    private final StreamEncoder sourceEncoder;
    private final FileToStreamDecoder<GifDrawable> cacheDecoder;

    public GifDrawableLoadProvider(Context context, BitmapPool bitmapPool) {
        decoder = new GifResourceDecoder(context, bitmapPool);
        cacheDecoder = new FileToStreamDecoder<GifDrawable>(decoder);
        encoder = new GifResourceEncoder(bitmapPool);
        sourceEncoder = new StreamEncoder();
    }

    @Override
    public ResourceDecoder<File, GifDrawable> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<InputStream, GifDrawable> getSourceDecoder() {
        return decoder;
    }

    @Override
    public Encoder<InputStream> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<GifDrawable> getEncoder() {
        return encoder;
    }
}
