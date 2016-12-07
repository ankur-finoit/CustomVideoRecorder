package com.app.videorecorder.glide.load.resource.file;

import com.app.videorecorder.glide.load.resource.SimpleResource;

import java.io.File;

/**
 * A simple {@link com.app.videorecorder.glide.load.engine.Resource} that wraps a {@link File}.
 */
public class FileResource extends SimpleResource<File> {
    public FileResource(File file) {
        super(file);
    }
}
