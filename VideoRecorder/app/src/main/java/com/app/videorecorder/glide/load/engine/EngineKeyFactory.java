package com.app.videorecorder.glide.load.engine;

import com.app.videorecorder.glide.load.Encoder;
import com.app.videorecorder.glide.load.Key;
import com.app.videorecorder.glide.load.ResourceDecoder;
import com.app.videorecorder.glide.load.ResourceEncoder;
import com.app.videorecorder.glide.load.Transformation;
import com.app.videorecorder.glide.load.resource.transcode.ResourceTranscoder;

class EngineKeyFactory {

    @SuppressWarnings("rawtypes")
    public EngineKey buildKey(String id, Key signature, int width, int height, ResourceDecoder cacheDecoder,
                              ResourceDecoder sourceDecoder, Transformation transformation, ResourceEncoder encoder,
                              ResourceTranscoder transcoder, Encoder sourceEncoder) {
        return new EngineKey(id, signature, width, height, cacheDecoder, sourceDecoder, transformation, encoder,
                transcoder, sourceEncoder);
    }

}
