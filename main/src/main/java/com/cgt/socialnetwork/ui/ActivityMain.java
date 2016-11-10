package com.cgt.socialnetwork.ui;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.bottomnavigation.AHBottomNavigation;
import com.cgt.socialnetwork.bottomnavigation.AHBottomNavigationItem;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.MyVolleyHelper;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.fragment.FragmentFeed;
import com.cgt.socialnetwork.fragment.FragmentNotification;
import com.cgt.socialnetwork.general.BaseActivityToolBar;
import com.cgt.socialnetwork.location.LocationProvider;
import com.cgt.socialnetwork.model.User;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.utils.CircleTransform;
import com.cgt.socialnetwork.utils.DebugLog;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by CGTechnosoft
 */
public class ActivityMain extends BaseActivityToolBar implements NavigationView.OnNavigationItemSelectedListener, FragmentNotification.INotificationUpdate {

    private NavigationView navigationView;
    private AHBottomNavigation bottomNavigation;
    private Picasso mPicasso;

    public static final int FEED = 0;
    public static final int NOTIFICATION = 1;

    private boolean isProfileSelected = false;

    private AppModuleManager manager;
    private User mUser;
    private int lastSelectedTab = -1;
    private int selectedNavigation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getBooleanExtra("EXIT", false)) {
            Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
            startActivity(i);
            finish();
        } else {
            manager = AppModuleManager.getInstance(this);
            mPicasso = manager.getPicasso();
            mUser = manager.getUser();

            navigationDrawerSetUp();
            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.menu_home_drawer);

            showUserInfo();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (navigationView != null && isProfileSelected) {
            navigationView.getMenu().getItem(0).setChecked(false);
            isProfileSelected = false;
            if (selectedNavigation != 0) {
                navigationView.getMenu().getItem(selectedNavigation).setChecked(true);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        replaceFragment(item.getItemId());
        return true;
    }

    private void showUserInfo() {
        mUser = manager.getUser();
        View view = navigationView.getHeaderView(0);
        if (view.findViewById(R.id.lblUser) != null)
            ((TextView) view.findViewById(R.id.lblUser)).setText(mUser.getUserName());

        ((TextView) view.findViewById(R.id.lblStatus)).setText(mUser.getEmail());

        ImageView imageView = ((ImageView) view.findViewById(R.id.imgAvatar));
        if (!TextUtils.isEmpty(mUser.getUserPhoto())) {
            mPicasso.load(mUser.getUserPhoto()).error(R.drawable.avatar_).placeholder(R.drawable.avatar_).transform(new CircleTransform(this)).resize(100, 100).centerCrop().into(imageView);
        }
    }

    private void replaceFragment(int type) {
        lastSelectedTab = -1;
        getFragmentManager().popBackStackImmediate("Screen-0", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        MenuItem item = navigationView.getMenu().findItem(type);
        // Highlight the selected item, update the title, and close the drawer
        item.setChecked(true);

        switch (type) {
            case R.id.nav_profile:
                selectedNavigation = 0;
                isProfileSelected = true;
                Intent in = new Intent(ActivityMain.this, ActivityUserProfile.class);
                in.putExtra(Constants.KEY_USER_ID, mUser.getUserId());
                in.putExtra(Constants.KEY_USER_NAME, mUser.getUserName());
                in.putExtra(Constants.KEY_PHOTO, mUser.getUserPhoto());
                in.putExtra(Constants.KEY_LOCATION, "");
                startActivityForResult(in, Constants.REQUEST_CODE_PROFILE);
                break;
            case R.id.nav_feedback:
                selectedNavigation = 1;
                sendFeedback();
                item.setChecked(false);
                break;
            case R.id.nav_logout:
                selectedNavigation = 2;
                AlertDialogFactory.alertBox(this, "", getString(R.string.msgLogout), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }, false);

                item.setChecked(false);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Initialization of bottom tabs
     */
    private void bottomNavigationSetup() {
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.empty, R.drawable.tab_feed, R.color.color_tab_1);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.empty, R.drawable.tab_notification, R.color.color_tab_4);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor(this.getResources().getColor(R.color.colorPrimary));

        // Disable the translation inside the CoordinatorLayout
        bottomNavigation.setBehaviorTranslationEnabled(false);

        // Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#FFFFFF"));
        /*bottomNavigation.setInactiveColor(Color.parseColor("#70738f"));*/
        bottomNavigation.setInactiveColor(Color.parseColor("#50FFFFFF"));

        // Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);

        // Force the titles to be displayed (against Material Design guidelines!)
        bottomNavigation.setForceTitlesDisplay(false);

        // Use colored navigation with circle reveal effect
        bottomNavigation.setColored(false);

        // Set current item programmatically
        //bottomNavigation.setCurrentItem(FEED);

        // Customize notification (title, background, typeface)
        bottomNavigation.setNotificationBackgroundColor(this.getResources().getColor(R.color.orange_btn));

        // Set listener
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, boolean wasSelected) {
                if (position == lastSelectedTab) {
                    return;
                }

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
                if (fragment != null)
                    clearAllFragment(fragment);

                if (navigationView != null) {
                    navigationView.getMenu().getItem(selectedNavigation).setChecked(false);
                    selectedNavigation = 0;
                }

                switch (position) {
                    case FEED:
                        addFragment(new FragmentFeed(), R.id.container, "Screen-0", false, false, null);
                        setTitle(getResources().getString(R.string.tab_feeds));
                        break;
                    case NOTIFICATION:
                        addFragment(new FragmentNotification(), R.id.container, "Screen-2", false, false, null);
                        setTitle(getResources().getString(R.string.tab_notifications));
                        break;
                }

                lastSelectedTab = position;
            }
        });

        updateNotificationCount();
    }

    private void checkAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) { // returns true if the app has requested this permission previously and the user denied the request.
                AlertDialogFactory.alertBox(this, "", "Application needs location permission to filter nearby post. Please allow permission or you can enable it from application settings.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ActivityMain.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, com.cgt.socialnetwork.common.Constants.PERMISSION_REQUEST_CODE_LOCATION);
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }, false);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, com.cgt.socialnetwork.common.Constants.PERMISSION_REQUEST_CODE_LOCATION);
            }
        } else {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                buildAlertMessageNoGps(this);
            } else {
                showLoader("Please wait fetching location information...", false);
                selectedNavigation = 2;
                LocationProvider.getInstance(this).connectToPlayService(new ResultReceiver(new Handler()) {

                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        hideLoader();
                        LocationProvider.getInstance(ActivityMain.this).disconnectFromPlayService();
                        switch (resultCode) {
                            case com.cgt.socialnetwork.common.Constants.SUCCESS_RESULT:
                                Double lat = Double.parseDouble(Preference.getInstance(ActivityMain.this).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LAT));
                                Double longi = Double.parseDouble(Preference.getInstance(ActivityMain.this).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LONG));
                                openGoogleMap(lat, longi, "near by hospitals");
                                break;
                            case com.cgt.socialnetwork.common.Constants.FAILURE_RESULT:
                                showPrompt("Unable to fetch location information");
                                break;
                        }
                    }
                });
            }
        }
    }

    private void buildAlertMessageNoGps(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendFeedback() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setType("text/plain");
        i.setData(Uri.parse("mailto:")); // only email apps should handle this
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"cgtbugtrace@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback));
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, "Send email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ActivityMain.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void init() {
        bottomNavigationSetup();

        // Open initial fragment
        Intent intent = getIntent();
        int screen = intent.getIntExtra(com.cgt.socialnetwork.common.Constants.KEY_CALLING_COMPONENT, FEED);
        bottomNavigation.setCurrentItem(screen);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_PROFILE:
                showUserInfo();
                break;
            case 100:
                checkAndProceed();
                break;
        }
    }

    public void logout() {
        showLoader();

        StringRequest request = new StringRequest(Request.Method.POST, Constants.LOGOUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                hideLoader();

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    switch (jsonObject.getInt(com.cgt.socialnetwork.common.Constants.KEY_CODE)) {
                        case com.cgt.socialnetwork.common.Constants.SUCCESS:
                        case com.cgt.socialnetwork.common.Constants.TOKEN_EXPIRED:
                        case com.cgt.socialnetwork.common.Constants.BLANK_TOKEN:
                            Preference.getInstance(ActivityMain.this).clearDataAfterLogout();

                            DBHelper dbHelper = DBHelper.getInstance(ActivityMain.this);
                            dbHelper.reset();

                            AppModuleManager manager = AppModuleManager.getInstance(ActivityMain.this);
                            manager.stopRunningJobs();
                            manager.reset();

                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }

                            Intent mIntent = new Intent(ActivityMain.this, ActivityLogin.class);
                            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mIntent);

                            finish();
                            break;
                        default:
                            if (jsonObject.has(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE) && !jsonObject.isNull(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE)) {
                                showPrompt(jsonObject.getString(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE));
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
                showPrompt(Util.getErrorMsg(error, ActivityMain.this));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(com.cgt.socialnetwork.common.Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(ActivityMain.this).getUser().getToken());
                return params;
            }
        };

        MyVolleyHelper.getIntance(ActivityMain.this).addRequestToQueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case com.cgt.socialnetwork.common.Constants.PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0) {
                    int length = permissions.length;
                    int countPermission = 0;
                    for (int i = 0; i < length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            DebugLog.e("Permission is granted");
                            countPermission++;

                            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                buildAlertMessageNoGps(this);
                            } else {
                                selectedNavigation = 2;
                                LocationProvider.getInstance(this).connectToPlayService(new ResultReceiver(new Handler()) {

                                    @Override
                                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                                        LocationProvider.getInstance(ActivityMain.this).disconnectFromPlayService();
                                        switch (resultCode) {
                                            case com.cgt.socialnetwork.common.Constants.SUCCESS_RESULT:
                                                Double lat = Double.parseDouble(Preference.getInstance(ActivityMain.this).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LAT));
                                                Double longi = Double.parseDouble(Preference.getInstance(ActivityMain.this).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LONG));
                                                openGoogleMap(lat, longi, "near by hospitals");
                                                break;
                                            case com.cgt.socialnetwork.common.Constants.FAILURE_RESULT:
                                                showPrompt("Unable to fetch location information");
                                                break;
                                        }
                                    }
                                });
                            }
                        } else {
                            DebugLog.e("Permission is revoked");
                        }
                    }
                }

                break;
        }
    }

    @Override
    public void updateNotificationCount() {
        int count = DBHelper.getInstance(this).getUnreadNotificationCount();
        if (count != 0) {
            bottomNavigation.setNotification("" + count, 3);
        } else
            bottomNavigation.setNotification("", 3);
    }
}
