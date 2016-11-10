package com.cgt.socialnetwork.model;

import java.io.Serializable;

/**
 * Created by CGTechnosoft
 */
public class FeedBean implements Serializable {

    public FeedBean() {
    }

    boolean isMedia;

    private String uCity;
    private String country;

    private String pId;
    private String pImage;
    private String pContent;
    private String pTags;
    private String uId;
    private String uName;
    private String uPhoto = "";
    private String type;

    private int pending = 0;
    private long createdDate;
    private long modifiedDate;
    private String key = null;
    private int votes;
    private int comments;
    private double latitute;
    private double longitute;
    private String clientId;
    private Integer userLike = 0;

    public String getCity() {
        return uCity;
    }

    public void setCity(String uCity) {
        this.uCity = uCity;
    }

    public Integer getUserLike() {
        return userLike;
    }

    public void setUserLike(Integer userLike) {
        this.userLike = userLike;
    }

    public String getImage() {
        return pImage;
    }

    public void setImage(String pImage) {
        this.pImage = pImage;
    }

    public String getPId() {
        return pId;
    }

    public void setPId(String pId) {
        this.pId = pId;
    }

    public String getPContent() {
        return pContent;
    }

    public void setPContent(String pContent) {
        this.pContent = pContent;
    }


    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public String getUName() {
        return uName;
    }

    public void setUName(String uName) {
        this.uName = uName;
    }

    public String getUPhoto() {
        return uPhoto;
    }

    public void setUPhoto(String uPhoto) {
        this.uPhoto = uPhoto;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public boolean isMedia() {
        return isMedia;
    }

    public void setMedia(boolean isMedia) {
        this.isMedia = isMedia;
    }

    public String getTag() {
        return pTags;
    }

    public void setTag(String tag) {
        this.pTags = tag;
    }

    public boolean isPending() {
        return pending > 0;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getKey() {
        if (key == null) {
            key = clientId == null ? pId + "_" + uId : clientId + "_" + uId;
        }

        return key;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

    public double getLongitute() {
        return longitute;
    }

    public void setLongitute(double longitute) {
        this.longitute = longitute;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
