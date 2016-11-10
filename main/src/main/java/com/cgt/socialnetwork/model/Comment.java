package com.cgt.socialnetwork.model;

import java.io.Serializable;

/**
 * Created by CGTechnosoft
 */

public class Comment implements Serializable {

    private int id;
    private String message;
    private long createdDate;
    private int userId;
    private String userName;
    private String userPhoto;

    private int postId;
    private double latitude;
    private double longitude;

    private int pending = 0;
    private String clientId;
    private String key = null;

    /**
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The created
     */
    public long getCreatedDate() {
        return createdDate;
    }

    /**
     * @param created The created
     */
    public void setCreatedDate(long created) {
        this.createdDate = created;
    }

    /**
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @param userId The user_id
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return The userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName The user_name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return The userPhoto
     */
    public String getUserPhoto() {
        return userPhoto;
    }

    /**
     * @param userPhoto The user_photo
     */
    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getPostId() {
        return postId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public boolean isPending() {
        return pending > 0;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getKey() {
        if (key == null) {
            key = clientId == null ? id + "_" + userId : clientId + "_" + userId;
        }

        return key;
    }
}
