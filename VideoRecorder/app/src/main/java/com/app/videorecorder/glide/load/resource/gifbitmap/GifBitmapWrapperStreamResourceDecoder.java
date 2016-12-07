package com.app.videorecorder.glide.load.resource.gifbitmap;

import com.app.videorecorder.glide.load.ResourceDecoder;
import com.app.videorecorder.glide.load.engine.Resource;
import com.app.videorecorder.glide.load.model.ImageVideoWrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link com.app.videorecorder.glide.load.ResourceDecoder} that can decode an
 * {@link com.app.videorecorder.glide.load.resource.gifbitmap.GifBitmapWrapper} from {@link java.io.InputStream} data.
 */
public class GifBitmapWrapperStreamResourceDecoder implements ResourceDecoder<InputStream, GifBitmapWrapper> {
    private final ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> gifBitmapDecoder;

    public GifBitmapWrapperStreamResourceDecoder(
            ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> gifBitmapDecoder) {
        this.gifBitmapDecoder = gifBitmapDecoder;
    }

    @Override
    public Resource<GifBitmapWrapper> decode(InputStream source, int width, int height) throws IOException {
        return gifBitmapDecoder.decode(new ImageVideoWrapper(source, null), width, height);
    }

    @Override
    public String getId() {
        return gifBitmapDecoder.getId();
    }
}
