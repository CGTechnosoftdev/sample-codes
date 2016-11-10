package com.cgt.socialnetwork.job;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
import com.cgt.socialnetwork.event.EventFetchedComment;
import com.cgt.socialnetwork.utils.DebugLog;
import com.cgt.socialnetwork.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class JobFetchComment extends BaseJob {

    private static final String GROUP = "JobFetchComment";

    transient EventBus eventBus;
    transient RequestDispatcher request;
    transient Context context;

    private int postId;
    private boolean reload;
    private boolean loadMore;

    public JobFetchComment(@Priority int priority, int postId, boolean reload, boolean loadMore) {
        super(new Params(priority).addTags(GROUP).requireNetwork().persist());
        this.postId = postId;
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
        Map<String, String> params = reload ? requestBuilder.getComments(postId) : requestBuilder.loadMoreComments(postId);

        try {
            String s = request.createGetRequest(Constants.GET_POST_COMMENTS, params);
            JSONObject jsonObject = new JSONObject(s);
            int code = jsonObject.getInt(Constants.KEY_CODE);
            long reference = 0l;
            switch (code) {
                case 1:
                    JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_DATA);
                    if (jsonArray.length() > 0) {
                        if (reload) {
                            // Save modified date of latest feed on every reload request
                            String date = Utils.getJsonValue((JSONObject) jsonArray.get(0), Constants.KEY_CREATED_DATE);
                            Preference preference = Preference.getInstance(context);
                            preference.put(Constants.PREF_KEY_COMMENT_SINCE_DATE, date);
                            reference = DateTimeUtil.convertUTCToGMT(date);

                            if (TextUtils.isEmpty(preference.getString(Constants.PREF_KEY_COMMENT_UNTIL_DATE))) {
                                date = Utils.getJsonValue((JSONObject) jsonArray.get(jsonArray.length() - 1), Constants.KEY_CREATED_DATE);
                                preference.put(Constants.PREF_KEY_COMMENT_UNTIL_DATE, date);
                            }
                        } else if (loadMore) {
                            // Save until date on every load more request
                            String date = Utils.getJsonValue((JSONObject) jsonArray.get(jsonArray.length() - 1), Constants.KEY_CREATED_DATE);
                            Preference.getInstance(context).put(Constants.PREF_KEY_COMMENT_UNTIL_DATE, date);
                            reference = DateTimeUtil.convertUTCToGMT(date);
                        }

                        DBHelper.getInstance(context).saveComments(postId, jsonArray);
                        eventBus.post(new EventFetchedComment(true, reference, null, reload, loadMore));
                    }
                    break;
                case 2:
                    break;
                case 9:
                    eventBus.post(new EventFetchedComment(false, 0, null, reload, loadMore));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventBus.post(new EventFetchedComment(false, 0, null, reload, loadMore));
            DebugLog.d("Problem occurred while fetching comments");
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
