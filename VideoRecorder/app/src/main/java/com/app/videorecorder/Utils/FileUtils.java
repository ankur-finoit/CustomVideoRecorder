package com.app.videorecorder.Utils;

import com.app.videorecorder.AppConstants;

import java.io.File;
import java.io.IOException;

/**
 * Created by EMP256 on 12/19/2016.
 */

public class FileUtils {

    public File mCurrentFile;
    public File mCurrentAudioFile;

    public void makeTempFile(boolean video) {

        File myDir = new File(AppConstants.TEMP_FILES);
        myDir.mkdirs();
        try {
            if (video)
                mCurrentFile = File.createTempFile(AppConstants.TEMP_FILES_PREFIX, AppConstants.SUFFIX, myDir);
            else
                mCurrentAudioFile = File.createTempFile(AppConstants.TEMP_FILES_PREFIX_AUDIO, AppConstants.SUFFIX, myDir);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

}
