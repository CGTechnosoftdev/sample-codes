package com.cgt.socialnetwork.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.MyVolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class ActivityBaseChangePassword extends ActivityBase {

    private TextView txtOldPass, txtNewPass, txtConfirmPass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Change Password");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyBoard();
                ActivityBaseChangePassword.this.finish();
            }
        });

        txtOldPass = (TextView) findViewById(R.id.txtOldPassword);
        txtNewPass = (TextView) findViewById(R.id.txtNewPassword);
        txtConfirmPass = (TextView) findViewById(R.id.txtConfirmPassword);

        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ActivityBaseChangePassword.this.finish();
            }
        });

        findViewById(R.id.btnChange).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_change_password;
    }

    private void changePassword() {
        boolean changePass = true;
        String msg = "";

        if (TextUtils.isEmpty(txtOldPass.getText().toString())) {
            changePass = false;
            msg = "Please enter old password";
        } else if (TextUtils.isEmpty(txtNewPass.getText().toString())) {
            changePass = false;
            msg = "Please enter new password";
        } else if (TextUtils.isEmpty(txtConfirmPass.getText().toString())) {
            changePass = false;
            msg = "Please enter confirm password";
        } else if (!txtConfirmPass.getText().toString().equals(txtNewPass.getText().toString())) {
            changePass = false;
            msg = "New and Confirm password mismatch";
        } else if (txtConfirmPass.getText().toString().length() < 6) {
            changePass = false;
            msg = "Password length should be at least 6 characters";
        } else if (txtConfirmPass.getText().toString().length() > 16) {
            changePass = false;
            msg = "Password length should not be more then 16 characters";
        }

        if (changePass) {
            showLoader(true);
            StringRequest request = new StringRequest(Request.Method.POST, Constants.CHANGE_PASSWORD, new Response.Listener<String>() {

                @Override
                public void onResponse(String s) {
                    hideLoader();

                    try {
                        JSONObject response = new JSONObject(s);
                        int code = response.getInt(Constants.KEY_CODE);
                        switch (code) {
                            case Constants.SUCCESS:
                                showToast("Password changed successfully");
                                ActivityBaseChangePassword.this.finish();
                                break;
                            case Constants.TOKEN_EXPIRED:
                            case Constants.BLANK_TOKEN:
                                showToast("Token expired, please re-login");
                                break;
                            default:
                                if (response.has(Constants.KEY_MESSAGE) && !response.isNull(Constants.KEY_MESSAGE)) {
                                    showToast(response.getString(Constants.KEY_MESSAGE));
                                }
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
                    showToast(Util.getErrorMsg(error, ActivityBaseChangePassword.this));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(ActivityBaseChangePassword.this).getUser().getToken());
                    return params;
                }

                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    return AppModuleManager.getInstance(ActivityBaseChangePassword.this).getRequestBuilder().
                            changePassword(txtOldPass.getText().toString(), txtNewPass.getText().toString());
                }
            };

            MyVolleyHelper.getIntance(this).addRequestToQueue(request);
        } else {
            showToast(msg);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideKeyBoard();
    }
}
