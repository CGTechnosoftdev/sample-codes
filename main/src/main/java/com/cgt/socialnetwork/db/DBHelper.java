package com.cgt.socialnetwork.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;

import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.DateTimeUtil;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.model.Comment;
import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CGTechnosoft
 */
public class DBHelper {

    private Context context = null;
    private static DBHelper instance = null;

    private DBHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }

        return instance;
    }

    /**
     * Helper Method to save Notification data into database table
     *
     * @param jsonArray
     */
    public void saveNotificationData(JSONArray jsonArray) {
        int len = jsonArray.length();
        ContentValues values = new ContentValues();
        JSONObject object, objPost = null;
        for (int i = 0; i < len; i++) {
            try {
                object = jsonArray.getJSONObject(i);
                values.put(DataProviderContract.Notification.NOTIFICATION_ID, object.getInt(Constants.KEY_ID));

                int userId = object.getInt(Constants.KEY_USER_ID);
                values.put(DataProviderContract.Notification.USER_ID, userId);
                values.put(DataProviderContract.Notification.POST_ID, object.getInt(Constants.KEY_POST_ID));
                values.put(DataProviderContract.Notification.IS_READ, object.getInt(Constants.KEY_IS_READ));

                int postUserId = Utils.getJsonValueInt(object, Constants.KEY_POST_USER_ID);
                values.put(DataProviderContract.Notification.POST_USER_ID, postUserId);

                int action = object.getInt(Constants.KEY_ACTION);
                values.put(DataProviderContract.Notification.ACTION, action);
                values.put(DataProviderContract.Notification.CONTENT, getNotificationContent(action, userId, postUserId));

                long modifiedDate = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
                values.put(DataProviderContract.Notification.TIMESTAMP, modifiedDate);
                values.put(DataProviderContract.Notification.USER_NAME, object.getString(Constants.KEY_USER_NAME));
                values.put(DataProviderContract.Notification.USER_PHOTO, object.getString(Constants.KEY_USER_PHOTO));

                context.getContentResolver().insert(DataProviderContract.Notification.CONTENT_URI, values);

                values.clear();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public int getUnreadNotificationCount() {
        Cursor cursor = context.getContentResolver().query(DataProviderContract.Notification.CONTENT_URI, new String[]{"count(*)"},
                DataProviderContract.Notification.IS_READ + " = ?", new String[]{"0"}, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    private String getNotificationContent(int action, int userId, int postUserId) {
        String content = "";
        switch (action) {
            case Constants.TAG_CREATED:
                content = context.getString(R.string.msgPostCreated);
                break;
            case Constants.TAG_LIKED:
                content = context.getString(R.string.msgPostLiked);
                break;
            case Constants.TAG_REPLY:
                if (userId == postUserId)
                    content = context.getString(R.string.msgReplied);
                else
                    content = context.getString(R.string.msgRepliedYourPost);
                break;
        }

        return content;
    }

    public void updateNotification(int notiId) {
        try {
            ContentValues values = new ContentValues();
            values.put(DataProviderContract.Notification.IS_READ, 1);

            context.getContentResolver().update(DataProviderContract.Notification.CONTENT_URI, values,
                    DataProviderContract.Notification.NOTIFICATION_ID + " = ?",
                    new String[]{"" + notiId});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteNotificationById(String notiId) {
        context.getContentResolver().delete(DataProviderContract.Notification.CONTENT_URI,
                DataProviderContract.Notification.NOTIFICATION_ID + " = ?",
                new String[]{notiId});
    }

    /**
     * Helper Method to save Notification data into database table
     *
     * @param jsonArray
     */
    public void saveCountryData(JSONArray jsonArray) {
        int len = jsonArray.length();
        ContentValues values = new ContentValues();
        JSONObject object;
        for (int i = 0; i < len; i++) {
            try {
                object = jsonArray.getJSONObject(i);
                values.put(DataProviderContract.Country._ID, object.getString(Constants.KEY_ID));
                values.put(DataProviderContract.Country.COUNTRY_NAME, object.getString(Constants.KEY_COUNTRY_NAME));
                values.put(DataProviderContract.Country.ISO, object.getString(Constants.KEY_ISO));

                context.getContentResolver().insert(DataProviderContract.Country.CONTENT_URI, values);

                values.clear();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void saveFeeds(JSONArray jsonArray) {
        int len = jsonArray.length();
        ContentValues values = new ContentValues();
        JSONObject object;
        for (int i = 0; i < len; i++) {
            try {
                object = jsonArray.getJSONObject(i);
                String postId = object.getString(Constants.KEY_P_ID);
                Cursor cursor = context.getContentResolver().query(DataProviderContract.Feed.CONTENT_URI, new String[]{DataProviderContract.Feed.CONTENT},
                        DataProviderContract.Feed._ID + "=?", new String[]{postId}, null);
                boolean isExists = cursor.getCount() > 0;
                cursor.close();
                if (isExists) {
                    updateFeed(object, values);
                } else {
                    insertFeed(object, values);
                }

                values.clear();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateFeed(JSONObject object, ContentValues values) throws JSONException {
        String postId = object.getString(Constants.KEY_P_ID);
        values.put(DataProviderContract.Feed.CONTENT, object.getString(Constants.KEY_CONTENT));
        if (!TextUtils.isEmpty(object.getString(Constants.KEY_POST_IMAGE)))
            values.put(DataProviderContract.Feed.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.Feed.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.Feed.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.Feed.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.Feed.USER_LIKED, object.getInt(Constants.KEY_USER_LIKE));
        values.put(DataProviderContract.Feed.USER_CITY, object.getString(Constants.KEY_USER_CITY));
        values.put(DataProviderContract.Feed.USER_COUNTRY, object.getString(Constants.KEY_USER_COUNTRY));

        context.getContentResolver().update(DataProviderContract.Feed.CONTENT_URI, values,
                DataProviderContract.Feed._ID + "=?", new String[]{postId});
    }

    private void insertFeed(JSONObject object, ContentValues values) throws JSONException {
        values.put(DataProviderContract.Feed._ID, object.getString(Constants.KEY_P_ID));
        values.put(DataProviderContract.Feed.CONTENT, object.getString(Constants.KEY_CONTENT));
        values.put(DataProviderContract.Feed.TYPE, object.getString(Constants.KEY_TYPE));
        values.put(DataProviderContract.Feed.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.Feed.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.Feed.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        values.put(DataProviderContract.Feed.USER_CITY, object.getString(Constants.KEY_USER_CITY));
        values.put(DataProviderContract.Feed.USER_COUNTRY, object.getString(Constants.KEY_USER_COUNTRY));

        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_CREATED_DATE));
        values.put(DataProviderContract.Feed.CREATED_DATE, timeStamp);
        timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.Feed.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.Feed.USER_ID, object.getString(Constants.KEY_U_ID));
        values.put(DataProviderContract.Feed.USER_NAME, object.getString(Constants.KEY_U_NAME));
        values.put(DataProviderContract.Feed.USER_PHOTO, object.getString(Constants.KEY_U_PHOTO));
        int userLiked = Util.getJsonValueInt(object, Constants.KEY_USER_LIKE);
        values.put(DataProviderContract.Feed.USER_LIKED, userLiked);

        context.getContentResolver().insert(DataProviderContract.Feed.CONTENT_URI, values);
    }

    public void deleteFeedPost(String postId) {
        context.getContentResolver().delete(DataProviderContract.Feed.CONTENT_URI,
                DataProviderContract.Feed._ID + " = ?",
                new String[]{postId});
    }

    public void deleteFeeds(String postIds) {
        context.getContentResolver().delete(DataProviderContract.Feed.CONTENT_URI,
                DataProviderContract.Feed._ID + " IN (" + postIds + ")",
                null);
    }

    public void updateFeed(String postId, ContentValues values) {
        try {
            String selection = DataProviderContract.Feed._ID + " = ?";
            String[] selectionArgs = new String[]{"" + postId};

            context.getContentResolver().update(DataProviderContract.Feed.CONTENT_URI, values, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFeed(int oldPostId, FeedBean feedBean) {
        ContentValues values = new ContentValues();

        String selection = DataProviderContract.Feed._ID + " = ?";
        String[] selectionArgs = new String[]{"" + oldPostId};

        values.put(DataProviderContract.Feed._ID, feedBean.getPId());
        values.put(DataProviderContract.Feed.CONTENT, feedBean.getPContent());
        if (!TextUtils.isEmpty(feedBean.getImage()))
            values.put(DataProviderContract.Feed.IMAGE, feedBean.getImage());
        values.put(DataProviderContract.Feed.VOTES, feedBean.getVotes());
        values.put(DataProviderContract.Feed.CREATED_DATE, feedBean.getCreatedDate());
        values.put(DataProviderContract.Feed.MODIFIED_DATE, feedBean.getModifiedDate());
        values.put(DataProviderContract.Feed.STATUS, feedBean.isPending());
        context.getContentResolver().update(DataProviderContract.Feed.CONTENT_URI, values, selection, selectionArgs);
    }

    public synchronized List<FeedBean> getFeedsSince(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.Feed.CONTENT_URI, DataProviderContract.Feed.LIST_PROJECTION,
                DataProviderContract.Feed.MODIFIED_DATE + "> ?", new String[]{"" + reference}, DataProviderContract.Feed.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.Feed._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.Feed.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.Feed.IMAGE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.Feed.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.Feed.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.Feed.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.Feed.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.Feed.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.Feed.USER_NAME);
            int indexCity = cursor.getColumnIndex(DataProviderContract.Feed.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.Feed.USER_COUNTRY);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.Feed.USER_PHOTO);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.Feed.USER_LIKED);
            int indexClientId = cursor.getColumnIndex(DataProviderContract.Feed.CLIENT_ID);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.Feed.STATUS);

            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));
                feedBean.setClientId(cursor.getString(indexClientId));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }

    public synchronized List<FeedBean> getFeedsUntil(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.Feed.CONTENT_URI, DataProviderContract.Feed.LIST_PROJECTION,
                DataProviderContract.Feed.MODIFIED_DATE + "< ?", new String[]{"" + reference}, DataProviderContract.Feed.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.Feed._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.Feed.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.Feed.IMAGE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.Feed.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.Feed.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.Feed.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.Feed.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.Feed.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.Feed.USER_NAME);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.Feed.USER_PHOTO);
            int indexCity = cursor.getColumnIndex(DataProviderContract.Feed.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.Feed.USER_COUNTRY);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.Feed.USER_LIKED);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.Feed.STATUS);

            int indexClientId = cursor.getColumnIndex(DataProviderContract.Feed.CLIENT_ID);
            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));
                feedBean.setClientId(cursor.getString(indexClientId));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }


    public void updateFeed(FeedBean feedBean) {
        try {
            ContentValues values = new ContentValues();

            String selection = DataProviderContract.Feed._ID + " = ?";
            String[] selectionArgs = new String[]{"" + feedBean.getPId()};

            values.put(DataProviderContract.Feed.CONTENT, feedBean.getPContent());
            if (!TextUtils.isEmpty(feedBean.getImage()))
                values.put(DataProviderContract.Feed.IMAGE, feedBean.getImage());
            values.put(DataProviderContract.Feed.MODIFIED_DATE, feedBean.getModifiedDate());
            values.put(DataProviderContract.Feed.STATUS, feedBean.isPending());
            context.getContentResolver().update(DataProviderContract.Feed.CONTENT_URI, values, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void savePublicFeeds(JSONArray jsonArray) {
        int len = jsonArray.length();
        ContentValues values = new ContentValues();
        JSONObject object;
        for (int i = 0; i < len; i++) {
            try {
                object = jsonArray.getJSONObject(i);
                String postId = object.getString(Constants.KEY_P_ID);
                Cursor cursor = context.getContentResolver().query(DataProviderContract.PublicFeed.CONTENT_URI, new String[]{DataProviderContract.PublicFeed.CONTENT},
                        DataProviderContract.PublicFeed._ID + "=?", new String[]{postId}, null);
                boolean isExists = cursor.getCount() > 0;
                cursor.close();
                if (isExists) {
                    updatePublicFeed(object, values);
                } else {
                    insertPublicFeed(object, values);
                }

                values.clear();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePublicFeed(JSONObject object, ContentValues values) throws JSONException {
        String postId = object.getString(Constants.KEY_P_ID);
        values.put(DataProviderContract.PublicFeed.CONTENT, object.getString(Constants.KEY_CONTENT));
        if (!TextUtils.isEmpty(object.getString(Constants.KEY_POST_IMAGE)))
            values.put(DataProviderContract.PublicFeed.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.PublicFeed.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.PublicFeed.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.PublicFeed.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.PublicFeed.USER_LIKED, object.getInt(Constants.KEY_USER_LIKE));

        context.getContentResolver().update(DataProviderContract.PublicFeed.CONTENT_URI, values,
                DataProviderContract.PublicFeed._ID + "=?", new String[]{postId});
    }

    private void insertPublicFeed(JSONObject object, ContentValues values) throws JSONException {
        values.put(DataProviderContract.PublicFeed._ID, object.getString(Constants.KEY_P_ID));
        values.put(DataProviderContract.PublicFeed.CONTENT, object.getString(Constants.KEY_CONTENT));
        values.put(DataProviderContract.PublicFeed.TYPE, object.getString(Constants.KEY_TYPE));
        values.put(DataProviderContract.PublicFeed.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.PublicFeed.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.PublicFeed.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        values.put(DataProviderContract.PublicFeed.USER_CITY, object.getString(Constants.KEY_USER_CITY));
        values.put(DataProviderContract.PublicFeed.USER_COUNTRY, object.getString(Constants.KEY_USER_COUNTRY));


        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_CREATED_DATE));
        values.put(DataProviderContract.PublicFeed.CREATED_DATE, timeStamp);
        timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.PublicFeed.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.PublicFeed.USER_ID, object.getString(Constants.KEY_U_ID));
        values.put(DataProviderContract.PublicFeed.USER_NAME, object.getString(Constants.KEY_U_NAME));
        values.put(DataProviderContract.PublicFeed.USER_PHOTO, object.getString(Constants.KEY_U_PHOTO));
        int userLiked = Util.getJsonValueInt(object, Constants.KEY_USER_LIKE);
        values.put(DataProviderContract.PublicFeed.USER_LIKED, userLiked);

        context.getContentResolver().insert(DataProviderContract.PublicFeed.CONTENT_URI, values);
    }

    public void updatePublicFeed(FeedBean feedBean) {
        try {
            ContentValues values = new ContentValues();

            String selection = DataProviderContract.PublicFeed._ID + " = ?";
            String[] selectionArgs = new String[]{"" + feedBean.getPId()};

            values.put(DataProviderContract.PublicFeed.CONTENT, feedBean.getPContent());
            if (!TextUtils.isEmpty(feedBean.getImage()))
                values.put(DataProviderContract.PublicFeed.IMAGE, feedBean.getImage());
            values.put(DataProviderContract.PublicFeed.MODIFIED_DATE, feedBean.getModifiedDate());
            values.put(DataProviderContract.PublicFeed.STATUS, feedBean.isPending());
            context.getContentResolver().update(DataProviderContract.PublicFeed.CONTENT_URI, values, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<FeedBean> getPublicFeedsSince(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.PublicFeed.CONTENT_URI, DataProviderContract.PublicFeed.LIST_PROJECTION,
                DataProviderContract.PublicFeed.MODIFIED_DATE + "> ?", new String[]{"" + reference}, DataProviderContract.PublicFeed.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.PublicFeed._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.PublicFeed.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.PublicFeed.IMAGE);
            int indexLat = cursor.getColumnIndex(DataProviderContract.PublicFeed.LATITUDE);
            int indexLongi = cursor.getColumnIndex(DataProviderContract.PublicFeed.LONGITUDE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.PublicFeed.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.PublicFeed.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.PublicFeed.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.PublicFeed.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_NAME);
            int indexCity = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_COUNTRY);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_PHOTO);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_LIKED);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.PublicFeed.STATUS);

            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }

    public synchronized List<FeedBean> getPublicFeedsUntil(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.PublicFeed.CONTENT_URI, DataProviderContract.PublicFeed.LIST_PROJECTION,
                DataProviderContract.PublicFeed.MODIFIED_DATE + "< ?", new String[]{"" + reference}, DataProviderContract.PublicFeed.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.PublicFeed._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.PublicFeed.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.PublicFeed.IMAGE);
            int indexLat = cursor.getColumnIndex(DataProviderContract.PublicFeed.LATITUDE);
            int indexLongi = cursor.getColumnIndex(DataProviderContract.PublicFeed.LONGITUDE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.PublicFeed.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.PublicFeed.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.PublicFeed.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.PublicFeed.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_NAME);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_PHOTO);
            int indexCity = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_COUNTRY);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.PublicFeed.USER_LIKED);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.PublicFeed.STATUS);

            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }

    public void addFeed(FeedBean feedBean) {
        ContentValues values = new ContentValues();
        values.put(DataProviderContract.Feed._ID, feedBean.getPId());
        values.put(DataProviderContract.Feed.CONTENT, feedBean.getPContent());
        values.put(DataProviderContract.Feed.IMAGE, feedBean.getImage());
        values.put(DataProviderContract.Feed.LATITUDE, feedBean.getLatitute());
        values.put(DataProviderContract.Feed.LONGITUDE, feedBean.getLongitute());
        values.put(DataProviderContract.Feed.VOTES, feedBean.getVotes());
        values.put(DataProviderContract.Feed.COMMENTS, feedBean.getComments());
        values.put(DataProviderContract.Feed.CREATED_DATE, feedBean.getModifiedDate());
        values.put(DataProviderContract.Feed.MODIFIED_DATE, feedBean.getModifiedDate());
        values.put(DataProviderContract.Feed.USER_NAME, feedBean.getUName());
        values.put(DataProviderContract.Feed.USER_ID, feedBean.getUId());
        values.put(DataProviderContract.Feed.USER_PHOTO, feedBean.getUPhoto());
        values.put(DataProviderContract.Feed.USER_COUNTRY, feedBean.getCountry());
        values.put(DataProviderContract.Feed.STATUS, feedBean.isPending()); // 0-posted, 1-posting
        values.put(DataProviderContract.Feed.CLIENT_ID, feedBean.getClientId());
        context.getContentResolver().insert(DataProviderContract.Feed.CONTENT_URI, values);
    }

    public synchronized void saveSearchFeeds(JSONArray jsonArray) {
        int len = jsonArray.length();
        ContentValues values = new ContentValues();
        JSONObject object;
        for (int i = 0; i < len; i++) {
            try {
                object = jsonArray.getJSONObject(i);
                String postId = object.getString(Constants.KEY_P_ID);
                Cursor cursor = context.getContentResolver().query(DataProviderContract.SearchFeeds.CONTENT_URI, new String[]{DataProviderContract.SearchFeeds.CONTENT},
                        DataProviderContract.SearchFeeds._ID + "=?", new String[]{postId}, null);
                boolean isExists = cursor.getCount() > 0;
                cursor.close();
                if (isExists) {
                    updateSearchFeeds(object, values);
                } else {
                    insertSearchFeeds(object, values);
                }

                values.clear();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSearchFeeds(JSONObject object, ContentValues values) throws JSONException {
        String postId = object.getString(Constants.KEY_P_ID);
        values.put(DataProviderContract.SearchFeeds.CONTENT, object.getString(Constants.KEY_CONTENT));
        if (!TextUtils.isEmpty(object.getString(Constants.KEY_POST_IMAGE)))
            values.put(DataProviderContract.SearchFeeds.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.SearchFeeds.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.SearchFeeds.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.SearchFeeds.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.SearchFeeds.USER_LIKED, object.getInt(Constants.KEY_USER_LIKE));
        values.put(DataProviderContract.SearchFeeds.USER_COUNTRY, object.getString(Constants.KEY_USER_COUNTRY));

        context.getContentResolver().update(DataProviderContract.SearchFeeds.CONTENT_URI, values,
                DataProviderContract.SearchFeeds._ID + "=?", new String[]{postId});
    }

    private void insertSearchFeeds(JSONObject object, ContentValues values) throws JSONException {
        values.put(DataProviderContract.SearchFeeds._ID, object.getString(Constants.KEY_P_ID));
        values.put(DataProviderContract.SearchFeeds.CONTENT, object.getString(Constants.KEY_CONTENT));
        values.put(DataProviderContract.SearchFeeds.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.SearchFeeds.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.SearchFeeds.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        values.put(DataProviderContract.SearchFeeds.USER_CITY, object.getString(Constants.KEY_USER_CITY));
        values.put(DataProviderContract.SearchFeeds.USER_COUNTRY, object.getString(Constants.KEY_USER_COUNTRY));

        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_CREATED_DATE));
        values.put(DataProviderContract.SearchFeeds.CREATED_DATE, timeStamp);
        timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.SearchFeeds.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.SearchFeeds.USER_ID, object.getString(Constants.KEY_U_ID));
        values.put(DataProviderContract.SearchFeeds.USER_NAME, object.getString(Constants.KEY_U_NAME));
        values.put(DataProviderContract.SearchFeeds.USER_PHOTO, object.getString(Constants.KEY_U_PHOTO));
        int userLiked = Util.getJsonValueInt(object, Constants.KEY_USER_LIKE);
        values.put(DataProviderContract.SearchFeeds.USER_LIKED, userLiked);

        context.getContentResolver().insert(DataProviderContract.SearchFeeds.CONTENT_URI, values);
    }

    public void updateSearchFeed(FeedBean feedBean) {
        try {
            ContentValues values = new ContentValues();

            String selection = DataProviderContract.SearchFeeds._ID + " = ?";
            String[] selectionArgs = new String[]{"" + feedBean.getPId()};

            values.put(DataProviderContract.SearchFeeds.CONTENT, feedBean.getPContent());
            if (!TextUtils.isEmpty(feedBean.getImage()))
                values.put(DataProviderContract.SearchFeeds.IMAGE, feedBean.getImage());
            values.put(DataProviderContract.SearchFeeds.MODIFIED_DATE, feedBean.getModifiedDate());
            values.put(DataProviderContract.SearchFeeds.STATUS, feedBean.isPending());
            context.getContentResolver().update(DataProviderContract.SearchFeeds.CONTENT_URI, values, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<FeedBean> getSearchFeedsSince(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.SearchFeeds.CONTENT_URI, DataProviderContract.SearchFeeds.LIST_PROJECTION,
                DataProviderContract.SearchFeeds.MODIFIED_DATE + "> ?", new String[]{"" + reference}, DataProviderContract.SearchFeeds.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.SearchFeeds._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.SearchFeeds.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.SearchFeeds.IMAGE);
            int indexLat = cursor.getColumnIndex(DataProviderContract.SearchFeeds.LATITUDE);
            int indexLongi = cursor.getColumnIndex(DataProviderContract.SearchFeeds.LONGITUDE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.SearchFeeds.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.SearchFeeds.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.SearchFeeds.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.SearchFeeds.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_NAME);
            int indexCity = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_COUNTRY);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_PHOTO);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_LIKED);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.SearchFeeds.STATUS);

            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }

    public synchronized List<FeedBean> getSearchFeedsUntil(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.SearchFeeds.CONTENT_URI, DataProviderContract.SearchFeeds.LIST_PROJECTION,
                DataProviderContract.SearchFeeds.MODIFIED_DATE + "< ?", new String[]{"" + reference}, DataProviderContract.SearchFeeds.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.SearchFeeds._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.SearchFeeds.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.SearchFeeds.IMAGE);
            int indexLat = cursor.getColumnIndex(DataProviderContract.SearchFeeds.LATITUDE);
            int indexLongi = cursor.getColumnIndex(DataProviderContract.SearchFeeds.LONGITUDE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.SearchFeeds.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.SearchFeeds.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.SearchFeeds.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.SearchFeeds.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_NAME);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_PHOTO);
            int indexCity = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_COUNTRY);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.SearchFeeds.USER_LIKED);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.SearchFeeds.STATUS);

            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }

    public synchronized void saveComments(int postId, JSONArray jsonArray) {
        int len = jsonArray.length();
        ContentValues values = new ContentValues();
        JSONObject object;
        for (int i = 0; i < len; i++) {
            try {
                object = jsonArray.getJSONObject(i);
                String commentId = object.getString(Constants.KEY_ID);
                Cursor cursor = context.getContentResolver().query(DataProviderContract.Comments.CONTENT_URI, new String[]{DataProviderContract.Comments.COMMENT},
                        DataProviderContract.Comments._ID + "=?", new String[]{commentId}, null);
                boolean isExists = cursor.getCount() > 0;
                cursor.close();
                if (isExists) {
                    updateComment(postId, object, values);
                } else {
                    insertComment(postId, object, values);
                }

                values.clear();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateComment(int postId, JSONObject object, ContentValues values) throws JSONException {
        String commentId = object.getString(Constants.KEY_ID);
        values.put(DataProviderContract.Comments.COMMENT, object.getString(Constants.KEY_MESSAGE));
        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_CREATED_DATE));
        values.put(DataProviderContract.Comments.CREATED_DATE, timeStamp);
        values.put(DataProviderContract.Comments.POST_ID, postId);
        values.put(DataProviderContract.Comments.USER_ID, object.getString(Constants.KEY_USER_ID));
        values.put(DataProviderContract.Comments.USER_NAME, object.getString(Constants.KEY_USER_NAME));
        values.put(DataProviderContract.Comments.USER_PHOTO, object.getString(Constants.KEY_USER_PHOTO));

        context.getContentResolver().update(DataProviderContract.Comments.CONTENT_URI, values,
                DataProviderContract.Comments._ID + "=?", new String[]{commentId});
    }

    private void insertComment(int postId, JSONObject object, ContentValues values) throws JSONException {
        values.put(DataProviderContract.Comments._ID, object.getString(Constants.KEY_ID));
        values.put(DataProviderContract.Comments.COMMENT, object.getString(Constants.KEY_MESSAGE));
        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_CREATED_DATE));
        values.put(DataProviderContract.Comments.CREATED_DATE, timeStamp);
        values.put(DataProviderContract.Comments.POST_ID, postId);
        values.put(DataProviderContract.Comments.USER_ID, object.getString(Constants.KEY_USER_ID));
        values.put(DataProviderContract.Comments.USER_NAME, object.getString(Constants.KEY_USER_NAME));
        values.put(DataProviderContract.Comments.USER_PHOTO, object.getString(Constants.KEY_USER_PHOTO));

        context.getContentResolver().insert(DataProviderContract.Comments.CONTENT_URI, values);
    }

    public void addComment(Comment comment) {
        ContentValues values = new ContentValues();
        values.put(DataProviderContract.Comments._ID, comment.getId());
        values.put(DataProviderContract.Comments.COMMENT, comment.getMessage());
        values.put(DataProviderContract.Comments.CREATED_DATE, comment.getCreatedDate());
        values.put(DataProviderContract.Comments.POST_ID, comment.getPostId());
        values.put(DataProviderContract.Comments.USER_ID, comment.getUserId());
        values.put(DataProviderContract.Comments.USER_NAME, comment.getUserName());
        values.put(DataProviderContract.Comments.USER_PHOTO, comment.getUserPhoto());
        values.put(DataProviderContract.Comments.STATUS, comment.isPending()); // 0-posted, 1-posting
        values.put(DataProviderContract.Comments.CLIENT_ID, comment.getClientId());
        context.getContentResolver().insert(DataProviderContract.Comments.CONTENT_URI, values);
    }

    public void updateComment(int oldCommentId, Comment comment) {
        ContentValues values = new ContentValues();

        String selection = DataProviderContract.Comments._ID + " = ?";
        String[] selectionArgs = new String[]{"" + oldCommentId};

        values.put(DataProviderContract.Comments._ID, comment.getId());
        values.put(DataProviderContract.Comments.CREATED_DATE, comment.getCreatedDate());
        values.put(DataProviderContract.Comments.STATUS, comment.isPending());
        context.getContentResolver().update(DataProviderContract.Comments.CONTENT_URI, values, selection, selectionArgs);
    }

    public void deleteComment(int commentId) {
        context.getContentResolver().delete(DataProviderContract.Comments.CONTENT_URI,
                DataProviderContract.Comments._ID + " = ?",
                new String[]{"" + commentId});
    }

    public synchronized List<Comment> getCommentsSince(int postId, long reference) {
        List<Comment> lstComments = new ArrayList<Comment>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.Comments.CONTENT_URI, DataProviderContract.Comments.LIST_PROJECTION,
                DataProviderContract.Comments.CREATED_DATE + "> ? and " + DataProviderContract.Comments.POST_ID + " = ? ",
                new String[]{"" + reference, "" + postId},
                DataProviderContract.Comments.CREATED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexCID = cursor.getColumnIndex(DataProviderContract.Comments._ID);
            int indexComment = cursor.getColumnIndex(DataProviderContract.Comments.COMMENT);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.Comments.CREATED_DATE);
            int indexUserId = cursor.getColumnIndex(DataProviderContract.Comments.USER_ID);
            int indexUserName = cursor.getColumnIndex(DataProviderContract.Comments.USER_NAME);
            int indexUserPhoto = cursor.getColumnIndex(DataProviderContract.Comments.USER_PHOTO);

            int indexClientId = cursor.getColumnIndex(DataProviderContract.Comments.CLIENT_ID);

            while (cursor.moveToNext()) {
                Comment mComment = new Comment();

                mComment.setId(cursor.getInt(indexCID));
                mComment.setMessage(cursor.getString(indexComment));
                mComment.setCreatedDate(cursor.getLong(indexCreatedTime));
                mComment.setUserId(cursor.getInt(indexUserId));
                mComment.setUserName(cursor.getString(indexUserName));
                mComment.setUserPhoto(cursor.getString(indexUserPhoto));
                mComment.setClientId(cursor.getString(indexClientId));

                lstComments.add(mComment);
            }
        }

        cursor.close();

        return lstComments;
    }

    public synchronized List<Comment> getCommentsUntil(int postId, long reference) {
        List<Comment> lstComments = new ArrayList<Comment>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.Comments.CONTENT_URI, DataProviderContract.Comments.LIST_PROJECTION,
                DataProviderContract.Comments.CREATED_DATE + "< ? and " + DataProviderContract.Comments.POST_ID + " = ? ",
                new String[]{"" + reference, "" + postId},
                DataProviderContract.Comments.CREATED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexCID = cursor.getColumnIndex(DataProviderContract.Comments._ID);
            int indexComment = cursor.getColumnIndex(DataProviderContract.Comments.COMMENT);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.Comments.CREATED_DATE);
            int indexUserId = cursor.getColumnIndex(DataProviderContract.Comments.USER_ID);
            int indexUserName = cursor.getColumnIndex(DataProviderContract.Comments.USER_NAME);
            int indexUserPhoto = cursor.getColumnIndex(DataProviderContract.Comments.USER_PHOTO);

            int indexClientId = cursor.getColumnIndex(DataProviderContract.Comments.CLIENT_ID);

            while (cursor.moveToNext()) {
                Comment mComment = new Comment();

                mComment.setId(cursor.getInt(indexCID));
                mComment.setMessage(cursor.getString(indexComment));
                mComment.setCreatedDate(cursor.getLong(indexCreatedTime));
                mComment.setUserId(cursor.getInt(indexUserId));
                mComment.setUserName(cursor.getString(indexUserName));
                mComment.setUserPhoto(cursor.getString(indexUserPhoto));
                mComment.setClientId(cursor.getString(indexClientId));

                lstComments.add(mComment);
            }
        }

        cursor.close();

        return lstComments;
    }

    public synchronized void saveHashTagFeeds(JSONArray jsonArray) {
        int len = jsonArray.length();
        ContentValues values = new ContentValues();
        JSONObject object;
        for (int i = 0; i < len; i++) {
            try {
                object = jsonArray.getJSONObject(i);
                String postId = object.getString(Constants.KEY_P_ID);
                Cursor cursor = context.getContentResolver().query(DataProviderContract.HashTagFeeds.CONTENT_URI, new String[]{DataProviderContract.HashTagFeeds.CONTENT},
                        DataProviderContract.HashTagFeeds._ID + "=?", new String[]{postId}, null);
                boolean isExists = cursor.getCount() > 0;
                cursor.close();
                if (isExists) {
                    updateHashTagFeeds(object, values);
                } else {
                    insertHashTagFeeds(object, values);
                }

                values.clear();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateHashTagFeeds(JSONObject object, ContentValues values) throws JSONException {
        String postId = object.getString(Constants.KEY_P_ID);
        values.put(DataProviderContract.HashTagFeeds.CONTENT, object.getString(Constants.KEY_CONTENT));
        if (!TextUtils.isEmpty(object.getString(Constants.KEY_POST_IMAGE)))
            values.put(DataProviderContract.HashTagFeeds.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.HashTagFeeds.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.HashTagFeeds.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.HashTagFeeds.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.HashTagFeeds.USER_LIKED, object.getInt(Constants.KEY_USER_LIKE));
        values.put(DataProviderContract.HashTagFeeds.USER_COUNTRY, object.getString(Constants.KEY_USER_COUNTRY));

        context.getContentResolver().update(DataProviderContract.HashTagFeeds.CONTENT_URI, values,
                DataProviderContract.HashTagFeeds._ID + "=?", new String[]{postId});
    }

    private void insertHashTagFeeds(JSONObject object, ContentValues values) throws JSONException {
        values.put(DataProviderContract.HashTagFeeds._ID, object.getString(Constants.KEY_P_ID));
        values.put(DataProviderContract.HashTagFeeds.CONTENT, object.getString(Constants.KEY_CONTENT));
        values.put(DataProviderContract.HashTagFeeds.IMAGE, object.getString(Constants.KEY_POST_IMAGE));
        values.put(DataProviderContract.HashTagFeeds.VOTES, object.getString(Constants.KEY_VOTES));
        values.put(DataProviderContract.HashTagFeeds.COMMENTS, object.getString(Constants.KEY_COMMENTS));
        values.put(DataProviderContract.HashTagFeeds.USER_CITY, object.getString(Constants.KEY_USER_CITY));
        values.put(DataProviderContract.HashTagFeeds.USER_COUNTRY, object.getString(Constants.KEY_USER_COUNTRY));

        long timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_CREATED_DATE));
        values.put(DataProviderContract.HashTagFeeds.CREATED_DATE, timeStamp);
        timeStamp = DateTimeUtil.convertUTCToGMT(object.getString(Constants.KEY_MODIFIED_DATE));
        values.put(DataProviderContract.HashTagFeeds.MODIFIED_DATE, timeStamp);
        values.put(DataProviderContract.HashTagFeeds.USER_ID, object.getString(Constants.KEY_U_ID));
        values.put(DataProviderContract.HashTagFeeds.USER_NAME, object.getString(Constants.KEY_U_NAME));
        values.put(DataProviderContract.HashTagFeeds.USER_PHOTO, object.getString(Constants.KEY_U_PHOTO));
        int userLiked = Utils.getJsonValueInt(object, Constants.KEY_USER_LIKE);
        values.put(DataProviderContract.HashTagFeeds.USER_LIKED, userLiked);

        context.getContentResolver().insert(DataProviderContract.HashTagFeeds.CONTENT_URI, values);
    }

    public void updateHashTagFeed(FeedBean feedBean) {
        try {
            ContentValues values = new ContentValues();

            String selection = DataProviderContract.HashTagFeeds._ID + " = ?";
            String[] selectionArgs = new String[]{"" + feedBean.getPId()};

            values.put(DataProviderContract.HashTagFeeds.CONTENT, feedBean.getPContent());
            if (!TextUtils.isEmpty(feedBean.getImage()))
                values.put(DataProviderContract.HashTagFeeds.IMAGE, feedBean.getImage());
            values.put(DataProviderContract.HashTagFeeds.MODIFIED_DATE, feedBean.getModifiedDate());
            values.put(DataProviderContract.HashTagFeeds.STATUS, feedBean.isPending());
            context.getContentResolver().update(DataProviderContract.HashTagFeeds.CONTENT_URI, values, selection, selectionArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<FeedBean> getHashTagFeedsSince(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.HashTagFeeds.CONTENT_URI, DataProviderContract.HashTagFeeds.LIST_PROJECTION,
                DataProviderContract.HashTagFeeds.MODIFIED_DATE + "> ?", new String[]{"" + reference}, DataProviderContract.HashTagFeeds.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.HashTagFeeds._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.IMAGE);
            int indexLat = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.LATITUDE);
            int indexLongi = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.LONGITUDE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_NAME);
            int indexCity = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_COUNTRY);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_PHOTO);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_LIKED);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.STATUS);

            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }

    public synchronized List<FeedBean> getHashTagFeedsUntil(long reference) {
        List<FeedBean> feeds = new ArrayList<FeedBean>();

        Cursor cursor = context.getContentResolver().query(DataProviderContract.HashTagFeeds.CONTENT_URI, DataProviderContract.HashTagFeeds.LIST_PROJECTION,
                DataProviderContract.HashTagFeeds.MODIFIED_DATE + "< ?", new String[]{"" + reference}, DataProviderContract.HashTagFeeds.MODIFIED_DATE + " DESC");
        if (cursor.getCount() > 0) {
            int indexPID = cursor.getColumnIndex(DataProviderContract.HashTagFeeds._ID);
            int indexContent = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.CONTENT);
            int indexImage = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.IMAGE);
            int indexLat = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.LATITUDE);
            int indexLongi = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.LONGITUDE);
            int indexVotes = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.VOTES);
            int indexComments = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.COMMENTS);
            int indexCreatedTime = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.CREATED_DATE);
            int indexModifiedTime = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.MODIFIED_DATE);

            int indexUID = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_ID);
            int indexUName = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_NAME);
            int indexPhotoUrl = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_PHOTO);
            int indexCity = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_CITY);
            int indexCountry = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_COUNTRY);
            int indexUserLiked = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.USER_LIKED);
            int indexStatus = cursor.getColumnIndex(DataProviderContract.HashTagFeeds.STATUS);

            while (cursor.moveToNext()) {
                FeedBean feedBean = new FeedBean();

                feedBean.setPId(cursor.getString(indexPID));
                feedBean.setPContent(cursor.getString(indexContent));
                feedBean.setImage(cursor.getString(indexImage));
                feedBean.setVotes(cursor.getInt(indexVotes));
                feedBean.setComments(cursor.getInt(indexComments));
                feedBean.setCreatedDate(cursor.getLong(indexCreatedTime));
                feedBean.setModifiedDate(cursor.getLong(indexModifiedTime));
                feedBean.setUId(cursor.getString(indexUID));
                feedBean.setUName(cursor.getString(indexUName));
                feedBean.setUPhoto(cursor.getString(indexPhotoUrl));
                feedBean.setCity(cursor.getString(indexCity));
                feedBean.setCountry(cursor.getString(indexCountry));
                feedBean.setPending(cursor.getInt(indexStatus));
                feedBean.setUserLike(cursor.getInt(indexUserLiked));

                feeds.add(feedBean);
            }
        }

        cursor.close();

        return feeds;
    }

    /**
     * Clear Notification table
     */
    public void clearNotificationData() {
        context.getContentResolver().delete(DataProviderContract.Notification.CONTENT_URI, null, null);
    }

    /**
     * Clear Feed table
     */
    public void clearFeedData() {
        context.getContentResolver().delete(DataProviderContract.Feed.CONTENT_URI, null, null);
    }

    /**
     * Clear PublicFeed table
     */
    public void clearPublicFeedData() {
        context.getContentResolver().delete(DataProviderContract.PublicFeed.CONTENT_URI, null, null);
    }

    /**
     * Clear SearchFeeds table
     */
    public void clearSearchFeedsData() {
        context.getContentResolver().delete(DataProviderContract.SearchFeeds.CONTENT_URI, null, null);
    }

    /**
     * Clear Comments table
     */
    public void clearCommentsData() {
        context.getContentResolver().delete(DataProviderContract.Comments.CONTENT_URI, null, null);
    }

    /**
     * Clear UnPending Comments table
     */
    public void clearUnPendingComments() {
        context.getContentResolver().delete(DataProviderContract.Comments.CONTENT_URI,
                DataProviderContract.Comments.STATUS + " != ? ", new String[]{"1"});
    }

    /**
     * Clear HashTagFeeds table
     */
    public void clearHashTagFeedsData() {
        context.getContentResolver().delete(DataProviderContract.HashTagFeeds.CONTENT_URI, null, null);
    }

    public boolean hasPendingComments(String postId) {
        Cursor cursor = context.getContentResolver().query(DataProviderContract.Comments.CONTENT_URI, new String[]{"count(*)"},
                DataProviderContract.Comments.STATUS + " = ? AND " + DataProviderContract.Comments.POST_ID + " = ?", new String[]{"1", postId}, null);
        cursor.moveToNext();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0 ? true : false;
    }

    public void updateUserPost(String userId, ContentValues values) {
        try {
            context.getContentResolver().update(DataProviderContract.Feed.CONTENT_URI, values, DataProviderContract.Feed.USER_ID + "=?", new String[]{userId});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        clearNotificationData();
        clearFeedData();
        clearPublicFeedData();
        clearCommentsData();
        clearSearchFeedsData();
        clearHashTagFeedsData();
    }
}
