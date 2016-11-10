package com.cgt.socialnetwork.ui;


import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.fragment.FragmentForgetPassword;
import com.cgt.socialnetwork.fragment.FragmentLogin;
import com.cgt.socialnetwork.fragment.FragmentSignUp;
import com.cgt.socialnetwork.gcm.RegistrationIntentService;
import com.cgt.socialnetwork.general.BaseActivity;
import com.cgt.socialnetwork.interfaces.IFragmentCommunication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by CGTechnosoft
 */
public class ActivityLogin extends BaseActivity implements IFragmentCommunication {

    public Dialog mdialog;

    private LinearLayout ll_login_sign_up;
    private TextView tv_login, tv_sign_up;
    private View view_login, view_sign_up;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_authentication);

        InitUI();

        String deviceName = android.os.Build.MODEL;
        String deviceMan = android.os.Build.MANUFACTURER;
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Preference.getInstance(ActivityLogin.this).put(com.cgt.socialnetwork.common.Constants.KEY_DEVICE_ID, deviceMan + "_" + deviceName + "_" + androidId);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            startService(new Intent(this, RegistrationIntentService.class));
        }
    }

    private void InitUI() {
        ll_login_sign_up = (LinearLayout) findViewById(R.id.ll_login_sign_up);
        tv_login = (TextView) findViewById(R.id.tv_login);
        tv_sign_up = (TextView) findViewById(R.id.tv_sign_up);
        view_login = (View) findViewById(R.id.view_login);
        view_sign_up = (View) findViewById(R.id.view_sign_up);

        mdialog = new Dialog(ActivityLogin.this);
        mdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mdialog.setContentView(R.layout.loading_layout);
        mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mdialog.setCancelable(false);
        mdialog.setCanceledOnTouchOutside(false);


        tv_login.setSelected(true);
        tv_sign_up.setSelected(false);
        view_login.setVisibility(View.VISIBLE);
        view_sign_up.setVisibility(View.GONE);

        replaceFragment(new FragmentLogin(), R.id.content_frame, Constants.LOGIN_FRAGMENT);


        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_login.setSelected(true);
                tv_sign_up.setSelected(false);
                view_login.setVisibility(View.VISIBLE);
                view_sign_up.setVisibility(View.GONE);

                replaceFragment(new FragmentLogin(), R.id.content_frame, Constants.LOGIN_FRAGMENT);
            }
        });

        tv_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_login.setSelected(false);
                tv_sign_up.setSelected(true);
                view_login.setVisibility(View.GONE);
                view_sign_up.setVisibility(View.VISIBLE);

                replaceFragment(new FragmentSignUp(), R.id.content_frame, Constants.SIGN_UP_FRAGMENT);
            }
        });
    }

    public void replaceFragment(Fragment mFragment, int id, String backStackTag) {
        FragmentManager manager = getFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStackTag, 0);

        if (manager.findFragmentByTag(backStackTag) == null) {
            if (!fragmentPopped) { //fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(id, mFragment, backStackTag);
                ft.addToBackStack(backStackTag);
                ft.commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ll_login_sign_up.setVisibility(View.VISIBLE);
        tv_login.setSelected(true);
        tv_sign_up.setSelected(false);
        view_login.setVisibility(View.VISIBLE);
        view_sign_up.setVisibility(View.GONE);
    }

    @Override
    public void openForgetPassword() {
        replaceFragment(new FragmentForgetPassword(), R.id.content_frame, Constants.FORGET_PSW_FRAGMENT);
        ll_login_sign_up.setVisibility(View.GONE);
    }

    @Override
    public void openLoginFragment() {
        replaceFragment(new FragmentLogin(), R.id.content_frame, Constants.LOGIN_FRAGMENT);
        ll_login_sign_up.setVisibility(View.VISIBLE);
        tv_login.setSelected(true);
        tv_sign_up.setSelected(false);
        view_login.setVisibility(View.VISIBLE);
        view_sign_up.setVisibility(View.GONE);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                finish();
            }

            return false;
        }

        return true;
    }
}
