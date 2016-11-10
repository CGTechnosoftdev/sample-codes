package com.cgt.socialnetwork.common;

import android.content.Context;
import android.text.TextUtils;

import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.model.Comment;
import com.cgt.socialnetwork.model.User;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

/**
 * Created by CGTechnosoft
 */
@Singleton
public class RequestBuilder {

    private Context context;
    private Preference preference = null;
    private Map<String, String> params;

    public RequestBuilder(Context context) {
        this.context = context;
        preference = Preference.getInstance(context);
        params = new HashMap();
    }

    public synchronized Map<String, String> login(String email, String psw, String device_id, String push_token) {
        params.clear();

        params.put(Constants.KEY_EMAIL, email);
        params.put(Constants.KEY_PASSWORD, psw);
        params.put(Constants.KEY_DEVICE_ID, device_id);
        params.put(Constants.KEY_DEVICE_TYPE, Constants.KEY_DEVICE_TYPE_VALUE);
        params.put(Constants.KEY_PUSH_TOKEN, push_token);

        return params;
    }

    public synchronized Map<String, String> loginAsFacebook(String facebookId, String email, String name, String gender, String device_id, String push_token) {
        params.clear();

        params.put(Constants.KEY_FACEBOOK_ID, facebookId);
        params.put(Constants.KEY_EMAIL, email);
        params.put(Constants.KEY_NAME, name);
        params.put(Constants.KEY_GENDER, gender.equalsIgnoreCase("male") ? "1" : "2");
        params.put(Constants.KEY_DEVICE_ID, device_id);
        params.put(Constants.KEY_DEVICE_TYPE, Constants.KEY_DEVICE_TYPE_VALUE);
        params.put(Constants.KEY_PUSH_TOKEN, push_token);

        return params;
    }

    public synchronized Map<String, String> forgotPassword(String email) {
        params.clear();

        params.put(Constants.KEY_EMAIL, email);

        return params;
    }

    public synchronized Map<String, String> createUser(String email, String psw, String name, int gender, String device_id, String push_token) {
        params.clear();

        params.put(Constants.KEY_EMAIL, email);
        params.put(Constants.KEY_PASSWORD, psw);
        params.put(Constants.KEY_NAME, name);
        params.put(Constants.KEY_GENDER, "" + gender);
        params.put(Constants.KEY_DEVICE_ID, device_id);
        params.put(Constants.KEY_DEVICE_TYPE, Constants.KEY_DEVICE_TYPE_VALUE);
        params.put(Constants.KEY_PUSH_TOKEN, push_token);

        return params;
    }

    public synchronized Map<String, String> getFeeds() {
        params.clear();

        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        String sinceDate = preference.getString(Constants.PREF_KEY_FEED_SINCE_DATE);
        if (!TextUtils.isEmpty(sinceDate))
            params.put(Constants.KEY_PARAM_SINCE_DATE, sinceDate);

        return params;
    }

    public synchronized Map<String, String> loadMoreFeeds() {
        params.clear();

        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        params.put(Constants.KEY_PARAM_UNTIL_DATE, preference.getString(Constants.PREF_KEY_FEED_UNTIL_DATE));

        return params;
    }

    public synchronized Map<String, String> getSearchFeeds(String searchText) {
        params.clear();

        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        params.put(Constants.KEY_PARAM_SEARCH_DATA, searchText);

        return params;
    }

    public synchronized Map<String, String> loadMoreSearchFeeds(String searchText) {
        params.clear();

        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        params.put(Constants.KEY_PARAM_SEARCH_DATA, searchText);
        params.put(Constants.KEY_PARAM_UNTIL_DATE, preference.getString(Constants.PREF_KEY_SEARCH_FEED_UNTIL_DATE));

        return params;
    }

    public synchronized Map<String, String> getPublicFeeds(int userId) {
        params.clear();

        params.put(Constants.KEY_PARAM_USER_ID, "" + userId);
        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        String sinceDate = preference.getString(Constants.PREF_KEY_PUBLIC_FEED_SINCE_DATE);
        if (!TextUtils.isEmpty(sinceDate))
            params.put(Constants.KEY_PARAM_SINCE_DATE, sinceDate);

        return params;
    }

    public synchronized Map<String, String> loadMorePublicFeeds(int userId) {
        params.clear();

        params.put(Constants.KEY_PARAM_USER_ID, "" + userId);
        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        params.put(Constants.KEY_PARAM_UNTIL_DATE, preference.getString(Constants.PREF_KEY_PUBLIC_FEED_UNTIL_DATE));

        return params;
    }

    public synchronized Map<String, String> getComments(int postId) {
        params.clear();

        params.put(Constants.KEY_PARAM_POST_ID, "" + postId);
        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        String sinceDate = preference.getString(Constants.PREF_KEY_COMMENT_SINCE_DATE);
        if (!TextUtils.isEmpty(sinceDate))
            params.put(Constants.KEY_PARAM_SINCE_DATE, sinceDate);

        return params;
    }

