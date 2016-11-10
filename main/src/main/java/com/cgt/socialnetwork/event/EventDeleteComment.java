package com.cgt.socialnetwork.event;

import com.cgt.socialnetwork.model.Comment;

/**
 * Created by CGTechnosoft
 */
public class EventDeleteComment {

    private boolean isSuccess;
    private Comment comment;

    public EventDeleteComment(boolean isSuccess, Comment comment) {
        this.isSuccess = isSuccess;
        this.comment = comment;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Comment getComment() {
        return comment;
    }
}
