package com.cgt.socialnetwork.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.fragment.FragmentPublicFeed;
import com.cgt.socialnetwork.fragment.FragmentUserProfile;
import com.cgt.socialnetwork.model.User;

/**
 * Created by CGTechnosoft
 */
public class ActivityUserProfile extends ActivityBase {

    private User mUser;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppModuleManager manager = AppModuleManager.getInstance(this);
        mUser = manager.getUser();
        userId = getIntent().getIntExtra(Constants.KEY_USER_ID, -1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.profile));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ActivityUserProfile.this.finish();
            }
        });

        // clear public feed database and preference;
        DBHelper.getInstance(this).clearPublicFeedData();
        Preference.getInstance(this).clearPublicFeedPreferences();

        FragmentManager frManager = getFragmentManager();
        FragmentUserProfile fragment = new FragmentUserProfile();
        frManager.beginTransaction().add(R.id.containerUserProfile, fragment).commit();


        FragmentPublicFeed fragment1 = new FragmentPublicFeed();
        frManager.beginTransaction().add(R.id.container, fragment1).commit();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_user_profile;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_feeds, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefresh:
                FragmentManager frManager = getFragmentManager();
                FragmentUserProfile fragment = new FragmentUserProfile();
                frManager.beginTransaction().replace(R.id.containerUserProfile, fragment).commit();

                FragmentPublicFeed fragment1 = new FragmentPublicFeed();
                frManager.beginTransaction().replace(R.id.container, fragment1).commit();
                break;
            case R.id.actionEdit:
                Intent in = new Intent(this, ActivityEditProfile.class);
                startActivityForResult(in, Constants.REQUEST_CODE_PROFILE);
                break;
        }
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (userId == mUser.getUserId()) {
            menu.findItem(R.id.actionEdit).setVisible(true);
            menu.findItem(R.id.actionRefresh).setVisible(true);
        } else {
            menu.findItem(R.id.actionEdit).setVisible(false);
            menu.findItem(R.id.actionRefresh).setVisible(true);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_PROFILE:
                FragmentManager frManager = getFragmentManager();
                FragmentUserProfile fragment = new FragmentUserProfile();
                frManager.beginTransaction().replace(R.id.containerUserProfile, fragment).commit();
                break;
        }
    }

}