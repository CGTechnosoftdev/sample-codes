package com.cgt.socialnetwork.job;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.common.RequestDispatcher;
import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.db.DataProviderContract;
import com.cgt.socialnetwork.event.EventAddVote;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.utils.DebugLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class JobAddVote extends BaseJob {

    private static final String GROUP = "new_post";
    private FeedBean feedBean;

    transient EventBus eventBus;
    transient RequestDispatcher request;
    transient Context context;

    public JobAddVote(FeedBean feedBean) {
        super(new Params(BACKGROUND).groupBy(GROUP).requireNetwork().persist());
        this.feedBean = feedBean;
    }

    @Override
    public void onAdded() {
        ContentValues values = new ContentValues();
        values.put(DataProviderContract.Feed.USER_LIKED, feedBean.getUserLike());
        values.put(DataProviderContract.Feed.VOTES, feedBean.getVotes());


        DBHelper.getInstance(context).updateFeed(feedBean.getPId(), values);
    }

    @Override
    public void onRun() throws Throwable {
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
        eventBus = manager.getEventBus();
        request = manager.getRequestDispatcher();
        context = manager.getContext();

        RequestBuilder requestBuilder = manager.getRequestBuilder();

        Map<String, String> params = requestBuilder.sendVote(manager.getUser().getUserId(), feedBean.getPId(), feedBean.getUserLike());

        try {
            String s = request.createPostRequest(Constants.ADD_VOTE, params);
            JSONObject jsonObject = new JSONObject(s);

            switch (jsonObject.getInt(Constants.KEY_CODE)) {
                case Constants.SUCCESS:
                    DebugLog.d("vote sent for post : " + feedBean.getPId());
                    eventBus.post(new EventAddVote(true));
                    break;
                case Constants.INVALID_REQUEST:
                    DebugLog.d("vote sending failed for post : " + feedBean.getPId());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            DebugLog.d("Problem occured while sending vote for post : " + feedBean.getPId());
            e.printStackTrace();
            eventBus.post(new EventAddVote(true));
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected int getRetryLimit() {
        return 2;
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
                                                     int maxRunCount) {
        if (shouldRetry(throwable)) {
            // For the purposes of the demo, just back off 250 ms.
            RetryConstraint constraint = RetryConstraint.createExponentialBackoff(runCount, 250);
            constraint.setApplyNewDelayToGroup(true);
            return constraint;
        }
        return RetryConstraint.CANCEL;
    }

}
