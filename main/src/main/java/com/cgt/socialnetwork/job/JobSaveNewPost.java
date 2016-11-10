package com.cgt.socialnetwork.job;

import android.content.Context;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.DateTimeUtil;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.RequestDispatcher;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.event.EventNewPost;
import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.utils.DebugLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class JobSaveNewPost extends BaseJob {

    private static final String GROUP = "new_post";
    private FeedBean feedBean;

    transient EventBus eventBus;
    transient RequestDispatcher request;
    transient Context context;

    public JobSaveNewPost(FeedBean feedBean) {
        super(new Params(BACKGROUND).groupBy(GROUP).requireNetwork().persist().delayInMs(5000));
        this.feedBean = feedBean;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        // insert or update into post table with status posted -- (Send event on post screen)
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
        eventBus = manager.getEventBus();
        request = manager.getRequestDispatcher();
        context = manager.getContext();

        RequestBuilder requestBuilder = manager.getRequestBuilder();

        Map<String, String> params = requestBuilder.addPost(feedBean);

        try {
            String s = request.createMultipartRequest(Constants.ADD_POST, params, "image", feedBean.getImage());
            JSONObject jsonObject = new JSONObject(s);

            switch (jsonObject.getInt(Constants.KEY_CODE)) {
                case Constants.SUCCESS:
                    int oldPostId = Integer.parseInt(feedBean.getPId());

                    JSONObject jsonData = jsonObject.getJSONObject(Constants.KEY_DATA);
                    int newPostId = jsonData.getInt(Constants.KEY_ID);
                    feedBean.setPId("" + newPostId);
                    feedBean.setImage(jsonData.getString(Constants.KEY_POST_IMAGE));
                    feedBean.setCreatedDate(DateTimeUtil.convertUTCToGMT(jsonData.getString(Constants.KEY_CREATED_DATE)));
                    feedBean.setModifiedDate(DateTimeUtil.convertUTCToGMT(jsonData.getString(Constants.KEY_MODIFIED_DATE)));
                    feedBean.setPending(0);

                    DBHelper.getInstance(context).updateFeed(oldPostId, feedBean);
                    eventBus.post(new EventNewPost(true, feedBean));
                    break;
                default:
                    if (jsonObject.has(Constants.KEY_MESSAGE) && !jsonObject.isNull(Constants.KEY_MESSAGE)) {
                        //showToast(response.getString(Constants.JSON_KEY_MSG));
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventBus.post(new EventNewPost(false, null));
            DebugLog.d("Problem occured while adding post");
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected int getRetryLimit() {
        return 5;
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
