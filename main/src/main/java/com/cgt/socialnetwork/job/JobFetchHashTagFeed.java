package com.cgt.socialnetwork.job;

import android.content.Context;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.DateTimeUtil;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.RequestDispatcher;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.event.EventFetchedHashTagFeed;
import com.cgt.socialnetwork.utils.DebugLog;
import com.cgt.socialnetwork.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class JobFetchHashTagFeed extends BaseJob {

    private static final String GROUP = "JobFetchHashTagFeed";

    transient EventBus eventBus;
    transient RequestDispatcher request;
    transient Context context;

    private String hashTag;
    private boolean reload;
    private boolean loadMore;

    public JobFetchHashTagFeed(@Priority int priority, String hashTag, boolean reload, boolean loadMore) {
        super(new Params(priority).addTags(GROUP).requireNetwork().persist());
        this.hashTag = hashTag;
        this.reload = reload;
        this.loadMore = loadMore;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
        eventBus = manager.getEventBus();
        request = manager.getRequestDispatcher();
        context = manager.getContext();

        RequestBuilder requestBuilder = manager.getRequestBuilder();
        Map<String, String> params = reload ? requestBuilder.getHashTagFeeds(hashTag) : requestBuilder.loadMoreHashTagFeeds(hashTag);

        try {
            String s = request.createGetRequest(Constants.GET_POST_BY_HASHTAG, params);
            JSONObject jsonObject = new JSONObject(s);
            int code = jsonObject.getInt(Constants.KEY_CODE);
            long reference = 0l;
            switch (code) {
                case Constants.SUCCESS:
                    JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_DATA);
                    if (jsonArray.length() > 0) {
                        // Save until date on every load more request
                        String date = Utils.getJsonValue((JSONObject) jsonArray.get(jsonArray.length() - 1), Constants.KEY_MODIFIED_DATE);
                        Preference.getInstance(context).put(Constants.PREF_KEY_HASHTAG_FEED_UNTIL_DATE, date);
                        reference = DateTimeUtil.convertUTCToGMT(date);

                        DBHelper.getInstance(context).saveHashTagFeeds(jsonArray);
                        eventBus.post(new EventFetchedHashTagFeed(true, reference, null, reload, loadMore));
                    }
                    break;
                case 2:
                    break;
                case 9:
                    eventBus.post(new EventFetchedHashTagFeed(false, 0, null, reload, loadMore));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventBus.post(new EventFetchedHashTagFeed(false, 0, null, reload, loadMore));
            DebugLog.d("Problem occurred while fetching feeds");
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    public RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                  int maxRunCount) {
        if (shouldRetry(throwable)) {
            return RetryConstraint.createExponentialBackoff(runCount, 1000);
        }
        return RetryConstraint.CANCEL;
    }

    @Override
    protected int getRetryLimit() {
        return 2;
    }
}
