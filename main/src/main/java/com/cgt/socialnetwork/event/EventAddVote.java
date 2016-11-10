package com.cgt.socialnetwork.event;

/**
 * Created by CGTechnosoft
 */
public class EventAddVote {

    private boolean isSuccess;

    public EventAddVote(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
