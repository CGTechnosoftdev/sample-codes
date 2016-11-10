package com.cgt.socialnetwork.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.cgt.socialnetwork.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by CGTechnosoft
 */
public class Util {

    public static String TAG = "UTIL";
    public static boolean isDEBUG = true;
    public static String AES_KEY = "1234567891123456";
    public static byte[] AES_IV = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


    public static final String MEDIA_DIRECTORY_NAME = "CGTSocialNetwork";
    public static final String EXTENSION_IMG_JPG = ".jpg";
    public static final String EXTENSION_IMG_PNG = ".png";


    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern
            .compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");

    public static boolean isValidEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    public static void encrypt(File source, File desti) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidKeySpecException,
            InvalidAlgorithmParameterException {

        // Here you read the cleartext.
        FileInputStream fis = new FileInputStream(source);
        // This stream write the encrypted text. This stream will be wrapped by
        // another stream.
        FileOutputStream fos = new FileOutputStream(desti);

        byte[] key = AES_KEY.getBytes();
        byte[] iv = AES_IV;

        byte[] keyBytes16 = new byte[16];
        System.arraycopy(key, 0, keyBytes16, 0, Math.min(key.length, 16));

        if (keyBytes16 == null || (keyBytes16.length < 16)) {

        }
        if (iv != null && iv.length != 16) {

        }

        SecretKeySpec keySpec = null;
        Cipher cipher = null;
        if (iv != null) {
            keySpec = new SecretKeySpec(keyBytes16, "AES/CBC/PKCS7Padding");
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        } else {
            keySpec = new SecretKeySpec(keyBytes16, "AES/ECB/PKCS7Padding");
            cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }

        CipherOutputStream cos = new CipherOutputStream(fos, cipher);

        // Write bytes
        int b;
        byte[] d = new byte[8];
        while ((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }

        // Flush and close streams.
        cos.flush();
        cos.close();
        fis.close();
    }

    public static void decrypt(File source, File desti) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidKeySpecException,
            InvalidAlgorithmParameterException {

        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(desti);

        byte[] key = AES_KEY.getBytes();
        byte[] iv = AES_IV;

        byte[] keyBytes16 = new byte[16];
        System.arraycopy(key, 0, keyBytes16, 0, Math.min(key.length, 16));

        if (keyBytes16 == null || (keyBytes16.length < 16)) {

        }

        if (iv != null && iv.length != 16) {

        }

        SecretKeySpec keySpec = null;
        Cipher cipher = null;
        if (iv != null) {
            keySpec = new SecretKeySpec(keyBytes16, "AES/CBC/PKCS7Padding");
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        } else {
            keySpec = new SecretKeySpec(keyBytes16, "AES/ECB/PKCS7Padding");
            cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
        }

        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while ((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }

        fos.flush();
        fos.close();
        cis.close();
    }

    public static byte[] encrypt(byte[] original) {
        byte[] key = AES_KEY.getBytes();
        byte[] iv = AES_IV;

        byte[] keyBytes16 = new byte[16];
        System.arraycopy(key, 0, keyBytes16, 0, Math.min(key.length, 16));

        if (keyBytes16 == null || (keyBytes16.length < 16)) {
            return null;
        }
        if (iv != null && iv.length != 16) {
            return null;
        }

        try {
            SecretKeySpec keySpec = null;
            Cipher cipher = null;
            if (iv != null) {
                keySpec = new SecretKeySpec(keyBytes16, "AES/CBC/PKCS7Padding");
                // keySpec = new SecretKeySpec(keyBytes16, "AES");
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(
                        iv));
            } else {
                keySpec = new SecretKeySpec(keyBytes16, "AES/ECB/PKCS7Padding");
                // keySpec = new SecretKeySpec(keyBytes16, "AES");
                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            }

            return cipher.doFinal(original);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] encrypted, String ask_key) {

        byte[] key = AES_KEY.getBytes();
        if (!isEmpty(ask_key)) {
            key = ask_key.getBytes();
        }

        byte[] iv = AES_IV;

        byte[] keyBytes16 = new byte[16];
        System.arraycopy(key, 0, keyBytes16, 0, Math.min(key.length, 16));

        if (keyBytes16 == null || (keyBytes16.length < 16)) {
            return null;
        }

        if (iv != null && iv.length != 16) {
            return null;
        }

        try {
            SecretKeySpec keySpec = null;
            Cipher cipher = null;
            if (iv != null) {
                keySpec = new SecretKeySpec(keyBytes16, "AES/CBC/PKCS7Padding");
                // keySpec = new SecretKeySpec(keyBytes16, "AES");
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(
                        iv));
            } else {
                keySpec = new SecretKeySpec(keyBytes16, "AES/ECB/PKCS7Padding");
                // keySpec = new SecretKeySpec(keyBytes16, "AES");
                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
            }

            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] original, String ask_key) {
        byte[] key = AES_KEY.getBytes();

        if (!isEmpty(ask_key)) {
            key = ask_key.getBytes();
        }

        byte[] iv = AES_IV;

        byte[] keyBytes16 = new byte[16];
        System.arraycopy(key, 0, keyBytes16, 0, Math.min(key.length, 16));

        if (keyBytes16 == null || (keyBytes16.length < 16)) {
            return null;
        }
        if (iv != null && iv.length != 16) {
            return null;
        }

        try {
            SecretKeySpec keySpec = null;
            Cipher cipher = null;
            if (iv != null) {
                keySpec = new SecretKeySpec(keyBytes16, "AES/CBC/PKCS7Padding");
                // keySpec = new SecretKeySpec(keyBytes16, "AES");
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(
                        iv));
            } else {
                keySpec = new SecretKeySpec(keyBytes16, "AES/ECB/PKCS7Padding");
                // keySpec = new SecretKeySpec(keyBytes16, "AES");
                cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            }

            return cipher.doFinal(original);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String getEncryptedVerId(Integer verid, String ask_key) {
        String vId = "" + verid;
        String authKey = new String(Base64.encode(Util.encrypt(
                Base64.encode(vId.getBytes(), Base64.DEFAULT), ask_key),
                Base64.DEFAULT));
        authKey = "\"" + authKey.trim() + "\"";
        return authKey;
    }

    public static String getEncryptedValue(String verid, String ask_key) {
        // String vId = "" + verid;
        String authKey = new String(Base64.encode(Util.encrypt(
                Base64.encode(verid.getBytes(), Base64.NO_WRAP), ask_key),
                Base64.NO_WRAP));
        authKey = "\"" + authKey.trim() + "\"";
        return authKey;
    }

    public static String getEncryptedAuthKey(String userName, String password) {
        String authKey = null;
        String u = new String(Base64.encode(encrypt(userName.getBytes()),
                Base64.NO_WRAP));
        String p = new String(Base64.encode(encrypt(password.getBytes()),
                Base64.NO_WRAP));
        authKey = new String(Base64.encode((u + ":" + p).getBytes(),
                Base64.NO_WRAP));
        return authKey;
    }

    public static String getEncryptedAuthKeyValue(String userName) {
        String authKey = null;
        String u = new String(Base64.encode(encrypt(userName.getBytes()),
                Base64.NO_WRAP));
        authKey = new String(Base64.encode((u).getBytes(), Base64.NO_WRAP));
        return authKey;
    }

    public static String getDecryptPassword(String password) {
        byte[] decoded = Base64.decode(password, Base64.DEFAULT);
        String pass = new String(Util.decrypt(decoded, AES_KEY));
        return pass;
    }

    /*
     * get encrypted string
     */
    public static String getEncryptedValues(String values) {
        return new String(Base64.encode(encrypt(values.getBytes()),
                Base64.NO_WRAP));
    }

    /*
     * get encrypted string with double code
     */
    public static String getEncryptedToken(String values) {
        String authKey = new String(Base64.encode(encrypt(values.getBytes()),
                Base64.NO_WRAP));
        authKey = "\"" + authKey.trim() + "\"";
        return authKey;
    }

    // Generate unique ID each time.
    public static String generateInstanceID() {
        String QRGUID = "";
        String temp = "";
        try {
            for (int i = 0; i < 6; i++) {
                if (i == 0) {
                    QRGUID += Integer.toHexString(new Random().nextInt());
                    while (QRGUID.length() < 8) {
                        QRGUID += "1";

                    }
                    QRGUID = QRGUID + "-";

                } else if (i == 1)

                    QRGUID += Integer.toHexString(new Random().nextInt())
                            .substring(0, 4) + "-";
                else if (i == 2)
                    QRGUID += Integer.toHexString(new Random().nextInt())
                            .substring(0, 4) + "-";
                else if (i == 3)
                    QRGUID += Integer.toHexString(new Random().nextInt())
                            .substring(0, 4) + "-";
                else if (i == 4)
                    temp += Integer.toHexString(new Random().nextInt());// Integer.toHexString(UIDGenerator.getUniqueScopingValue());
                else if (i == 5) {
                    QRGUID += Integer.toHexString(new Random().nextInt())
                            + temp.substring(0, 4);// Integer.toHexString(UIDGenerator.getUniqueScopingValue())
                    // + temp.substring(0,4);
                    while (QRGUID.length() < 36) {
                        QRGUID += "2";
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return QRGUID;
    }

	/*
     * public static String getDateTime() { Calendar c = Calendar.getInstance();
	 * SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy hh:mm:ss a");
	 * String strDate = sdf.format(c.getTime()); return strDate; }
	 */

    public static long getDateTime() {
        return System.currentTimeMillis();
    }

    public static long getLongDate(String str) {
        long longDate = 0;
        String date = str;
        if (!Util.isEmpty(date)) {
            SimpleDateFormat sd = new SimpleDateFormat("mm/dd/yyyy hh:mm:ss a");
            try {
                longDate = sd.parse(date).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return longDate;
    }

    // return device USB memory size
    public static long getFreeMemoryOnExternal() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        long bytesAvailable = (long) stat.getBlockSize()
                * (long) stat.getAvailableBlocks();
        long megAvailable = bytesAvailable / (1024 * 1024);
        return megAvailable;
    }

    // return device Internal memory size.
    public static long getFreeMemoryOnInternal() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize()
                * (long) stat.getAvailableBlocks();
        long megAvailable = bytesAvailable / (1024 * 1024);
        return megAvailable;
    }

    // return device USB memory size
    public static long getFreeMemoryOnSD() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath() + "/external_sd/");
        long bytesAvailable = (long) stat.getBlockSize()
                * (long) stat.getAvailableBlocks();
        long megAvailable = bytesAvailable / (1024 * 1024);
        return megAvailable;
    }

    // checked sdcard status
    public static boolean isSDCardPresent() {
        boolean isPresent = false;
        String state = Environment.getExternalStorageState();
        if ((!state.equalsIgnoreCase(Environment.MEDIA_UNMOUNTABLE) && !state
                .equalsIgnoreCase(Environment.MEDIA_UNMOUNTED))
                && !state.equalsIgnoreCase(Environment.MEDIA_REMOVED)) {
            isPresent = true;
        }
        return isPresent;
    }

    public static File getOutputMediaFile() {
        File mediaStorageDir = null;

        boolean bool = false;

        if (android.os.Build.DEVICE.contains("Samsung")
                || android.os.Build.MANUFACTURER.contains("Samsung")
                || android.os.Build.MANUFACTURER.contains("samsung")) {
            if (new File(Environment.getExternalStorageDirectory()
                    + "/external_sd/").exists()
                    && getFreeMemoryOnSD() > 200) {
                mediaStorageDir = new File(
                        Environment.getExternalStorageDirectory()
                                + "/external_sd/",
                        MEDIA_DIRECTORY_NAME);
                bool = true;
            } else {
                File f = Environment.getExternalStorageDirectory();
                if (null != f) {
                    if (f.exists() && getFreeMemoryOnExternal() > 200) {
                        mediaStorageDir = new File(
                                Environment.getExternalStorageDirectory(),
                                MEDIA_DIRECTORY_NAME);
                        bool = true;
                    }
                } else if (getFreeMemoryOnInternal() > 512) {
                    mediaStorageDir = new File(
                            "//data//data//com.cgt.socialnetwork//Files");
                    bool = true;
                }
            }
        } else {
            File f = Environment.getExternalStorageDirectory();
            if (null != f) {
                if (f.exists() && getFreeMemoryOnExternal() > 200) {
                    mediaStorageDir = new File(
                            Environment.getExternalStorageDirectory(),
                            MEDIA_DIRECTORY_NAME);
                    bool = true;
                }
            } else if (getFreeMemoryOnInternal() > 200) {
                mediaStorageDir = new File(
                        "//data//data//com.cgt.socialnetwork//Files");
                bool = true;
            }
        }
        if (!bool) {
            if (Util.isSDCardPresent()) {
                mediaStorageDir = new File(
                        Environment.getExternalStorageDirectory(),
                        MEDIA_DIRECTORY_NAME);
                bool = true;
            }
        }

        // Create the storage directory if it does not exist

        if (null == mediaStorageDir) {
            return null;
        } else if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                // Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File mediaFile = null;
        File mediaSubDir = new File(mediaStorageDir, "");
        if (!mediaSubDir.exists()) {
            if (!mediaSubDir.mkdirs()) {
                // Log.d(TAG, "failed to create image directory");
                return null;
            }
        }
        mediaFile = new File(mediaSubDir.getPath() + File.separator
                + "SKETCH_" + timeStamp + EXTENSION_IMG_JPG);
        return mediaFile;
    }

    public static String checkFilePath(String filePath) {
        String tempFile = "file:///";
        String tempFile1 = "/file:";
        if (filePath.contains(tempFile)) {
            filePath = filePath.substring(tempFile.length(), filePath.length());
        } else if (filePath.contains(tempFile1)) {
            filePath = filePath
                    .substring(tempFile1.length(), filePath.length());
        }
        return filePath;
    }

    public static void saveToDisk(File path, Bitmap bmp) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(
                    fileOutputStream);
            bmp.compress(CompressFormat.JPEG, 30, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            Log.e("Utility --- ", "Error while writing to file", e);
        }
    }

    public static File saveScaledBitmapToDisk(Uri uri, String pathToSave,
                                              Context c) {
        File path = new File(pathToSave);
        InputStream is = null;
        Bitmap bmp = null;
        try {

            is = getInputStream(uri, c);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeStream(is, null, opts);
            is.close();
            int scale = getSampleSize(opts);
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scale;
            is = getInputStream(uri, c);
            bmp = BitmapFactory.decodeStream(is, null, opts);
            opts = null;

            bmp.compress(CompressFormat.JPEG, 50, new FileOutputStream(
                    path));
        } catch (IOException io) {
            io.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bmp != null) {
                bmp.recycle();
            }
        }

        return path;
    }

    private static InputStream getInputStream(Uri uri, Context c)
            throws FileNotFoundException {
        InputStream is = null;
        if (uri.toString().startsWith("content:")) {
            is = c.getContentResolver().openInputStream(uri);

        } else {
            File f = new File(uri.toString());
            is = new FileInputStream(f);
        }

        return is;
    }

    private static int getSampleSize(BitmapFactory.Options options) {
        int megaPixel = (options.outHeight * options.outWidth) / 1024000;

        double power = megaPixel / 5.0;
        power = power < 1 ? 1 : power;
        int newPower = (int) (power > 1 ? getRoundOff(power) : power);
        return (int) Math.pow(2, newPower);
    }

    private static int getRoundOff(double d) {
        int rVal = (int) Math.round(d);
        double newValue = d - rVal;
        return newValue > 0 ? rVal + 1 : rVal;
    }

    public static ArrayList<Uri> getAllFilesName(String path) {

        ArrayList<Uri> results = new ArrayList<Uri>();
        File[] files = new File(path).listFiles(fileNameFilter);
        for (File file : files) {
            if (file.isFile()) {
                results.add(Uri.parse("file://" + file.getPath()));
            }
        }
        return results;
    }

    public static ArrayList<Uri> getAllFilesNameWithAudio(String path) {

        ArrayList<Uri> results = new ArrayList<Uri>();
        File[] files = new File(path).listFiles(fileNameFilterWithAudio);
        if (null != files && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    results.add(Uri.parse("file://" + file.getPath()));
                }
            }
        }
        return results;
    }

    public static ArrayList<String> getAllFilesNameWithFilter(String path,
                                                              String type) {

        ArrayList<String> results = new ArrayList<String>();
        File[] files = new File(path).listFiles(fileNameFilterWithType);
        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getPath());
            }
        }
        return results;
    }

    public static ArrayList<String> getAllFilesNameWithFilterForCamera(
            String path, String type, String input_id) {

        ArrayList<String> results = new ArrayList<String>();
        FilenameFilter select = new FileListFilter(input_id);
        File[] files = new File(path).listFiles(select);
        if (null != files) {
            for (File file : files) {
                if (file.isFile()) {
                    results.add(file.getPath());
                }
            }
        }

        return results;
    }

    static FilenameFilter fileNameFilterWithTypeForCamera = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);

                // match path name extension
                if (name.contains("CAMERA")) {
                    return true;
                }
            }
            return false;
        }
    };

    static class FileListFilter implements FilenameFilter {
        private String extension;

        public FileListFilter(String extension) {
            this.extension = extension;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                // get last index for '.' char
                // int lastIndex = name.lastIndexOf('.');

                // get extension
                // String str = name.substring(lastIndex);

                // match path name extension
                if (name.contains(extension) && !name.contains("foreground")
                        && !name.contains("background")) {
                    return true;
                }
            }
            return false;
        }
    }

    static class CopyFileListFilter implements FilenameFilter {
        private String extension;

        public CopyFileListFilter(String extension) {
            this.extension = extension;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);

                // match path name extension
                if (str.equals(".pdf") || str.equals(".html")
                        || str.equals(".csv")) {
                    return false;
                }
            }
            return true;
        }
    }

    static FilenameFilter fileNameFilterWithType = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);

                // match path name extension
                if (str.equals(".mp4")) {
                    return true;
                }
            }
            return false;
        }

    };
    static FilenameFilter fileNameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);

                // match path name extension
                if (str.equals(".html") || name.contains("foreground")
                        || name.contains("background") || name.contains("AUD")
                        || name.contains("IMG") || name.contains("SKETCH")) {
                    return false;
                }
            }
            return true;
        }
    };

    static FilenameFilter fileNameFilterWithAudio = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);

                // match path name extension
                if (str.equals(".html") || name.contains("foreground")
                        || name.contains("background") || name.contains("IMG")
                        || name.contains("SKETCH")) {
                    return false;
                }
            }
            return true;
        }
    };

    public static Uri savingVideo(Context c, String tempPath,
                                  boolean delOriginal, String strFile) {

        Uri uri = null;
        try {

            Uri imgUri = Uri.parse(tempPath); // "content://media/external/images/media/92";
            InputStream is = null;
            if (tempPath.startsWith("content:")) {
                is = c.getContentResolver().openInputStream(imgUri);
            } else {
                File f = new File(tempPath);
                is = new BufferedInputStream(new FileInputStream(f));
            }
            // File file = new File(strFile);

            File myDir = new File(strFile);
            if (!myDir.exists())
                myDir.mkdirs();

            String fname = "mspec_"
                    + String.valueOf(System.currentTimeMillis()) + ".mp4";
            File file = new File(myDir, fname);

            if (file.exists())
                file.delete();

            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            // Read bytes (and store them) until there is nothing more to
            // read(-1)
            int len;
            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff, 0, len);
            }

            uri = Uri.fromFile(file);

            String path = uri.toString();

            if (path.contains("file:///mnt/")) {
                path = path.substring(path.indexOf("mnt/"), path.length());
                uri = Uri.parse(path);
            } else if (path.contains("file:///sdcard")) {
                path = path.substring(path.indexOf("sdcard/"), path.length());
                uri = Uri.parse(path);
            } else if (path.contains("file:///storage/")) {
                path = path.substring(path.indexOf("storage/"), path.length());
                uri = Uri.parse(path);
            }

            // clean up
            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // /storage/sdcard0/mSpectrum/mspec_1398684893211.mp4
        return uri;
    }

    private static File createDir() {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + MEDIA_DIRECTORY_NAME);
        if (null != myDir && !myDir.exists()) {
            myDir.mkdirs();
        }
        return myDir;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options}
     * object when decoding bitmaps using the decode* methods from
     * {@link BitmapFactory}. This implementation calculates the closest
     * inSampleSize that will result in the final decoded bitmap having a width
     * and height equal to or larger than the requested width and height. This
     * implementation does not ensure a power of 2 is returned for inSampleSize
     * which can be faster when decoding but results in a larger bitmap which
     * isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run
     *                  through a decode* method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and
     * height.
     *
     * @param filename  The full path of the file to decode
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect
     * ratio and dimensions that are equal to or greater than the
     * requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    public static void copyFolder(File src, File dest) throws IOException {

        if (src.isDirectory()) {

            // if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
                // System.out.println("Directory copied from " + src + "  to "
                // + dest);
            }

            // list all the directory contents
            FilenameFilter select = new CopyFileListFilter("");
            String files[] = src.list(select);

            for (String file : files) {
                // construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {
            // if file, then copy it
            // Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            // copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
            // System.out.println("File copied from " + src + " to " + dest);
        }
    }

    public static void copyFile(File src, File dest) throws IOException {

        // if file, then copy it
        // Use bytes stream to support all file types
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);

        byte[] buffer = new byte[1024];

        int length;
        // copy the file content in bytes
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
        System.out.println("File copied from " + src + " to " + dest);

    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null && children.length > 0) {
                int count = children.length;
                for (int i = 0; i < count; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        return dir.delete();
    }

    public static String getErrorMsg(Throwable e, Context context) {
        String msg = context.getString(R.string.msg_unable_to_process_request);

        if (e instanceof NoConnectionError) {
            msg = context.getString(R.string.no_network_connection_toast);
        }

        return msg;
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static String getJsonValue(JSONObject object, String key) {
        try {
            return object.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getJsonValueInt(JSONObject object, String key) {
        try {
            return object.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
