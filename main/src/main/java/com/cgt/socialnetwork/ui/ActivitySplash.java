package com.cgt.socialnetwork.ui;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.DateTimeUtil;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.general.BaseActivity;
import com.cgt.socialnetwork.model.User;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by CGTechnosoft
 */
public class ActivitySplash extends BaseActivity {

    @Bind(R.id.img_splash_logo)
    ImageView imgSplashLogo;

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_splash);
        ButterKnife.bind(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent in;

                AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
                User user = manager.getUser();
                if (user != null) {
                    in = new Intent(ActivitySplash.this, ActivityMain.class);
                } else {
                    in = new Intent(ActivitySplash.this, ActivityLogin.class);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(ActivitySplash.this, imgSplashLogo, "robot");
                    // start the new activity
                    startActivity(in, options.toBundle());
                    ActivitySplash.this.finishAfterTransition();
                } else {
                    startActivity(in);
                    ActivitySplash.this.finish();
                }
            }
        }, SPLASH_TIME_OUT);

        deleteOldData();
    }

    private void deleteOldData() {

        // Delete 3 days old data
        String untilDate = null;
        long lngUntilDate = 0l;
        long diff = 0l;
        untilDate = Preference.getInstance(this).getString(com.cgt.socialnetwork.common.Constants.PREF_KEY_FEED_UNTIL_DATE);
        if (!TextUtils.isEmpty(untilDate)) {
            lngUntilDate = DateTimeUtil.convertUTCToGMT(untilDate);
            diff = System.currentTimeMillis() - lngUntilDate;
            if (diff > (1000 * 60 * 60 * 24 * 3)) {
                DBHelper.getInstance(this).clearFeedData();
                Preference.getInstance(this).clearFeedPreferences();

                // Delete Comments
                DBHelper.getInstance(this).clearUnPendingComments();
            }
        }
    }
}
