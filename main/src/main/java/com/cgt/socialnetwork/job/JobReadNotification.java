package com.cgt.socialnetwork.job;

import android.content.Context;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.common.RequestDispatcher;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.utils.DebugLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class JobReadNotification extends BaseJob {

    private static final String GROUP = "read_notification";
    private int notiId;

    transient EventBus eventBus;
    transient RequestDispatcher request;
    transient Context context;

    public JobReadNotification(int notiId) {
        super(new Params(BACKGROUND).groupBy(GROUP).requireNetwork().persist());
        this.notiId = notiId;
    }

    @Override
    public void onAdded() {
        // update notification table to mark notification is read
        DBHelper.getInstance(context).updateNotification(notiId);
    }

    @Override
    public void onRun() throws Throwable {
        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
        eventBus = manager.getEventBus();
        request = manager.getRequestDispatcher();
        context = manager.getContext();

        RequestBuilder requestBuilder = manager.getRequestBuilder();

        Map<String, String> params = requestBuilder.readNotification(notiId);

        try {
            String s = request.createPostRequest(Constants.READ_NOTIFICATION, params);
            JSONObject jsonObject = new JSONObject(s);

            switch (jsonObject.getInt(Constants.KEY_CODE)) {
                case Constants.SUCCESS:
                    DebugLog.d("read notification sent for : " + notiId);
                    break;
                case Constants.INVALID_REQUEST:
                    DebugLog.d("read notification failed for : " + notiId);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            DebugLog.d("Problem occurred while reading notification : " + notiId);
            e.printStackTrace();
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
