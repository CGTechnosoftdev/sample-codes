package com.cgt.socialnetwork.common;


import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.utils.DebugLog;
import com.cgt.socialnetwork.utils.Filename;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class RequestDispatcher {
    private final OkHttpClient mClient;

    public RequestDispatcher(OkHttpClient mOkHttpClient) {
        mClient = mOkHttpClient;
    }

    public String createGetRequest(String url, Map<String, String> parameter) throws IOException {
        if (parameter != null) {
            StringBuilder mBuilder = new StringBuilder();
            Iterator<Map.Entry<String, String>> iterator = parameter.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, String> mapEntry = (Map.Entry) iterator.next();
                mBuilder.append(mapEntry.getKey() + "=");
                mBuilder.append(URLEncoder.encode(mapEntry.getValue(), "UTF-8"));
                if (count != parameter.size() - 1) {
                    mBuilder.append("&");
                    count++;
                }
            }

            url = url + "?" + mBuilder.toString();
        }

        return run(url);
    }

    public String createPostRequest(String url, Map<String, String> parameter) throws IOException {
        return run(url, parameter);
    }

    public String createMultipartRequest(String url, Map<String, String> parameters, String imageTitle, String pathImage) throws Exception {
        return run(url, parameters, imageTitle, pathImage);
    }

    private String run(String url) throws IOException {
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());

        DebugLog.e("Url : " + url);

        Request request = new Request.Builder()
                .url(url).header(Constants.KEY_HEADER_TOKEN, manager.getUser().getToken())
                .build();
        Response response = mClient.newCall(request).execute();
        String s = (response.body().string());
        DebugLog.e(s);
        return s;
    }

    private String run(String url, Map<String, String> parameters) throws IOException {
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());

        Request request;

        if (parameters != null) {
            MultipartBuilder multipartBuilder = new MultipartBuilder();
            multipartBuilder.type(MultipartBuilder.FORM);

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }

            RequestBody requestBody = multipartBuilder.build();
            if (manager.getUser() != null && manager.getUser().getToken() != null) {
                request = new Request.Builder()
                        .addHeader(Constants.KEY_HEADER_TOKEN, manager.getUser().getToken())
                        .url(url)
                        .post(requestBody)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
            }
        } else {
            RequestBody requestBody = RequestBody.create(null, new byte[0]);
            if (manager.getUser() != null && manager.getUser().getToken() != null) {
                request = new Request.Builder()
                        .addHeader(Constants.KEY_HEADER_TOKEN, manager.getUser().getToken())
                        .url(url)
                        .post(requestBody)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
            }
        }

        DebugLog.e(request.body().contentLength() + "");

        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String s = (response.body().string());
        DebugLog.e(s);
        return s;
    }

    public String run(String url, Map<String, String> perameters, String imageTitle, ArrayList<String> pathArray, String fileMimeType) throws Exception {
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());

        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder
                .type(MultipartBuilder.FORM);
        if (pathArray != null) {
            if (pathArray.size() > 0) {
                if (pathArray.size() == 1) {
                    String path = pathArray.get(0);
                    multipartBuilder.addFormDataPart(imageTitle, "profile.png", RequestBody.create(MEDIA_TYPE_PNG, new File(path)));
                } else {

                    Filename filename = null;
                    String path = null;
                    for (int i = 0; i < pathArray.size(); i++) {
                        path = pathArray.get(i);
                        filename = new Filename(path, '/', '.');
                        multipartBuilder.addFormDataPart(imageTitle + "[" + i + "]", filename.extension(), RequestBody.create(MEDIA_TYPE_PNG, new File(path)));
                    }
                }
            }

        }

        for (Map.Entry<String, String> entry : perameters.entrySet()) {
            multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            DebugLog.e(entry.getKey() + ":" + entry.getValue());
        }

        RequestBody requestBody = multipartBuilder
                .build();

        Request request = new Request.Builder()
                .addHeader(Constants.KEY_HEADER_TOKEN, manager.getUser().getToken())
                .url(url)
                .post(requestBody)
                .build();
        DebugLog.e(request.body().contentLength() + "");

        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Error occurred " + response);
        String s = (response.body().string());
        DebugLog.e(s);
        return s;
    }

    public String run(String url, Map<String, String> perameters, String imageTitle, String pathArray) throws Exception {
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());

        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder
                .type(MultipartBuilder.FORM);

        if (pathArray != null)
            if (!pathArray.equals("")) {
                String path = pathArray;
                multipartBuilder.addFormDataPart(imageTitle, "profile.png", RequestBody.create(MEDIA_TYPE_PNG, new File(path)));
            }

        for (Map.Entry<String, String> entry : perameters.entrySet()) {
            multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            DebugLog.e(entry.getKey() + ":" + entry.getValue());
        }

        RequestBody requestBody = multipartBuilder
                .build();

        Request request = new Request.Builder()
                .addHeader(Constants.KEY_HEADER_TOKEN, manager.getUser().getToken())
                .url(url)
                .post(requestBody)
                .build();
        DebugLog.e(request.body().contentLength() + "");

        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Error occurred " + response);
        String s = (response.body().string());
        DebugLog.e(s);
        return s;
    }

}
