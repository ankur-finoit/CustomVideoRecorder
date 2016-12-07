package com.app.videorecorder.glide.load.model.stream;

import android.content.Context;
import android.net.Uri;

import com.app.videorecorder.glide.Glide;
import com.app.videorecorder.glide.load.data.DataFetcher;
import com.app.videorecorder.glide.load.data.StreamAssetPathFetcher;
import com.app.videorecorder.glide.load.data.StreamLocalUriFetcher;
import com.app.videorecorder.glide.load.model.GenericLoaderFactory;
import com.app.videorecorder.glide.load.model.GlideUrl;
import com.app.videorecorder.glide.load.model.ModelLoader;
import com.app.videorecorder.glide.load.model.ModelLoaderFactory;
import com.app.videorecorder.glide.load.model.UriLoader;

import java.io.InputStream;

/**
 * A {@link ModelLoader} for translating uri models into {@link InputStream} data. Capable of handling 'http',
 * 'https', 'android.resource', 'content', and 'file' schemes. Unsupported schemes will throw an exception in
 * {@link #getResourceFetcher(Uri, int, int)}.
 */
public class StreamUriLoader extends UriLoader<InputStream> implements StreamModelLoader<Uri> {

    /**
     * THe default factory for {@link com.app.videorecorder.glide.load.model.stream.StreamUriLoader}s.
     */
    public static class Factory implements ModelLoaderFactory<Uri, InputStream> {

        @Override
        public ModelLoader<Uri, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new StreamUriLoader(context, factories.buildModelLoader(GlideUrl.class, InputStream.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

    public StreamUriLoader(Context context) {
        this(context, Glide.buildStreamModelLoader(GlideUrl.class, context));
    }

    public StreamUriLoader(Context context, ModelLoader<GlideUrl, InputStream> urlLoader) {
        super(context, urlLoader);
    }

    @Override
    protected DataFetcher<InputStream> getLocalUriFetcher(Context context, Uri uri) {
        return new StreamLocalUriFetcher(context, uri);
    }

    @Override
    protected DataFetcher<InputStream> getAssetPathFetcher(Context context, String assetPath) {
        return new StreamAssetPathFetcher(context.getApplicationContext().getAssets(), assetPath);
    }
}
