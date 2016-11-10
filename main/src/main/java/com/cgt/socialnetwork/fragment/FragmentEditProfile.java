package com.cgt.socialnetwork.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.FacebookLoginHelper;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.RequestDispatcher;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.db.DataProviderContract;
import com.cgt.socialnetwork.model.FacebookUser;
import com.cgt.socialnetwork.model.User;
import com.cgt.socialnetwork.ui.ActivityBaseChangePassword;
import com.cgt.socialnetwork.utils.CircleTransform;
import com.cgt.socialnetwork.utils.DebugLog;
import com.cgt.socialnetwork.utils.MasterGson;
import com.cgt.socialnetwork.common.MyVolleyHelper;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by CGTechnosoft
 */
public class FragmentEditProfile extends BaseFragment implements FragmentAttachImage.onImageChoseListener, View.OnClickListener {

    private FragmentAttachImage mFragment;

    private ImageView iv_user_dp, iv_edit;
    private EditText et_user_name, et_full_name, et_email, et_status;
    private RadioGroup radio_group;
    private RadioButton rb_male, rb_female;
    private Spinner sp_county;
    private Button btn_update;
    private ProgressBar progressCountry;

    private LinearLayout ll_active_account;
    private EditText et_email_activate;
    private Button btn_send_activation;
    private TextView tv_fb_activation;

    private AppModuleManager manager;
    private User mUser;

    private FacebookLoginHelper facebookLoginHelper;

    String selectImagePath = "";
    private int gender = 0; //0-nothing, 1-male, 2-female

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        manager = AppModuleManager.getInstance(getActivity());
        mUser = manager.getUser();
        mPicasso = manager.getPicasso();

        initView();
        setData();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(getActivity(), ActivityBaseChangePassword.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        return true;
    }

