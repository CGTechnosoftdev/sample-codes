package com.cgt.socialnetwork.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.controller.FeedController;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.location.LocationProvider;
import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.model.User;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.utils.CircleTransform;
import com.cgt.socialnetwork.utils.DebugLog;
import com.cgt.socialnetwork.common.MyVolleyHelper;
import com.cgt.socialnetwork.widget.ThresholdEditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by CGTechnosoft
 */
public class FragmentAddPost extends BaseFragment implements FragmentAttachImage.onImageChoseListener {

    @Bind(R.id.iv_user)
    ImageView iv_user;
    @Bind(R.id.tv_user_name)
    TextView tv_user_name;

    @Bind(R.id.et_post_content)
    ThresholdEditText et_post_content;
    @Bind(R.id.tv_remain)
    TextView tv_remain;

    @Bind(R.id.rl_photo)
    RelativeLayout rl_photo;
    @Bind(R.id.iv_photo_post)
    ImageView iv_photo_post;
    @Bind(R.id.iv_remove_photo)
    ImageView iv_remove_photo;

    private User mUser;

    private FragmentAttachImage mFragment;
    private String audioPath = "";
    private String selectImagePath = "";

    private FeedController feedController = null;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private String strHashTag = "";
    private int hashTagPos;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ItemAdapter mAdapter;
    private List<String> mItems = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_add_post, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        AppModuleManager manager = AppModuleManager.getInstance(getActivity());
        mUser = manager.getUser();
        mPicasso = manager.getPicasso();
        feedController = manager.getFeedController();

        mFragment = new FragmentAttachImage(getActivity(), this, 320, 480);

