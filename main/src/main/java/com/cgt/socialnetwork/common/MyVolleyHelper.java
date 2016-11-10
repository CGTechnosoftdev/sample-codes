package com.cgt.socialnetwork.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class MyVolleyHelper {

    private static MyVolleyHelper mInstance = null;
    private static Context mCtx = null;
    private RequestQueue mRequestQueue = null;
    private ImageLoader mImageLoader = null;

    private MyVolleyHelper(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(ctx));
    }

    public static synchronized MyVolleyHelper getIntance(Context ctx) {
        if (mInstance == null) {
            mInstance = new MyVolleyHelper(ctx);
        }

        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public <T> void addRequestToQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    public void cancelRequest(String tag) {
        getRequestQueue().cancelAll(tag);
    }

    public void clearCache(String requestKey) {
        getRequestQueue().getCache().remove(requestKey);
    }

    static class LruBitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

        LruBitmapCache(int maxSize) {
            super(maxSize);
        }

        LruBitmapCache(Context context) {
            super(getCacheSize(context));
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }

        @Override
        public Bitmap getBitmap(String url) {
            return get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }

        // Returns a cache size equal to approximately three screens worth of images.
        public static int getCacheSize(Context ctx) {
            final DisplayMetrics displayMetrics = ctx.getResources().
                    getDisplayMetrics();
            final int screenWidth = displayMetrics.widthPixels;
            final int screenHeight = displayMetrics.heightPixels;

            // 4 bytes per pixel
            final int screenBytes = screenWidth * screenHeight * 4;
            return screenBytes * 3;
        }
    }

    public String addParamsToUrl(Map parameter, String url) {
        StringBuilder mBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = parameter.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, String> mapEntry = (Map.Entry) iterator.next();
            mBuilder.append(mapEntry.getKey() + "=");
            try {
                mBuilder.append(URLEncoder.encode(mapEntry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (count != parameter.size() - 1) {
                mBuilder.append("&");
                count++;
            }
        }

        return url = url + "?" + mBuilder.toString();
    }


}