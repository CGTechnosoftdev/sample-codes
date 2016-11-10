package com.cgt.socialnetwork.event;

import com.cgt.socialnetwork.model.Comment;

/**
 * Created by CGTechnosoft
 */
public class EventNewComment {

    private boolean isSuccess;
    private Comment comment;

    public EventNewComment(boolean isSuccess, Comment comment) {
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