        if (checkPlayServices()) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) { // returns true if the app has requested this permission previously and the user denied the request.
                    AlertDialogFactory.alertBox(getActivity(), "", "Application needs location permission to filter nearby post. Please allow permission or you can enable it from application settings.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.PERMISSION_REQUEST_CODE_LOCATION);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }, false);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.PERMISSION_REQUEST_CODE_LOCATION);
                }
            } else {
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Please wait fetching location information...");
                progressDialog.setCancelable(false);
                LocationProvider.getInstance(getActivity()).connectToPlayService(new ResultReceiver(new Handler()) {

                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        progressDialog.dismiss();
                        LocationProvider.getInstance(getActivity()).disconnectFromPlayService();
                        switch (resultCode) {
                            case Constants.FAILURE_RESULT:
                                showPrompt("Unable to fetch location information");
                                break;
                        }
                    }
                });
            }
        }

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int height = (int) metrics.heightPixels * 58 / 100;

        View bottomSheet = getView().findViewById(R.id.bottom_sheet);
        bottomSheet.setMinimumHeight(height);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(0);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new ItemAdapter();
        recyclerView.setAdapter(mAdapter);

        if (!TextUtils.isEmpty(mUser.getUserPhoto())) {
            mPicasso.load(mUser.getUserPhoto()).error(R.drawable.avatar_).placeholder(R.drawable.avatar_)
                    .transform(new CircleTransform(getActivity()))
                    .into(iv_user);
        }

        tv_user_name.setText(mUser.getUserName());

        et_post_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int l = s.length();
                tv_remain.setText(l + "/" + (500 - l) + " characters");

                if (start == 0 && count == 0) {
                    MyVolleyHelper.getIntance(getActivity()).cancelRequest("reqHashtag");
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    //Log.d("Before : ", "" + before);
                    String text = "";
                    if (before == 1) {
                        text = s.toString().substring(0, start);

                        MyVolleyHelper.getIntance(getActivity()).cancelRequest("reqHashtag");
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    } else {
                        text = s.toString().substring(0, start + count);
                    }

                    if (text.lastIndexOf("#") > text.lastIndexOf(" ")) {
                        //Log.d("Before 2 : ", "" + before);
                        hashTagPos = text.lastIndexOf("#");
                        strHashTag = text.substring(hashTagPos, text.length());
                        getSearchHashTag();
                    } else {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_post, menu);

        try {
            Field[] fields = menu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(menu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean permission = false;
        switch (item.getItemId()) {
            case R.id.actionCamera:
                permission = isPermissionGranted(Constants.PERMISSION_REQUEST_CODE_CAMERA, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                if (permission) {
                    mFragment.openCameraForImage(FragmentAddPost.this);
                }
                break;
            case R.id.actionGallery:
                permission = isPermissionGranted(Constants.PERMISSION_REQUEST_CODE_STORAGE, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
                if (permission) {
                    mFragment.openGalleryForImage(FragmentAddPost.this);
                }
                break;
            case R.id.actionSend:
                if (!TextUtils.isEmpty(et_post_content.getText().toString().trim())) {
                    postData();
                } else {
                    showPrompt("Please add post message");
                }
                break;
        }

        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.iv_photo_post)
    void onPostPhotoClick() {
        if (!TextUtils.isEmpty(selectImagePath)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri imgUri = Uri.parse("file://" + selectImagePath);
            intent.setDataAndType(imgUri, "image/*");
            startActivity(intent);
        }
    }

    @OnClick(R.id.iv_remove_photo)
    void onPhotoRemoveClick() {
        File file = (File) rl_photo.getTag();
        file.delete();
        rl_photo.setVisibility(View.GONE);
    }

    /**
     *
     */
    private void postData() {
        hideKeyboard();
        String tags = "";
        double latitude = 0;
        double longitude = 0;

        FeedBean bean = new FeedBean();

        User user = AppModuleManager.getInstance(getActivity()).getUser();
        bean.setUId("" + user.getUserId());
        bean.setUName(user.getUserName());
        bean.setUPhoto(user.getUserPhoto());
        bean.setCountry(user.getCountry());

        long currentTime = System.currentTimeMillis();

        int postId = (int) (currentTime / 1000);
        bean.setPId("" + postId);
        bean.setModifiedDate(currentTime);
        bean.setCreatedDate(currentTime);
        bean.setPContent(et_post_content.getText().toString().trim());
        bean.setTag(tags);
        bean.setImage(selectImagePath);
        bean.setPending(1);
        bean.setClientId(UUID.randomUUID().toString());
        if (isLocationPermissionGranted(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})) {
            if (!TextUtils.isEmpty(Preference.getInstance(getActivity()).getString(Constants.KEY_CURRENT_LAT)))
                latitude = Double.parseDouble(Preference.getInstance(getActivity()).getString(Constants.KEY_CURRENT_LAT));
            if (!TextUtils.isEmpty(Preference.getInstance(getActivity()).getString(Constants.KEY_CURRENT_LONG)))
                longitude = Double.parseDouble(Preference.getInstance(getActivity()).getString(Constants.KEY_CURRENT_LONG));
            LocationProvider.getInstance(getActivity()).disconnectFromPlayService();
        }

        bean.setLatitute(latitude);
        bean.setLongitute(longitude);

        // Insert into post table with status posting -- (No need to send event to screen)
        DBHelper.getInstance(getActivity()).addFeed(bean);

        feedController.sendPostAsync(bean);
        getActivity().finish();
        showPrompt("Posting...");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragment.onActivityResult(requestCode, resultCode, data, getActivity());
    }

    @Override
    public void onImageSelected(String imagePath) {
        selectImagePath = imagePath;
        File targetFile = new File(imagePath);
        Uri imageUri = Uri.fromFile(targetFile);
        mPicasso.load(imageUri).placeholder(R.drawable.avatar_).error(R.drawable.avatar_).into(iv_photo_post);
        rl_photo.setTag(targetFile);
        rl_photo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0) {
                    int length = permissions.length;
                    int countPermission = 0;
                    for (int i = 0; i < length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            DebugLog.e("Permission is granted");
                            countPermission++;
                        } else {
                            DebugLog.e("Permission is revoked");
                        }
                    }
                    if (countPermission == length) {
                        mFragment.openCameraForImage(FragmentAddPost.this);
                    }
                }
                break;
            case Constants.PERMISSION_REQUEST_CODE_STORAGE:
                if (grantResults.length > 0) {
                    int length = permissions.length;
                    int countPermission = 0;
                    for (int i = 0; i < length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            DebugLog.e("Permission is granted");
                            countPermission++;
                        } else {
                            DebugLog.e("Permission is revoked");
                        }
                    }
                    if (countPermission == length) {
                        mFragment.openGalleryForImage(FragmentAddPost.this);
                    }
                }
                break;
            case Constants.PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0) {
                    int length = permissions.length;
                    int countPermission = 0;
                    for (int i = 0; i < length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            DebugLog.e("Permission is granted");
                            countPermission++;
                        } else {
                            DebugLog.e("Permission is revoked");
                        }
                    }
                }
                break;
        }
    }

    public boolean isPermissionGranted(int permissionCode, String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            int length = permissions.length;
            for (int i = 0; i < length; i++) {
                if (ContextCompat.checkSelfPermission(getActivity(), permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                    DebugLog.e("Permission is granted");
                } else {
                    DebugLog.e("Permission is revoked");
                    FragmentCompat.requestPermissions(FragmentAddPost.this, permissions, permissionCode);
                    return false;
                }
            }
            return true;
        } else { //permission is automatically granted on sdk<23 upon installation
            DebugLog.e("Permission is granted");
            return true;
        }
    }

    public boolean isLocationPermissionGranted(String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            int length = permissions.length;
            for (int i = 0; i < length; i++) {
                if (ContextCompat.checkSelfPermission(getActivity(), permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                    DebugLog.e("Permission is granted");
                } else {
                    DebugLog.e("Permission is revoked");
                    return false;
                }
            }
            return true;
        } else { //permission is automatically granted on sdk<23 upon installation
            DebugLog.e("Permission is granted");
            return true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    private void getSearchHashTag() {
        MyVolleyHelper.getIntance(getActivity()).cancelRequest("reqHashtag");

        AppModuleManager manager = AppModuleManager.getInstance(getActivity());
        RequestBuilder requestBuilder = manager.getRequestBuilder();

        Map<String, String> params = requestBuilder.getSearchHashTag(strHashTag);
        String url = MyVolleyHelper.getIntance(getActivity()).addParamsToUrl(params, Constants.SEARCH_HASH_TAG);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    int code = response.getInt(com.cgt.socialnetwork.common.Constants.KEY_CODE);
                    switch (code) {
                        case com.cgt.socialnetwork.common.Constants.SUCCESS:
                            mItems.clear();
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                            JSONArray jsonArray = response.getJSONArray(Constants.KEY_DATA);
                            int len = jsonArray.length();
                            for (int i = 0; i < len; i++) {
                                try {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    mItems.add(object.getString(Constants.KEY_HASH_TAG));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            break;
                        case com.cgt.socialnetwork.common.Constants.TOKEN_EXPIRED:
                        case com.cgt.socialnetwork.common.Constants.BLANK_TOKEN:
                            logout();
                            break;
                        case Constants.DATA_NOT_FOUND:
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request.setTag("reqHashtag"));
    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        public ItemAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(mItems.get(position));

            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    String str = et_post_content.getText().toString();
                    String newString = str.substring(0, hashTagPos) + holder.textView.getText().toString() + " " + str.substring(hashTagPos + strHashTag.length());

                    et_post_content.setText(newString);

                    // set cursor
                    if (et_post_content.getText().toString().length() >= 500) {
                        et_post_content.setSelection(500);
                    } else {
                        et_post_content.setSelection(hashTagPos + holder.textView.getText().toString().length() + 1);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.textView);
            }
        }
    }
}
