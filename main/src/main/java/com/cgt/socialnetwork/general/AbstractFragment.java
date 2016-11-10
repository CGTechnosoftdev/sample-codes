package com.cgt.socialnetwork.general;

import android.app.Fragment;
import android.os.Bundle;

import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.utils.MasterGson;
import com.cgt.socialnetwork.widget.AVLoadingIndicatorDialog;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by CGTechnosoft
 */
public abstract class AbstractFragment extends Fragment {

    protected Picasso mPicasso;
    protected MasterGson masterGson;
    protected EventBus eventBus;

    private AVLoadingIndicatorDialog progressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppModuleManager appModuleManager = AppModuleManager.getInstance(getActivity());
        mPicasso = appModuleManager.getPicasso();
        masterGson = appModuleManager.getMasterGson();
        eventBus = appModuleManager.getEventBus();
    }

    protected void showLoader() {
        progressDialog = new AVLoadingIndicatorDialog(getActivity());
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }

    protected void hideLoader() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    protected void showLoader(String msg) {
        progressDialog = new AVLoadingIndicatorDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    protected void showLoader(boolean value) {
        progressDialog = new AVLoadingIndicatorDialog(getActivity());
        progressDialog.show();
        progressDialog.setCancelable(value);
    }

    protected void showLoader(String msg, boolean value) {
        progressDialog = new AVLoadingIndicatorDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.show();
        progressDialog.setCancelable(value);
    }
}
