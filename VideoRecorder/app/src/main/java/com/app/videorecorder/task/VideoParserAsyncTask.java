package com.app.videorecorder.task;

import android.os.AsyncTask;

import com.app.videorecorder.AppConstants;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.app.videorecorder.AppConstants.FILE_PATH;

/**
 * Created by Ankur Parashar on 8/2/2016.
 */
public class VideoParserAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnVideoParserAsyncTask mListener;

    private String pathToMergedVideo;

    private ArrayList<String> videoList;

    private ArrayList<String> audioList;

    public interface OnVideoParserAsyncTask {
        void onVideoMergingStart();
        void onVideoMergingFinished(String pathToMergedVideo);
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
        return null;
    }

    @Override
    protected void onPostExecute(Void bitmaps) {
        super.onPostExecute(bitmaps);
        if (mListener != null) {
            mListener.onVideoMergingFinished(pathToMergedVideo);
        }
    }

    /*Used for creating a single video from the given set of videos*/
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
                    if (t.getHandler().equals(AppConstants.VIDEO)) {
                        videoTracks.add(t);
                    }
                }
            }

            for (Movie m : inAudios) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals(AppConstants.SOUND)) {
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

            MovieHeaderBox mvhd = Path.getPath(out, AppConstants.MOOV_MVHD);
            mvhd.setMatrix(Matrix.ROTATE_180);

            File myDir = new File(FILE_PATH);
            myDir.mkdirs();
            File file = File.createTempFile(AppConstants.OUTPUT, AppConstants.SUFFIX, myDir);
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

           // createGif(pathToMergedVideo);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



