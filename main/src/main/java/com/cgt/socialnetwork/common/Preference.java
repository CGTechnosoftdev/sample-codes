package com.cgt.socialnetwork.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by CGTechnosoft
 */
public class Preference {

    private static Preference mInstance = null;
    private Context mContext = null;
    private SharedPreferences mPreferences = null;

    private Preference(Context context) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Preference getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Preference(context.getApplicationContext());
        }

        return mInstance;
    }

    public void put(String key, int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void put(String key, long value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public void put(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, Set<String> value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    public boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public int getInt(String key) {
        return mPreferences.getInt(key, 0);
    }

    public long getLong(String key) {
        return mPreferences.getLong(key, 0);
    }

    public long getLong(String key, long defVal) {
        return mPreferences.getLong(key, defVal);
    }

    public Set<String> getStringSet(String key) {
        return mPreferences.getStringSet(key, new HashSet<String>());
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public void clearDataAfterLogout() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public void clearFeedPreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(Constants.PREF_KEY_FEED_UNTIL_DATE);
        editor.remove(Constants.PREF_KEY_FEED_SINCE_DATE);
        editor.commit();
    }

    public void clearPublicFeedPreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(Constants.PREF_KEY_PUBLIC_FEED_SINCE_DATE);
        editor.remove(Constants.PREF_KEY_PUBLIC_FEED_UNTIL_DATE);
        editor.commit();
    }

    public void clearSearchFeedsPreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(Constants.PREF_KEY_SEARCH_FEED_UNTIL_DATE);
        editor.commit();
    }

    public void clearCommentsPreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(Constants.PREF_KEY_COMMENT_SINCE_DATE);
        editor.remove(Constants.PREF_KEY_COMMENT_UNTIL_DATE);
        editor.commit();
    }

    public void clearHashTagFeedsPreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(Constants.PREF_KEY_HASHTAG_FEED_UNTIL_DATE);
        editor.commit();
    }
}
