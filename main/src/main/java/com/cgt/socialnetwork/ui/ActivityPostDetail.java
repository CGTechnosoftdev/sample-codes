package com.cgt.socialnetwork.ui;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.DateTimeUtil;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.fragment.FragmentPostDetail;
import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.common.MyVolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class ActivityPostDetail extends ActivityBase {

    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.post_detail));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyBoard();
                ActivityPostDetail.this.finish();
            }
        });

        postId = getIntent().getIntExtra(Constants.KEY_POST_ID, -1);

        getPostDetail();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_common;
    }

    private void getPostDetail() {
        showLoader();

        String url = Constants.GET_POST_DETAIL + Constants.KEY_POST_ID + "=" + postId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                hideLoader();
                try {
                    int code = response.getInt(Constants.KEY_CODE);
                    switch (code) {
                        case Constants.SUCCESS:
                            JSONObject jsonData = response.getJSONObject(Constants.KEY_DATA);

                            String content = jsonData.getString(Constants.KEY_CONTENT);
                            String type = jsonData.getString(Constants.KEY_TYPE);
                            String postImage = jsonData.getString(Constants.KEY_POST_IMAGE);
                            int likeCount = jsonData.getInt(Constants.KEY_VOTES);
                            int commentCount = jsonData.getInt(Constants.KEY_COMMENTS);
                            int userId = jsonData.getInt(Constants.KEY_U_ID);
                            String userName = jsonData.getString(Constants.KEY_U_NAME);
                            String userPhoto = jsonData.getString(Constants.KEY_U_PHOTO);
                            String userCity = jsonData.getString(Constants.KEY_USER_CITY);
                            int userLiked = jsonData.getInt(Constants.KEY_USER_LIKE);
                            Long timeStamp = DateTimeUtil.convertUTCToGMT(jsonData.getString(Constants.KEY_CREATED_DATE));

                            FeedBean feedBean = new FeedBean();
                            feedBean.setPId("" + postId);
                            feedBean.setPContent(content);
                            feedBean.setType(type);
                            feedBean.setImage(postImage);
                            feedBean.setVotes(likeCount);
                            feedBean.setComments(commentCount);
                            feedBean.setUId("" + userId);
                            feedBean.setUName(userName);
                            feedBean.setUPhoto(userPhoto);
                            feedBean.setCity(userCity);
                            feedBean.setUserLike(userLiked);
                            feedBean.setModifiedDate(timeStamp);

                            //updateUI(feedBean);

                            if (!isFinishing()) {
                                FragmentManager frManager = getFragmentManager();
                                FragmentPostDetail fragment = new FragmentPostDetail();
                                Bundle bundle = new Bundle();
                                bundle.putInt(Constants.KEY_POST_ID, Integer.parseInt(feedBean.getPId()));
                                bundle.putInt(Constants.KEY_USER_ID, Integer.parseInt(feedBean.getUId()));
                                bundle.putSerializable(com.cgt.socialnetwork.common.Constants.KEY_DATA, feedBean);
                                fragment.setArguments(bundle);
                                frManager.beginTransaction().add(R.id.container, fragment).commit();
                            }
                            break;
                        case Constants.TOKEN_EXPIRED:
                        case Constants.BLANK_TOKEN:
                            break;
                        case Constants.DATA_NOT_FOUND:
                            if (!isFinishing()) {
                                AlertDialogFactory.alertBox(ActivityPostDetail.this, "", "May be the post has been deleted.", "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        ActivityPostDetail.this.finish();
                                    }
                                }, false);
                            }
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoader();
                showToast(Util.getErrorMsg(error, ActivityPostDetail.this));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(ActivityPostDetail.this).getUser().getToken());
                return params;
            }
        };

        MyVolleyHelper.getIntance(ActivityPostDetail.this).addRequestToQueue(request);
    }

    @Override
    public void showLoader() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

}