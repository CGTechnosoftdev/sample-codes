package com.cgt.socialnetwork.event;

import com.cgt.socialnetwork.model.FeedBean;

/**
 * Created by CGTechnosoft
 */
public class EventNewPost {

    private boolean isSuccess;
    private FeedBean post;

    public EventNewPost(boolean isSuccess, FeedBean post) {
        this.isSuccess = isSuccess;
        this.post = post;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public FeedBean getPost() {
        return post;
    }
}
