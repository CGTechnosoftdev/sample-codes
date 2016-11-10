package com.cgt.socialnetwork.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.model.User;
import com.cgt.socialnetwork.utils.MasterGson;
import com.cgt.socialnetwork.common.MyVolleyHelper;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class FragmentUserProfile extends BaseFragment {
    private TextView tvUserStatus;

    private int userId;
    private String strUserName, strLocation = "", strPhoto;

    private User mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppModuleManager manager = AppModuleManager.getInstance(getActivity());
        mUser = manager.getUser();
        mPicasso = manager.getPicasso();

        tvUserStatus = (TextView) getView().findViewById(R.id.tv_user_status);

        userId = getActivity().getIntent().getIntExtra(Constants.KEY_USER_ID, -1);
        strUserName = getActivity().getIntent().getStringExtra(Constants.KEY_USER_NAME);
        strLocation = getActivity().getIntent().getStringExtra(Constants.KEY_LOCATION);
        strPhoto = getActivity().getIntent().getStringExtra(Constants.KEY_PHOTO);

        getProfile();
    }

    private void getProfile() {
        showLoader();

        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
        RequestBuilder requestBuilder = manager.getRequestBuilder();

        Map<String, String> params = requestBuilder.getProfile("" + userId);
        String url = MyVolleyHelper.getIntance(getActivity()).addParamsToUrl(params, Constants.GET_PROFILE);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                hideLoader();
                try {
                    int code = response.getInt(com.cgt.socialnetwork.common.Constants.KEY_CODE);
                    switch (code) {
                        case com.cgt.socialnetwork.common.Constants.SUCCESS:
                            JSONObject jsonData = response.getJSONObject(com.cgt.socialnetwork.common.Constants.KEY_DATA);
                            strUserName = jsonData.getString(com.cgt.socialnetwork.common.Constants.KEY_FIRST_NAME)
                                    + " " + jsonData.getString(com.cgt.socialnetwork.common.Constants.KEY_LAST_NAME);

                            String status = Util.getJsonValue(jsonData, com.cgt.socialnetwork.common.Constants.KEY_USER_STATUS);
                            strLocation = Util.getJsonValue(jsonData, com.cgt.socialnetwork.common.Constants.KEY_COUNTRY);
                            strPhoto = Util.getJsonValue(jsonData, com.cgt.socialnetwork.common.Constants.KEY_USER_PHOTO);

                            if (userId == mUser.getUserId()) {
                                MasterGson masterGson1 = manager.getMasterGson();
                                User user = (User) masterGson1.createPOJOfromJsonObject(jsonData, User.class);
                                user.setUserType(com.cgt.socialnetwork.common.Constants.KEY_LOGIN_AS_APP);
                                manager.setUser(user);
                                Preference.getInstance(getActivity()).put(com.cgt.socialnetwork.common.Constants.KEY_USER_DATA, masterGson1.toJsonString(user));
                            }

                            tvUserStatus.setText(StringEscapeUtils.unescapeJava(status));
                            break;
                        case com.cgt.socialnetwork.common.Constants.TOKEN_EXPIRED:
                        case com.cgt.socialnetwork.common.Constants.BLANK_TOKEN:
                            logout();
                            break;
                        default:
                            if (response.has(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE) && !response.isNull(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE)) {
                                if (isVisible())
                                    showPrompt(response.getString(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE));
                            }
                            break;
                    }

                    updateToolBar();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isVisible()) {
                    hideLoader();
                    showPrompt(Util.getErrorMsg(error, getActivity()));
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(com.cgt.socialnetwork.common.Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(getActivity()).getUser().getToken());
                return params;
            }
        };

        MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
    }

    private void updateToolBar() {
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(strUserName);
            ((TextView) appBarLayout.findViewById(R.id.tv_UserCity)).setText(strLocation);
            ImageView imgUserImage = ((ImageView) appBarLayout.findViewById(R.id.img_userImage));
            if (!TextUtils.isEmpty(strPhoto)) {
                mPicasso.load(strPhoto).into(imgUserImage);
            }

            appBarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));

            imgUserImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(strPhoto)) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri imgUri = Uri.parse(strPhoto);
                        intent.setDataAndType(imgUri, "image/*");
                        getActivity().startActivity(intent);
                    }
                }
            });
        }
    }
}
