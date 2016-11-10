package com.cgt.socialnetwork.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.fragment.FragmentComments;

/**
 * Created by CGTechnosoft
 */
public class ActivityComment extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Comments");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyBoard();
                ActivityComment.this.finish();
            }
        });

        FragmentManager frManager = getFragmentManager();
        FragmentComments fragment = new FragmentComments();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_POST_ID, getIntent().getIntExtra(Constants.KEY_POST_ID, -1));
        bundle.putInt(Constants.KEY_USER_ID, getIntent().getIntExtra(Constants.KEY_USER_ID, -1));
        bundle.putString(com.cgt.socialnetwork.common.Constants.KEY_SCREEN, com.cgt.socialnetwork.common.Constants.KEY_SCREEN_COMMENT);
        bundle.putSerializable(com.cgt.socialnetwork.common.Constants.KEY_DATA, getIntent().getSerializableExtra(com.cgt.socialnetwork.common.Constants.KEY_DATA));
        fragment.setArguments(bundle);
        frManager.beginTransaction().add(R.id.container, fragment).commit();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_common;
    }
}