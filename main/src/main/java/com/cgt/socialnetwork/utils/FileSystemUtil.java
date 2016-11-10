package com.cgt.socialnetwork.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by CGTechnosoft
 */
public class FileSystemUtil {

    public static final String TAG = "FILE SYSTEM UTIL ---- ";

    public static File getOutputFilePath(Context context, String dirName, String fileName) {
        File dir = new File(
                context.getExternalFilesDir(null), dirName);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d(TAG, "failed to create " + dirName + " directory");
                return null;
            }
        }

        File mediaFile = new File(dir, fileName);
        return mediaFile;
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}