    public synchronized Map<String, String> loadMoreComments(int postId) {
        params.clear();

        params.put(Constants.KEY_PARAM_POST_ID, "" + postId);
        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        params.put(Constants.KEY_PARAM_UNTIL_DATE, preference.getString(Constants.PREF_KEY_COMMENT_UNTIL_DATE));

        return params;
    }

    public synchronized Map<String, String> addPost(FeedBean feedBean) {
        params.clear();

        params.put(Constants.KEY_PARAM_CONTENT, StringEscapeUtils.escapeJava(feedBean.getPContent()));
        params.put(Constants.KEY_PARAM_LATITUDE, "" + feedBean.getLatitute());
        params.put(Constants.KEY_PARAM_LONGITUDE, "" + feedBean.getLongitute());
        params.put(Constants.KEY_HASH_TAG, feedBean.getTag());

        return params;
    }

    public synchronized Map<String, String> addComment(Comment comment) {
        params.clear();

        params.put(Constants.KEY_PARAM_POST_ID, "" + comment.getPostId());
        params.put(Constants.KEY_MESSAGE, "" + StringEscapeUtils.escapeJava(comment.getMessage()));
        params.put(Constants.KEY_PARAM_LATITUDE, "" + comment.getLatitude());
        params.put(Constants.KEY_PARAM_LONGITUDE, "" + comment.getLongitude());

        return params;
    }

    public synchronized Map<String, String> deletePost(String postId) {
        params.clear();

        params.put(Constants.KEY_POST_ID, postId);

        return params;
    }

    public synchronized Map<String, String> deleteComment(int commentId) {
        params.clear();

        params.put(Constants.KEY_COMMENT_ID, "" + commentId);

        return params;
    }

    public synchronized Map<String, String> editPost(FeedBean feedBean) {
        params.clear();

        params.put(Constants.KEY_POST_ID, feedBean.getPId());
        params.put(Constants.KEY_PARAM_CONTENT, StringEscapeUtils.escapeJava(feedBean.getPContent()));
        params.put(Constants.KEY_HASH_TAG, feedBean.getTag());

        return params;
    }

    public synchronized Map<String, String> getProfile(String userId) {
        params.clear();

        params.put(Constants.KEY_USER_ID, userId);

        return params;
    }

    public synchronized Map<String, String> editProfile(User user) {
        params.clear();

        params.put(Constants.KEY_NAME, user.getUserName());
        params.put(Constants.KEY_EMAIL, user.getEmail());
        params.put(Constants.KEY_GENDER, "" + user.getGender());
        params.put(Constants.KEY_CITY, user.getCity());
        params.put(Constants.KEY_COUNTRY, user.getCountry());
        params.put(Constants.KEY_USER_STATUS, StringEscapeUtils.escapeJava(user.getUserStatus()));
        params.put(Constants.KEY_IS_PRIVATE, "" + user.getIsPrivate());

        return params;
    }

    public synchronized Map<String, String> sendVote(int userId, String postId, int vote) {
        params.clear();

        params.put(Constants.KEY_PARAM_POST_ID, postId);
        params.put(Constants.KEY_PARAM_USER_ID, "" + userId);
        params.put(Constants.KEY_PARAM_VOTE, "" + vote);

        return params;
    }

    public synchronized Map<String, String> readNotification(int notiId) {
        params.clear();

        params.put(Constants.KEY_NOTIFICATION_ID, "" + notiId);

        return params;
    }

    public synchronized Map<String, String> changePassword(String oldPass, String newPass) {
        params.clear();

        params.put(Constants.KEY_PARAM_OLD_PASSWORD, oldPass);
        params.put(Constants.KEY_PARAM_NEW_PASSWORD, newPass);

        return params;
    }

    public synchronized Map<String, String> accountActivation(String email, String userId) {
        params.clear();

        params.put(Constants.KEY_EMAIL, email);
        params.put(Constants.KEY_PARAM_USER_ID, userId);

        return params;
    }

    public synchronized Map<String, String> getHashTagFeeds(String hashTag) {
        params.clear();

        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        params.put(Constants.KEY_HASH_TAG, hashTag);

        return params;
    }

    public synchronized Map<String, String> loadMoreHashTagFeeds(String hashTag) {
        params.clear();

        params.put(Constants.KEY_PARAM_PAGE_SIZE, Constants.FEEDS_PAGE_SIZE);
        params.put(Constants.KEY_HASH_TAG, hashTag);
        params.put(Constants.KEY_PARAM_UNTIL_DATE, preference.getString(Constants.PREF_KEY_HASHTAG_FEED_UNTIL_DATE));

        return params;
    }

    public synchronized Map<String, String> getSearchHashTag(String hashTag) {
        params.clear();

        params.put(Constants.KEY_HASH_TAG, hashTag);

        return params;
    }
}
