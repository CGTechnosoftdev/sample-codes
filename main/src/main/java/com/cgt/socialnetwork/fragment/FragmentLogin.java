package com.cgt.socialnetwork.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.FacebookLoginHelper;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.interfaces.IFragmentCommunication;
import com.cgt.socialnetwork.model.FacebookUser;
import com.cgt.socialnetwork.model.User;
import com.cgt.socialnetwork.ui.ActivityLogin;
import com.cgt.socialnetwork.ui.ActivityMain;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.utils.MasterGson;
import com.cgt.socialnetwork.common.MyVolleyHelper;

import org.json.JSONObject;

import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by CGTechnosoft
 */
public class FragmentLogin extends BaseFragment implements OnClickListener {

    private FacebookLoginHelper facebookLoginHelper;
    private String GuestNo = "", fbID = "";
    private int width, height;
    private String buttonclick = "";

    private boolean bottomReach = false;
    private ActivityLogin mActivity;

    private EditText et_email, et_psw;
    private Button btnLogin;
    private TextView tv_forgot_psw;
    private LinearLayout btnFbSignIn;

    private IFragmentCommunication mCallback;

    private FacebookUser mFacebookUser;

    public FragmentLogin() {

    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        super.onAttach(context);
        onAttachToContext(context);
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    protected void onAttachToContext(Context context) {
        mCallback = (IFragmentCommunication) context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        facebookLoginHelper = new FacebookLoginHelper(this);
        mActivity = (ActivityLogin) this.getActivity();
        InitUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        return view;
    }

    private void InitUI() {
        DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        GuestNo = nDigitRandomNo(6);

        et_email = (EditText) getView().findViewById(R.id.txtEmail);
        et_psw = (EditText) getView().findViewById(R.id.txtPass);

        tv_forgot_psw = (TextView) getView().findViewById(R.id.tv_forgot_psw);
        btnFbSignIn = (LinearLayout) getView().findViewById(R.id.btnFbSignIn);
        btnLogin = (Button) getView().findViewById(R.id.btnLogin);

        tv_forgot_psw.setOnClickListener(this);
        btnFbSignIn.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    private String nDigitRandomNo(int digits) {
        int max = (int) Math.pow(10, (digits)) - 1; //for digits =7, max will be 9999999
        int min = (int) Math.pow(10, digits - 1); //for digits = 7, min will be 1000000
        int range = max - min; //This is 8999999
        Random r = new Random();
        int x = r.nextInt(range);// This will generate random integers in range 0 - 8999999
        int nDigitRandomNo = x + min; //Our random rumber will be any random number x + min
        return String.valueOf(nDigitRandomNo);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if (v == tv_forgot_psw) {
            mCallback.openForgetPassword();
        } else if (v == btnFbSignIn) {
            facebookLoginHelper.fetchProfile(new FacebookLoginHelper.IProfileReceiver() {
                @Override
                public void onProfileFetched(JSONObject mObject) {
                    if (mObject.has("id")) {
                        mFacebookUser = (FacebookUser) masterGson.createPOJOfromJsonObject(mObject, FacebookUser.class);
                        if (TextUtils.isEmpty(mFacebookUser.getEmail())) {
                            AlertDialogFactory.alertBox(getActivity(), "", getResources().getString(R.string.facebook_error_msg), "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().onBackPressed();
                                }
                            }, false);
                        } else {
                            loginAsFacebook();
                        }
                    }
                }
            });
        } else if (v == btnLogin) {
            if (validate()) {
                login();
            }
        }
    }

