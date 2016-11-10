package com.cgt.socialnetwork.common;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.cgt.socialnetwork.utils.DebugLog;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by CGTechnosoft.
 */
public class FacebookLoginHelper {

    private Fragment fragment;
    private CallbackManager callbackManager;
    private List<String> permissionList = Arrays.asList("public_profile", "email");
    private String userImage;
    private String title, content, link;

    public FacebookLoginHelper(Fragment fragment) {
        this.fragment = fragment;
        callbackManager = CallbackManager.Factory.create();
    }

    public void fetchProfile(final IProfileReceiver receiver) {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (permissionList.contains(loginResult.getRecentlyDeniedPermissions())) {
                    DebugLog.e("Permission Not Granted");
                } else {
                    GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {

                            if (object != null)
                                receiver.onProfileFetched(object);
                        }
                    });

                    Bundle params = new Bundle();
                    params.putString("fields", "id,email,name,gender,first_name,last_name,link");
                    graphRequest.setParameters(params);
                    graphRequest.executeAsync();
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(fragment.getActivity(), "Please provide permissions to access required information for login.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                if (AccessToken.getCurrentAccessToken() != null) {
                    LoginManager.getInstance().logOut();
                }
                error.printStackTrace();
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(fragment, permissionList);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void publishFeedDialog(String title, String userImage, String content, String link) {
        this.title = title;
        this.content = content;
        this.link = link;
        this.userImage = userImage;
        if (FacebookSdk.isInitialized()) {
            ShareDialog shareDialog = new ShareDialog(fragment);
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle(title)
                        .setImageUrl(Uri.parse(userImage))
                        .setContentDescription(content)
                        .setContentUrl(Uri.parse(link))
                        .build();
                shareDialog.show(linkContent);
            }
        }
    }

    public interface IProfileReceiver {
        void onProfileFetched(JSONObject mObject);
    }
}
