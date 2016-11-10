package com.cgt.socialnetwork.common;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.controller.FeedController;
import com.cgt.socialnetwork.model.User;
import com.cgt.socialnetwork.utils.DebugLog;
import com.cgt.socialnetwork.utils.MasterGson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by CGTechnosoft
 */
public class AppModuleManager {

    private Context context;
    private MainApp application;
    private EventBus eventBus;
    private JobManager jobManager;
    private Gson gson;
    private Picasso picasso;
    private MasterGson masterGson;
    private OkHttpClient okHttpClient;
    private FeedController feedController;
    private User user;
    private RequestBuilder requestBuilder;
    private RequestDispatcher requestDispatcher;

    private final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

    private static AppModuleManager instance = null;

    private AppModuleManager(Context context) {
        this.context = context.getApplicationContext();
        application = MainApp.getInstance();
        eventBus = eventBus();
        jobManager = jobManager();
        okHttpClient = createOkHttpClient(application);
        okHttpClient.setConnectTimeout(45, TimeUnit.SECONDS); // connect timeout
        okHttpClient.setWriteTimeout(45, TimeUnit.SECONDS);    // socket timeout
        okHttpClient.setReadTimeout(45, TimeUnit.SECONDS);    // socket timeout

        picasso = providePicasso(application, okHttpClient);
        gson = provideGson();
        masterGson = new MasterGson(gson);
        feedController = new FeedController(jobManager, this.context);
        requestBuilder = new RequestBuilder(context);
        requestDispatcher = new RequestDispatcher(okHttpClient);

        try {
            user = (User) masterGson.createPOJOfromString(Preference.getInstance(context).getString(Constants.KEY_USER_DATA), User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AppModuleManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppModuleManager(context);
        }

        return instance;
    }

    public void reset() {
        instance = null;
    }

    public void stopRunningJobs() {
        jobManager.stop();
    }

    private JobManager jobManager() {
        Configuration config = new Configuration.Builder(context)
                .consumerKeepAlive(45)
                .maxConsumerCount(3)
                .minConsumerCount(1)
                .build();

        return new JobManager(config);
    }

    private EventBus eventBus() {
        return new EventBus();
    }

    private Picasso providePicasso(Application application, OkHttpClient client) {
        return new Picasso.Builder(application)
                .downloader(new OkHttpDownloader(client))
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e) {
                        DebugLog.e(e.toString() + "Failed to load image: %s" + uri);
                    }
                })
                .build();
    }

    private OkHttpClient createOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(30, SECONDS);
        client.setReadTimeout(30, SECONDS);
        client.setWriteTimeout(30, SECONDS);

        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        client.setCache(cache);
        return client;
    }

    private Gson provideGson() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    }

    public Context getContext() {
        return context;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public Picasso getPicasso() {
        return picasso;
    }

    public MasterGson getMasterGson() {
        return masterGson;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FeedController getFeedController() {
        return feedController;
    }

    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public RequestDispatcher getRequestDispatcher() {
        return requestDispatcher;
    }
}
