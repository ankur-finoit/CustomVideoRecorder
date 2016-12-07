package com.app.videorecorder.glide.load.resource.file;

import com.app.videorecorder.glide.load.ResourceDecoder;
import com.app.videorecorder.glide.load.engine.Resource;

import java.io.File;

/**
 * A simple {@link com.app.videorecorder.glide.load.ResourceDecoder} that creates resource for a given {@link java.io.File}.
 */
public class FileDecoder implements ResourceDecoder<File, File> {

    @Override
    public Resource<File> decode(File source, int width, int height) {
        return new FileResource(source);
    }

    @Override
    public String getId() {
        return "";
    }
}
