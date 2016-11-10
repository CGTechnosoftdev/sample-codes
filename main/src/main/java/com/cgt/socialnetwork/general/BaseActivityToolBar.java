package com.cgt.socialnetwork.general;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.cgt.socialnetwork.R;

/**
 * Created by CGTechnosoft
 */
public abstract class BaseActivityToolBar extends BaseActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
    }

    protected abstract int getLayoutResourceId();

    protected Toolbar setUpToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        return toolbar;
    }

    protected void navigationDrawerSetUp() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, setUpToolBar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        init();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected void showPrompt(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void openGoogleMap(String address) {
        Uri uri = Uri.parse("geo:0,0?z=10&q=" + address);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        if (!getPackageManager().queryIntentActivities(i, 0).isEmpty()) {
            startActivity(i);
        } else {
            showPrompt(getString(R.string.msg_google_map_not_avail));
        }
    }

    protected void openGoogleMap(double lat, double longi) {
        Uri uri = Uri.parse("geo:" + lat + "," + longi + "?z=10&q=" + lat + "," + longi);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        if (!getPackageManager().queryIntentActivities(i, 0).isEmpty()) {
            startActivity(i);
        } else {
            showPrompt(getString(R.string.msg_google_map_not_avail));
        }
    }

    protected void openGoogleMap(double lat, double longi, String address) {
        Uri uri = Uri.parse("geo:" + lat + "," + longi + "?z=10&q=" + address);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        if (!getPackageManager().queryIntentActivities(i, 0).isEmpty()) {
            startActivity(i);
        } else {
            showPrompt(getString(R.string.msg_google_map_not_avail));
        }
    }

    public abstract void init();

}
