package com.cgt.socialnetwork.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

/**
 * Created by CGTechnosoft
 */
@Generated("org.jsonschema2pojo")
public class User {

    @SerializedName("id")
    @Expose
    private int userId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("user_photo")
    @Expose
    private String userPhoto;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("gender")
    @Expose
    private int gender; //0-nothing, 1-male, 2-female
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("type")
    @Expose
    private String userType;
    @SerializedName("facebook_id")
    @Expose
    private String facebookId;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("user_status")
    @Expose
    private String userStatus;
    @SerializedName("is_private")
    @Expose
    private int isPrivate;

    /**
     *
     * @return
     * The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     * The user_id
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The userPhoto
     */
    public String getUserPhoto() {
        return userPhoto;
    }

    /**
     *
     * @param userPhoto
     * The user_photo
     */
    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    /**
     *
     * @return
     * The userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @param userName
     * The user_name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @return
     * The userName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @param userName
     * The user_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @return
     * The userName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @param userName
     * The user_name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *
     * @return
     * The gender
     */
    public int getGender() {
        return gender;
    }

    /**
     *
     * @param gender
     * The gender
     */
    public void setGender(int gender) {
        this.gender = gender;
    }

    /**
     *
     * @return
     * The token
     */
    public String getToken() {
        return token;
    }

    /**
     *
     * @param token
     * The token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     *
     * @return
     * The userType
     */
    public String getUserType() {
        return userType;
    }

    /**
     *
     * @param userType
     * The type
     */
    public void setUserType(String type) {
        this.userType = type;
    }

    /**
     *
     * @return
     * The facebookId
     */
    public String getFacebookId() {
        return facebookId;
    }

    /**
     *
     * @param facebookId
     * The facebook_id
     */
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    /**
     *
     * @return
     * The type
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * @param city
     * The city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     * @return
     * The city
     */
    public String getCountry() {
        return country;
    }

    /**
     *
     * @param country
     * The country
     */
    public void setCountry(String country) {
        this.country = country;
    }


    /**
     *
     * @return
     * The userStatus
     */
    public String getUserStatus() {
        return userStatus;
    }

    /**
     *
     * @param userStatus
     * The user_status
     */
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }


    /**
     *
     * @return
     * The isPrivate
     */
    public int getIsPrivate() {
        return isPrivate;
    }

    /**
     *
     * @param isPrivate
     * The is_private
     */
    public void setIsPrivate(int isPrivate) {
        this.isPrivate = isPrivate;
    }

}
