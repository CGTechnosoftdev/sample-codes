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
import com.cgt.socialnetwork.event.EventDeleteComment;
import com.cgt.socialnetwork.event.EventNewComment;
import com.cgt.socialnetwork.model.Comment;
import com.cgt.socialnetwork.utils.DebugLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class JobSaveNewComment extends BaseJob {

    private static final String GROUP = "new_comment";
    private Comment comment;

    transient EventBus eventBus;
    transient RequestDispatcher request;
    transient Context context;

    public JobSaveNewComment(Comment comment) {
        super(new Params(BACKGROUND).groupBy(GROUP).requireNetwork().persist().delayInMs(5000));
        this.comment = comment;
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

        Map<String, String> params = requestBuilder.addComment(comment);

        try {
            String s = request.createPostRequest(Constants.ADD_COMMENT, params);
            JSONObject jsonObject = new JSONObject(s);

            switch (jsonObject.getInt(Constants.KEY_CODE)) {
                case Constants.SUCCESS:
                    int oldCommentId = comment.getId();
                    JSONObject jsonData = jsonObject.getJSONObject(Constants.KEY_DATA);
                    int newPostId = jsonData.getInt(Constants.KEY_ID);
                    comment.setId(newPostId);
                    comment.setCreatedDate(DateTimeUtil.convertUTCToGMT(jsonData.getString(Constants.KEY_CREATED_DATE)));
                    comment.setPending(0);
                    DBHelper.getInstance(context).updateComment(oldCommentId, comment);
                    eventBus.post(new EventNewComment(true, comment));
                    break;
                case Constants.INVALID_REQUEST:
                    break;
                case Constants.USER_BLOCK:
                    DBHelper.getInstance(context).deleteComment(comment.getId());
                    eventBus.post(new EventDeleteComment(true, comment));
                    break;
                default:
                    if (jsonObject.has(Constants.KEY_MESSAGE) && !jsonObject.isNull(Constants.KEY_MESSAGE)) {
                        //showToast(response.getString(Constants.JSON_KEY_MSG));
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            eventBus.post(new EventNewComment(false, null));
            DebugLog.d("Problem occured while adding comment");
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
