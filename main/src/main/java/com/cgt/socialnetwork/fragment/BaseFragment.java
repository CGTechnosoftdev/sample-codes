package com.cgt.socialnetwork.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.general.AbstractFragment;
import com.cgt.socialnetwork.ui.ActivityMain;
import com.cgt.socialnetwork.utils.LifecycleListener;
import com.cgt.socialnetwork.utils.LifecycleProvider;
import com.cgt.socialnetwork.common.MyVolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by CGTechnosoft
 */
public abstract class BaseFragment extends AbstractFragment implements LifecycleProvider {

    private final CopyOnWriteArrayList<LifecycleListener> mLifecycleListeners
            = new CopyOnWriteArrayList<>();

    private String mSessionId;
    private ProgressDialog progressDialog = null;

    JobManager jobManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        jobManager = AppModuleManager.getInstance(getActivity()).getJobManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSessionId = UUID.randomUUID().toString();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Helper method to show prompt on screen
     * @param msg
     */
    protected void showPrompt(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * The method will launch Google Map application on device and will show the pointer for user address.
     * @param address
     */
    protected void openGoogleMap(String address) {
        Uri uri = Uri.parse("geo:0,0?z=10&q=" + address);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        if (!getActivity().getPackageManager().queryIntentActivities(i, 0).isEmpty()) {
            getActivity().startActivity(i);
        } else {
            showPrompt(getActivity().getString(R.string.msg_google_map_not_avail));
        }
    }

    /**
     * The method will launch Google Map application on device and will show the pointer for user latitude and longitude.
     * @param lat
     * @param longi
     */
    protected void openGoogleMap(double lat, double longi) {
        Uri uri = Uri.parse("geo:" + lat + "," + longi + "?z=10&q=" + lat + "," + longi);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        if (!getActivity().getPackageManager().queryIntentActivities(i, 0).isEmpty()) {
            getActivity().startActivity(i);
        } else {
            showPrompt(getActivity().getString(R.string.msg_google_map_not_avail));
        }
    }

    protected void openGoogleMap(double lat, double longi, String address) {
        Uri uri = Uri.parse("geo:" + lat + "," + longi + "?z=10&q=" + address);
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setPackage("com.google.android.apps.maps");
        if (!getActivity().getPackageManager().queryIntentActivities(i, 0).isEmpty()) {
            getActivity().startActivity(i);
        } else {
            showPrompt(getActivity().getString(R.string.msg_google_map_not_avail));
        }
    }

    protected void logout() {
        StringRequest request = new StringRequest(Request.Method.POST, Constants.LOGOUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                hideLoader();

                try {
                    JSONObject response = new JSONObject(s);
                    int code = response.getInt(com.cgt.socialnetwork.common.Constants.KEY_CODE);
                    switch (code) {
                        case Constants.SUCCESS:
                        case Constants.TOKEN_EXPIRED:
                        case Constants.BLANK_TOKEN:
                            Preference.getInstance(getActivity()).clearDataAfterLogout();

                            DBHelper dbHelper = DBHelper.getInstance(getActivity());
                            dbHelper.reset();

                            AppModuleManager manager = AppModuleManager.getInstance(getActivity());
                            manager.stopRunningJobs();
                            manager.reset();

                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }

                            Intent mIntent = new Intent(getActivity(), ActivityMain.class);
                            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            mIntent.putExtra("EXIT", true);
                            startActivity(mIntent);

                            getActivity().finish();
                            break;
                        default:
                            if (response.has(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE) && !response.isNull(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE)) {
                                if (isVisible())
                                    showPrompt(response.getString(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE));
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

    /**
     * Helper method to hide the keyboard
     */
    protected void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        for (LifecycleListener callback : mLifecycleListeners) {
            callback.onProviderStopped();
        }

        jobManager.cancelJobsInBackground(null, TagConstraint.ALL, mSessionId);
        mLifecycleListeners.clear();
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }
}