    private void initView() {
        iv_user_dp = (ImageView) getView().findViewById(R.id.iv_user_dp);
        iv_edit = (ImageView) getView().findViewById(R.id.iv_edit);
        et_user_name = (EditText) getView().findViewById(R.id.et_user_name);
        et_full_name = (EditText) getView().findViewById(R.id.et_full_name);
        et_email = (EditText) getView().findViewById(R.id.txtEmail);
        radio_group = (RadioGroup) getView().findViewById(R.id.radio_group);
        rb_male = (RadioButton) getView().findViewById(R.id.male);
        rb_female = (RadioButton) getView().findViewById(R.id.female);
        et_status = (EditText) getView().findViewById(R.id.et_status);
        sp_county = (Spinner) getView().findViewById(R.id.sp_county);
        btn_update = (Button) getView().findViewById(R.id.btn_update);

        progressCountry = (ProgressBar) getView().findViewById(R.id.progressCountry);

        ll_active_account = (LinearLayout) getView().findViewById(R.id.ll_active_account);
        et_email_activate = (EditText) getView().findViewById(R.id.et_email_activate);
        btn_send_activation = (Button) getView().findViewById(R.id.btn_send_activation);
        tv_fb_activation = (TextView) getView().findViewById(R.id.tv_fb_activation);

        iv_user_dp.setOnClickListener(this);
        iv_edit.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_send_activation.setOnClickListener(this);
        tv_fb_activation.setOnClickListener(this);

        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.male:
                        gender = 1;
                        break;
                    case R.id.female:
                        gender = 2;
                        break;
                }
            }
        });
    }

    public void setData() {
        mFragment = new FragmentAttachImage(getActivity(), this, 320, 480);
        if (!TextUtils.isEmpty(mUser.getUserPhoto())) {
            mPicasso.load(mUser.getUserPhoto()).transform(new CircleTransform(getActivity())).placeholder(R.drawable.avatar_).error(R.drawable.avatar_).into(iv_user_dp);
        }

        if (!TextUtils.isEmpty(mUser.getFirstName())) {
            et_full_name.setText(mUser.getFirstName() + " " + mUser.getLastName());
        } else {
            et_full_name.setText(mUser.getUserName());
        }

        et_user_name.setText(mUser.getUserName());
        et_status.setText(StringEscapeUtils.unescapeJava(mUser.getUserStatus()));
        et_email.setText(mUser.getEmail());

        if (mUser.getGender() == 1) {
            radio_group.check(rb_male.getId());
        } else if (mUser.getGender() == 2) {
            radio_group.check(rb_female.getId());
        }

        if (!TextUtils.isEmpty(mUser.getEmail())) {
            ll_active_account.setVisibility(View.GONE);
        } else {
            ll_active_account.setVisibility(View.VISIBLE);
        }

        try {
            progressCountry.setVisibility(View.VISIBLE);
            Cursor cursor = getActivity().getContentResolver().query(DataProviderContract.Country.CONTENT_URI,
                    DataProviderContract.Country.PROJECTION,
                    null,
                    null,
                    DataProviderContract.Country.COUNTRY_NAME + " ASC");

            if (cursor.getCount() > 0) {
                bindCountrySpinner(cursor);
            } else {
                cursor.close();
                // fetch from server and store it in local database
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Constants.GET_COUNTRIES, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //hideLoader();
                        try {
                            int code = response.getInt(Constants.KEY_CODE);
                            switch (code) {
                                case Constants.SUCCESS:
                                    JSONArray data = response.getJSONArray(Constants.KEY_DATA);
                                    if (data.length() > 0) {
                                        DBHelper.getInstance(getActivity()).saveCountryData(data);
                                        Cursor cursor = getActivity().getContentResolver().query(DataProviderContract.Country.CONTENT_URI,
                                                DataProviderContract.Country.PROJECTION,
                                                null,
                                                null,
                                                DataProviderContract.Country.COUNTRY_NAME + " ASC");
                                        if (cursor.getCount() > 0) {
                                            bindCountrySpinner(cursor);
                                        }
                                    }
                                    break;
                                case Constants.TOKEN_EXPIRED:
                                case Constants.BLANK_TOKEN:
                                    logout();
                                    break;
                                case Constants.DATA_NOT_FOUND:
                                    sp_county.setAdapter(null);
                                    progressCountry.setVisibility(View.GONE);
                                    break;
                                default:
                                    if (response.has(Constants.KEY_MESSAGE) && !response.isNull(Constants.KEY_MESSAGE)) {
                                        if (isVisible())
                                            showPrompt(response.getString(Constants.KEY_MESSAGE));
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
                            //hideLoader();
                            showPrompt(Util.getErrorMsg(error, getActivity()));
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(getActivity()).getUser().getToken());
                        return params;
                    }
                };

                MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onUpdateClick() {
        if (validate()) {
            showLoader(false);

            String userName = et_full_name.getText().toString().trim();
            String email = et_email.getText().toString().trim();
            String status = et_status.getText().toString().trim();

            Cursor cursor = (Cursor) sp_county.getSelectedItem();
            String country = cursor.getString(cursor.getColumnIndex(DataProviderContract.Country.COUNTRY_NAME));

            mUser.setUserName(userName);
            mUser.setEmail(email);
            mUser.setUserStatus(status);
            mUser.setGender(gender);
            mUser.setCountry(country);

            new AsyncTask<Void, Void, Integer>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Integer doInBackground(Void... params) {
                    try {
                        AppModuleManager appModuleManager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
                        RequestDispatcher requestDispatcher = appModuleManager.getRequestDispatcher();
                        RequestBuilder requestBuilder = manager.getRequestBuilder();

                        Map<String, String> reqParams = requestBuilder.editProfile(mUser);
                        String response = requestDispatcher.createMultipartRequest(Constants.EDIT_PROFILE, reqParams, "image", selectImagePath);

                        JSONObject jsonObject = new JSONObject(response);
                        int code = jsonObject.getInt(Constants.KEY_CODE);
                        switch (code) {
                            case Constants.SUCCESS:
                                JSONObject jsonData = jsonObject.getJSONObject(Constants.KEY_DATA);
                                MasterGson masterGson1 = manager.getMasterGson();
                                User user = (User) masterGson1.createPOJOfromJsonObject(jsonData, User.class);
                                manager.setUser(user);
                                Preference.getInstance(getActivity()).put(Constants.KEY_USER_DATA, masterGson1.toJsonString(user));

                                // Update user feeds
                                ContentValues values = new ContentValues();
                                values.put(DataProviderContract.Feed.USER_NAME, user.getUserName());
                                values.put(DataProviderContract.Feed.USER_CITY, user.getCity());
                                values.put(DataProviderContract.Feed.USER_COUNTRY, user.getCountry());
                                values.put(DataProviderContract.Feed.USER_PHOTO, user.getUserPhoto());

                                DBHelper.getInstance(getActivity()).updateUserPost(user.getUserId() + "", values);
                                break;
                        }

                        return code;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return 2;
                }

                @Override
                protected void onPostExecute(Integer code) {
                    hideLoader();
                    switch (code) {
                        case Constants.SUCCESS:
                            showPrompt(getString(R.string.msg_profile_updated));
                            Picasso.with(getActivity()).invalidate(mUser.getUserPhoto());
                            getActivity().finish();
                            break;
                        case Constants.TOKEN_EXPIRED:
                        case Constants.BLANK_TOKEN:
                            showPrompt(getString(R.string.msg_token_expired));
                            break;
                        default:
                            showPrompt(getString(R.string.msg_unable_to_process_request));
                            break;
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    private boolean validate() {
        String userName = et_user_name.getText().toString().trim();
        String name = et_full_name.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String county = sp_county.getSelectedItem().toString();

        if (StringUtils.isEmpty(userName)) {
            showPrompt("Please enter user name");
            return false;
        } else if (StringUtils.isEmpty(name)) {
            showPrompt("Please enter full name");
            return false;
        } else if (StringUtils.isEmpty(email)) {
            showPrompt("Please enter email address");
            return false;
        } else if (!emailValidator(email)) {
            showPrompt("Please enter valid email address");
            return false;
        } else if (StringUtils.isEmpty(county)) {
            showPrompt("Please select country");
            return false;
        }

        // if all done good return true and move ahead
        return true;
    }

    private void bindCountrySpinner(Cursor cursor) {
        try {
            // Columns from DB to map into the view file
            String[] fromColumns = {DataProviderContract.Country.COUNTRY_NAME};
            // View IDs to map the columns (fetched above) into
            int[] toViews = {R.id.text1};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    getActivity(), // context
                    R.layout.spinner_item, // layout file
                    cursor, // DB cursor
                    fromColumns, // data to bind to the UI
                    toViews, // views that'll represent the data from `fromColumns`
                    0
            );

            adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
            sp_county.setAdapter(adapter);
            if (!TextUtils.isEmpty(mUser.getCountry())) {
                setCountrySpinnerByName(sp_county, mUser.getCountry());
            }

            progressCountry.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCountrySpinnerByName(Spinner spinner, String _name) {
        int spinnerCount = spinner.getCount();
        for (int i = 0; i < spinnerCount; i++) {
            Cursor value = (Cursor) spinner.getItemAtPosition(i);
            String name = value.getString(value.getColumnIndex(DataProviderContract.Country.COUNTRY_NAME));
            if (name.toLowerCase().equals(_name.toLowerCase())) {
                spinner.setSelection(i);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SimpleCursorAdapter countryAdapter = (SimpleCursorAdapter) sp_county.getAdapter();
        if (countryAdapter != null)
            countryAdapter.getCursor().close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (facebookLoginHelper != null) {
            facebookLoginHelper.onActivityResult(requestCode, resultCode, data);
        } else {
            mFragment.onActivityResult(requestCode, resultCode, data, getActivity());
        }
    }

    private boolean emailValidator(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


    private void OnActiveAccClick() {
        final String email = et_email_activate.getText().toString().trim() + "";
        if (StringUtils.isEmpty(email)) {
            showPrompt("Please enter email address");
            return;
        } else if (!emailValidator(email)) {
            showPrompt("Please enter valid email address");
            return;
        }

        showLoader();

        // Http call for account activation
        StringRequest request = new StringRequest(Request.Method.POST, Constants.URL + "accountActivation", new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                hideLoader();

                try {
                    JSONObject response = new JSONObject(s);
                    int code = response.getInt(Constants.KEY_CODE);
                    switch (code) {
                        case Constants.SUCCESS:
                            showPrompt("Link to set password has been sent to above mentioned email");
                            break;
                        case Constants.TOKEN_EXPIRED:
                        case Constants.BLANK_TOKEN:
                            showPrompt("Token expired, please re-login");
                            break;
                        default:
                            if (response.has(Constants.KEY_MESSAGE) && !response.isNull(Constants.KEY_MESSAGE)) {
                                showPrompt(response.getString(Constants.KEY_MESSAGE));
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
                hideLoader();
                showPrompt(Util.getErrorMsg(error, getActivity()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(getActivity()).getUser().getToken());
                return params;
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                AppModuleManager manager = AppModuleManager.getInstance(getActivity());
                return manager.getRequestBuilder().accountActivation(email, "" + manager.getUser().getUserId());
            }
        };

        MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
    }

    private void onFbClick() {
        facebookLoginHelper = new FacebookLoginHelper(this);
        facebookLoginHelper.fetchProfile(new FacebookLoginHelper.IProfileReceiver() {
            @Override
            public void onProfileFetched(JSONObject mObject) {
                facebookLoginHelper = null;
                FacebookUser mFacebookUser = (FacebookUser) masterGson.createPOJOfromJsonObject(mObject, FacebookUser.class);
                et_full_name.setText(mFacebookUser.getName() + "");
                et_user_name.setText(mFacebookUser.getName() + "");
                et_email.setText(mFacebookUser.getEmail());
                ll_active_account.setVisibility(View.GONE);
                et_email.setEnabled(false);
            }
        });
    }

    @Override
    public void onImageSelected(String imagePath) {
        selectImagePath = imagePath;
        File targetFile = new File(imagePath);
        Uri imageUri = Uri.fromFile(targetFile);
        mPicasso.load(imageUri)
                .transform(new CircleTransform(getActivity()))
                .placeholder(R.drawable.avatar_)
                .resize(100, 100).centerCrop()
                .error(R.drawable.avatar_)
                .into(iv_user_dp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_user_dp:
                if (TextUtils.isEmpty(selectImagePath)) {
                    if (!TextUtils.isEmpty(mUser.getUserPhoto())) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri imgUri = Uri.parse(mUser.getUserPhoto());
                        intent.setDataAndType(imgUri, "image/*");
                        getActivity().startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri imgUri = Uri.parse("file://" + selectImagePath);
                    intent.setDataAndType(imgUri, "image/*");
                    startActivity(intent);
                }
                break;
            case R.id.iv_edit:
                if (isPermissionGranted(Constants.PERMISSION_REQUEST_CODE_CAMERA, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                    showAttachOptions(iv_edit);
                }

                break;
            case R.id.btn_update:
                onUpdateClick();
                break;
            case R.id.btn_send_activation:
                OnActiveAccClick();
                break;
            case R.id.tv_fb_activation:
                onFbClick();
                break;
        }
    }

    private void showAttachOptions(View view) {
        PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.inflate(R.menu.menu_attachment);
        menu.setOnMenuItemClickListener(onAttachMenuItemClickListener);
        menu.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        menu.show();
    }

    PopupMenu.OnMenuItemClickListener onAttachMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.actionCamera:
                    mFragment.openCameraForImage(FragmentEditProfile.this);
                    break;

                case R.id.actionGallery:
                    mFragment.openGalleryForImage(FragmentEditProfile.this);
                    break;
            }
            return true;
        }
    };

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
                    if (countPermission == length)
                        showAttachOptions(iv_edit);
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
                    FragmentCompat.requestPermissions(FragmentEditProfile.this, permissions, permissionCode);
                    return false;
                }
            }
            return true;
        } else { //permission is automatically granted on sdk<23 upon installation
            DebugLog.e("Permission is granted");
            return true;
        }
    }

}
