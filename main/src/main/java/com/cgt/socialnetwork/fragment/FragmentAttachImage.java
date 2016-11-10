package com.cgt.socialnetwork.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Surface;

import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.utils.FileSystemUtil;
import com.cgt.socialnetwork.utils.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by CGTechnosoft
 */
public class FragmentAttachImage extends Fragment {

    private Context context;

    public static final int OPEN_CAMERA = 23;
    public static final int OPEN_GALLERY = 24;

    private String strImagePath = "";
    private int width, height;

    public FragmentAttachImage() {
    }

    public FragmentAttachImage(Context context, onImageChoseListener imageCallback, int width, int height) {
        this.context = context;
        this.imageCallback = imageCallback;
        this.width = width;
        this.height = height;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, Context mBaseActivity) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            switch (requestCode) {
                case OPEN_CAMERA:
                    bitmap = ImageUtil.decodeSampledBitmapFromFile(strImagePath, width, height);
                    bitmap = ImageUtil.getResizedBitmap(bitmap, width, height);
                    int degree = ImageUtil.getExifOrientation(strImagePath);
                    bitmap = ImageUtil.rotate(bitmap, degree + getDisplayRotation());
                    saveBitmap(strImagePath, bitmap);
                    imageCallback.onImageSelected(strImagePath);
                    break;
                case OPEN_GALLERY:
                    String strImagePath1 = getGalleryImagePath(data.getData());
                    bitmap = ImageUtil.decodeSampledBitmapFromFile(strImagePath1, width, height);
                    File filePath = FileSystemUtil.getOutputFilePath(context, Constants.DIR_IMAGE, Constants.PREFIX_IMG + System.currentTimeMillis() + Constants.EXT_JPG);
                    strImagePath = filePath.getAbsolutePath();
                    saveBitmap(strImagePath, ImageUtil.getResizedBitmap(bitmap, width, height));
                    imageCallback.onImageSelected(strImagePath);
                    break;
            }
        }
    }

    private String getGalleryImagePath(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null) {
            String picturePath = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return picturePath;
        }
        return null;
    }

    public void openCameraForImage(Fragment fragment) {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File filePath = FileSystemUtil.getOutputFilePath(context, Constants.DIR_IMAGE, Constants.PREFIX_IMG + System.currentTimeMillis() + Constants.EXT_JPG);
        strImagePath = filePath.getAbsolutePath();
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePath));
        fragment.startActivityForResult(i, OPEN_CAMERA);
    }

    public void openGalleryForImage(Fragment fragment) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        fragment.startActivityForResult(i, OPEN_GALLERY);
    }

    private File saveBitmap(String filePath, Bitmap bitmap) {
        OutputStream outStream = null;

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            file = new File(filePath);
        }

        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Log.e("file", "" + file);
        return file;
    }

    private onImageChoseListener imageCallback;

    public interface onImageChoseListener {
        void onImageSelected(String imagePath);
    }

    private int getDisplayRotation() {
        int rotation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

}

