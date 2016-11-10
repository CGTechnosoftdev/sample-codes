package com.cgt.socialnetwork.general;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.utils.DebugLog;
import com.cgt.socialnetwork.utils.MasterGson;
import com.cgt.socialnetwork.widget.AVLoadingIndicatorDialog;

import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public abstract class BaseActivity extends AppCompatActivity implements ComponentCallbacks2 {

    private boolean isExit;

    InputMethodManager imm;

    public MasterGson masterGson;

    private AVLoadingIndicatorDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        masterGson = AppModuleManager.getInstance(this).getMasterGson();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() <= 1) {
            if (isExit) {
                finish();
            } else {
                Toast.makeText(this, "Press back again to exit " + getResources().getString(R.string.app_name), Toast.LENGTH_LONG).show();
                if (!isExit)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            isExit = false;
                        }
                    }, 2000);
                isExit = true;
            }
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public void addFragment(AbstractFragment mFragment, int id, String tag, boolean addtoBackStack, boolean animation, Map<String, View> map) {
        FragmentTransaction mTransaction = getFragmentManager().beginTransaction();

        mTransaction.replace(id, mFragment);
        if (addtoBackStack)
            mTransaction.addToBackStack(tag);
        mTransaction.commit();
    }

    public void clearAllFragment(Fragment mFragment) {
        try {
            FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
            mTransaction.remove(mFragment);
            mTransaction.commit();
            getFragmentManager().popBackStack();
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_LOW) {
            DebugLog.e("MEMORY Getting LoW");
            System.gc();
        }
    }

    public void hideLoader() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showLoader() {
        progressDialog = new AVLoadingIndicatorDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }

    protected void showLoader(String msg) {
        progressDialog = new AVLoadingIndicatorDialog(this);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    protected void showLoader(String msg, boolean cancelable) {
        progressDialog = new AVLoadingIndicatorDialog(this);
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    public void shareAppV2(String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.invite_frnds));
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share with"));
    }

}
