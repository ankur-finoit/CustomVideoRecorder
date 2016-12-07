package com.app.videorecorder;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import com.app.videorecorder.glide.gifencoder.AnimatedGifEncoder;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/*
 * Created by emp203 on 8/2/2016.
 */
public class VideoParserAsyncTask extends AsyncTask<Void, Void, Void> {

    //private String videoPath;
    private OnVideoParserAsyncTask mListener;

    private String pathToMergedVideo;
    private String pathToCreatedGif;

    private ArrayList<String> videoList;
    private ArrayList<String> audioList;

    public interface OnVideoParserAsyncTask {
        void onVideoMergingStart();

        void onVideoMergingFinished(String pathToMergedVideo, String pathToCreatedGif);
    }

    public VideoParserAsyncTask(OnVideoParserAsyncTask listener, ArrayList<String> videoList, ArrayList<String> audioList) {
        this.videoList = new ArrayList<>();
        this.audioList = new ArrayList<>();
        this.audioList = audioList;
        this.videoList = videoList;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null)
            mListener.onVideoMergingStart();
    }

    @Override
    protected Void doInBackground(Void... params) {

        mergeVideos();
        createGif(pathToMergedVideo);
        return null;
    }

    @Override
    protected void onPostExecute(Void bitmaps) {
        super.onPostExecute(bitmaps);
        if (mListener != null) {
            mListener.onVideoMergingFinished(pathToMergedVideo, pathToCreatedGif);
        }
    }

    private void createGif(String string) {
        try {
            MediaMetadataRetriever mMediaMetadataRetriever = new MediaMetadataRetriever();
            mMediaMetadataRetriever.setDataSource(string);

            ArrayList<Bitmap> mCapturedFrames = new ArrayList<>();

            for (int j = 2000; j < 6000; j = j + 1000)
                mCapturedFrames.add(mMediaMetadataRetriever.getFrameAtTime(j * 1000, mMediaMetadataRetriever.OPTION_CLOSEST_SYNC));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.setFrameRate(2);
            encoder.start(bos);

            File myDir = new File("/sdcard/mov_gifs");
            myDir.mkdirs();

            for (Bitmap bitmap : mCapturedFrames) {
                Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 480, 640, false);
                encoder.addFrame(bitmap1);
                bitmap1.recycle();
            }
            encoder.finish();

            File file = File.createTempFile("mov_gif", ".gif", myDir);
            pathToCreatedGif = file.getAbsolutePath();
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream out = new FileOutputStream(file);
            out.write(bos.toByteArray());
            out.close();

            mCapturedFrames.clear();
            mMediaMetadataRetriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeVideos() {
        try {

            MovieCreator movieCreator = new MovieCreator();

            Movie[] inVideos = new Movie[videoList.size()];
            for (int i = 0; i < videoList.size(); i++) {
                inVideos[i] = movieCreator.build(videoList.get(i));
            }

            Movie[] inAudios = new Movie[audioList.size()];
            for (int i = 0; i < audioList.size(); i++) {
                inAudios[i] = movieCreator.build(audioList.get(i));
            }

            List<Track> videoTracks = new LinkedList<>();
            List<Track> audioTracks = new LinkedList<>();

            for (Movie m : inVideos) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                }
            }

            for (Movie m : inAudios) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                }
            }

            Movie result = new Movie();

            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }

            Container out = new DefaultMp4Builder().build(result);

            MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
            mvhd.setMatrix(Matrix.ROTATE_180);

            File myDir = new File("/sdcard/mov_video");
            myDir.mkdirs();
            File file = File.createTempFile("output", ".mp4", myDir);
            pathToMergedVideo = file.getAbsolutePath();
            RandomAccessFile ram = new RandomAccessFile(file, "rw");

            FileChannel fc = ram.getChannel();
            out.writeContainer(fc);
            ram.close();
            fc.close();

            for (int i = 0; i < audioList.size(); i++)
                new File(audioList.get(i)).delete();
            for (int i = 0; i < videoList.size(); i++)
                new File(videoList.get(i)).delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



