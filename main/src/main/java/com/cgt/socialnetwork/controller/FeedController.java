package com.cgt.socialnetwork.controller;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.cgt.socialnetwork.job.JobAddVote;
import com.cgt.socialnetwork.job.BaseJob;
import com.cgt.socialnetwork.job.JobFetchComment;
import com.cgt.socialnetwork.job.JobFetchFeed;
import com.cgt.socialnetwork.job.JobFetchHashTagFeed;
import com.cgt.socialnetwork.job.JobFetchPublicFeed;
import com.cgt.socialnetwork.job.JobFetchSearchFeed;
import com.cgt.socialnetwork.job.JobReadNotification;
import com.cgt.socialnetwork.job.JobSaveEditPost;
import com.cgt.socialnetwork.job.JobSaveNewComment;
import com.cgt.socialnetwork.job.JobSaveNewPost;
import com.cgt.socialnetwork.model.Comment;
import com.cgt.socialnetwork.model.FeedBean;

/**
 * Created by CGTechnosoft
 */
public class FeedController {

    private JobManager jobManager;
    private Context context;

    public FeedController(JobManager jobManager, Context context) {
        this.jobManager = jobManager;
        this.context = context;
    }

    public void fetchFeedsAsync(boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, true, false));
    }

    public void loadMoreFeedsAsync(boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, false, true));
    }

    public void fetchPublicFeedsAsync(int userId, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchPublicFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, userId, true, false));
    }

    public void loadMorePublicFeedsAsync(int userId, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchPublicFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, userId, false, true));
    }

    public void fetchSearchFeedsAsync(String searchText, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchSearchFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, searchText, true, false));
    }

    public void loadMoreSearchFeedsAsync(String searchText, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchSearchFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, searchText, false, true));
    }

    public void fetchCommentsAsync(int postId, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchComment(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, postId, true, false));
    }

    public void loadMoreCommentsAsync(int postId, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchComment(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, postId, false, true));
    }

    public void fetchHashTagFeedsAsync(String hashTag, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchHashTagFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, hashTag, true, false));
    }

    public void loadMoreHashTagFeedsAsync(String hashTag, boolean fromUI) {
        jobManager.addJobInBackground(
                new JobFetchHashTagFeed(fromUI ? BaseJob.UI_HIGH : BaseJob.BACKGROUND, hashTag, false, true));
    }

    public void sendPostAsync(FeedBean feedBean) {
        jobManager.addJobInBackground(new JobSaveNewPost(feedBean));
    }

    public void sendCommentAsync(Comment comment) {
        jobManager.addJobInBackground(new JobSaveNewComment(comment));
    }

    public void sendLike(FeedBean feedBean) {
        jobManager.addJobInBackground(new JobAddVote(feedBean));
    }

    public void sendEditPostAsync(FeedBean feedBean, String screenName) {
        jobManager.addJobInBackground(new JobSaveEditPost(feedBean, screenName));
    }

    public void readNotification(int notiId) {
        jobManager.addJobInBackground(new JobReadNotification(notiId));
    }
}
