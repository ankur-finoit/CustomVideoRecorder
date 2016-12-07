package com.app.videorecorder.glide.load.resource.transcode;

import com.app.videorecorder.glide.load.engine.Resource;
import com.app.videorecorder.glide.load.resource.bytes.BytesResource;
import com.app.videorecorder.glide.load.resource.gif.GifDrawable;

/**
 * An {@link com.app.videorecorder.glide.load.resource.transcode.ResourceTranscoder} that converts
 * {@link com.app.videorecorder.glide.load.resource.gif.GifDrawable} into bytes by obtaining the original bytes of the GIF from
 * the {@link com.app.videorecorder.glide.load.resource.gif.GifDrawable}.
 */
public class GifDrawableBytesTranscoder implements ResourceTranscoder<GifDrawable, byte[]> {
    @Override
    public Resource<byte[]> transcode(Resource<GifDrawable> toTranscode) {
        GifDrawable gifData = toTranscode.get();
        return new BytesResource(gifData.getData());
    }

    @Override
    public String getId() {
        return "GifDrawableBytesTranscoder.com.app.videorecorder.glide.load.resource.transcode";
    }
}
