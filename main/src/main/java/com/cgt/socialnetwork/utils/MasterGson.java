package com.cgt.socialnetwork.utils;

import com.google.gson.Gson;

import org.json.JSONObject;

import javax.inject.Singleton;

/**
 * Created by CGTechnosoft
 */
@Singleton
public class MasterGson {
    private final Gson mGson;

    public MasterGson(Gson gson) {
        this.mGson = gson;
    }

    /**
     * add json object  as param also add pojo.class as param so will clare object with that class
     * you must type cast object to your object
     *
     * @param mJsonObject
     * @param mClass
     * @return
     */
    public Object createPOJOfromJsonObject(JSONObject mJsonObject, Class mClass) {
        Object mObject = mGson.fromJson(mJsonObject.toString(), mClass);
        return mObject;
    }

    /**
     * add json object  as param also add pojo.class as param so will clare object with that class
     * you must type cast object to your object
     *
     * @param s      (String type variable)
     * @param mClass
     * @return
     */
    public Object createPOJOfromString(String s, Class mClass) throws Exception {
        DebugLog.e(s);
        Object mObject = mGson.fromJson(s, mClass);
        return mObject;
    }

    public String toJsonString(Object mObject) {
        String result = mGson.toJson(mObject);
        return result;
    }
}