    private boolean validate() {
        String email = et_email.getText().toString().trim();
        String pass = et_psw.getText().toString();

        if (TextUtils.isEmpty(email)) {
            showPrompt("Please enter email address");
            return false;
        }

        if (!verifyEmail(email)) {
            showPrompt("Please enter valid email address");
            return false;
        }

        if (TextUtils.isEmpty(pass)) {
            showPrompt("Please enter password");
            return false;
        }

        return true;
    }

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private boolean verifyEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookLoginHelper.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.CODE_FACEBOOK_LOGIN) {
                facebookLoginHelper.fetchProfile(new FacebookLoginHelper.IProfileReceiver() {
                    @Override
                    public void onProfileFetched(JSONObject mObject) {
                        if (mObject.has("id")) {
                            mFacebookUser = (FacebookUser) masterGson.createPOJOfromJsonObject(mObject, FacebookUser.class);
                            if (TextUtils.isEmpty(mFacebookUser.getEmail())) {
                                AlertDialogFactory.alertBox(getActivity(), "", getResources().getString(R.string.facebook_error_msg), "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().onBackPressed();
                                    }
                                }, false);
                            } else {
                                loginAsFacebook();
                            }
                        }
                    }
                });
            }
        }
    }

    private void login() {
        showLoader(getString(R.string.msg_login));
        String email = et_email.getText().toString().trim();
        String pass = et_psw.getText().toString();
        String pushToken = Preference.getInstance(getActivity()).getString(Constants.KEY_PUSH_TOKEN);

        StringRequest request = new StringRequest(Request.Method.POST, Constants.LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                hideLoader();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    switch (jsonObject.getInt(Constants.KEY_CODE)) {
                        case Constants.SUCCESS:
                            AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
                            MasterGson masterGson1 = manager.getMasterGson();
                            JSONObject jsonData = jsonObject.getJSONObject(Constants.KEY_DATA);
                            User user = (User) masterGson1.createPOJOfromJsonObject(jsonData, User.class);
                            user.setUserType(Constants.KEY_LOGIN_AS_APP);
                            manager.setUser(user);
                            Preference.getInstance(getActivity()).put(Constants.KEY_USER_DATA, masterGson1.toJsonString(user));

                            startActivity(new Intent(mActivity, ActivityMain.class));
                            mActivity.finish();
                            break;
                        case Constants.INVALID_REQUEST:
                            break;
                        default:
                            if (jsonObject.has(Constants.KEY_MESSAGE) && !jsonObject.isNull(Constants.KEY_MESSAGE)) {
                                showPrompt(jsonObject.getString(Constants.KEY_MESSAGE));
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoader();
                showPrompt(Util.getErrorMsg(error, getActivity()));
            }
        }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                RequestBuilder requestBuilder = AppModuleManager.getInstance(getActivity()).getRequestBuilder();
                Map<String, String> params = requestBuilder.login(email, pass, Preference.getInstance(getActivity()).getString(Constants.KEY_DEVICE_ID), pushToken);
                return params;
            }
        };

        MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
    }

    private void loginAsFacebook() {
        showLoader(getString(R.string.msg_login), true);
        String pushToken = Preference.getInstance(getActivity()).getString(Constants.KEY_PUSH_TOKEN);

        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
        RequestBuilder requestBuilder = manager.getRequestBuilder();
        MasterGson masterGson1 = manager.getMasterGson();

        StringRequest request = new StringRequest(Request.Method.POST, Constants.FACEBOOK_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                hideLoader();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    switch (jsonObject.getInt(Constants.KEY_CODE)) {
                        case Constants.SUCCESS:
                            JSONObject jsonData = jsonObject.getJSONObject(Constants.KEY_DATA);
                            User user = (User) masterGson1.createPOJOfromJsonObject(jsonData, User.class);
                            user.setUserType(Constants.KEY_LOGIN_AS_FB);
                            manager.setUser(user);
                            Preference.getInstance(getActivity()).put(Constants.KEY_USER_DATA, masterGson1.toJsonString(user));
                            break;
                        case Constants.INVALID_REQUEST:
                            break;
                        default:
                            if (jsonObject.has(Constants.KEY_MESSAGE) && !jsonObject.isNull(Constants.KEY_MESSAGE)) {
                                showPrompt(jsonObject.getString(Constants.KEY_MESSAGE));
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoader();
                showPrompt(Util.getErrorMsg(error, getActivity()));
            }
        }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = requestBuilder.loginAsFacebook(mFacebookUser.getId(), mFacebookUser.getEmail(), mFacebookUser.getName(),
                        mFacebookUser.getGender(), Preference.getInstance(getActivity()).getString(Constants.KEY_DEVICE_ID), pushToken);
                return params;
            }
        };

        MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
    }
}