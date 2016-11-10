package com.cgt.socialnetwork.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.interfaces.IFragmentCommunication;
import com.cgt.socialnetwork.ui.ActivityLogin;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.common.MyVolleyHelper;

import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;

/**
 * Created by CGTechnosoft
 */
public class FragmentSignUp extends BaseFragment implements View.OnClickListener {

    private EditText txtName, txtEmail, txtPass, txtConfirmPass;
    private Button btnSignUp;
    private RadioGroup radio_group;

    private IFragmentCommunication mCallback;

    private int gender = 0; //0-nothing, 1-male, 2-female

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_signup, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SetTint();
        InitUI();
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        super.onAttach(context);
        onAttachToContext(context);
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    protected void onAttachToContext(Context context) {
        mCallback = (IFragmentCommunication) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void SetTint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTranslucentStatus(boolean on) {
        Window win = getActivity().getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void InitUI() {
        txtName = (EditText) getView().findViewById(R.id.txtName);
        txtEmail = (EditText) getView().findViewById(R.id.txtEmail);
        txtPass = (EditText) getView().findViewById(R.id.txtPass);
        txtConfirmPass = (EditText) getView().findViewById(R.id.txtConfirmPass);
        radio_group = (RadioGroup) getView().findViewById(R.id.radio_group);

        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) getView().findViewById(checkedId);
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

        btnSignUp = (Button) getView().findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignUp:
                if (validate()) {
                    callWs();
                }
                break;
        }
    }

    private void callWs() {
        showLoader(getString(R.string.msg_signup));

        String name = txtName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String pass = txtPass.getText().toString().trim();

        String pushToken = Preference.getInstance(getActivity()).getString(Constants.KEY_PUSH_TOKEN);

        AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, Constants.CREATE_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {
                hideLoader();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    switch (jsonObject.getInt(Constants.KEY_CODE)) {
                        case Constants.SUCCESS:
                            AlertDialogFactory.alertBox(getActivity(), "", "User successfully registered, the verification link has been sent to your email.",
                                    "Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            relaunchLoginScreen();
                                        }
                                    }, false);
                            break;
                        default:
                            if (jsonObject.has(Constants.KEY_MESSAGE) && !jsonObject.isNull(Constants.KEY_MESSAGE)) {
                                showPrompt(jsonObject.getString(Constants.KEY_MESSAGE));
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
                showPrompt(Util.getErrorMsg(error, getActivity()));
            }
        }) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                AppModuleManager manager = AppModuleManager.getInstance(getActivity());
                return manager.getRequestBuilder().createUser(email, pass, name, gender, Preference.getInstance(getActivity()).getString(Constants.KEY_DEVICE_ID), pushToken);
            }
        };

        MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
    }

    private void relaunchLoginScreen() {
        Intent intent = new Intent(getActivity(), ActivityLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private boolean checkEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validate() {
        String name = txtName.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();
        String pass = txtPass.getText().toString();
        String confirmPass = txtConfirmPass.getText().toString();

        if (TextUtils.isEmpty(name)) {
            showPrompt("Please enter full name");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            showPrompt("Please enter email address");
            return false;
        }

        if (!checkEmail(email)) {
            showPrompt("Please enter valid email address");
            return false;
        }

        if (TextUtils.isEmpty(pass)) {
            showPrompt("Please enter password");
            return false;
        }

        if (pass.trim().length() < 6) {
            showPrompt("Password length should be at least 6 characters");
            return false;
        }

        if (pass.trim().length() > 16) {
            showPrompt("Password length should not be more then 16 characters");
            return false;
        }

        if (TextUtils.isEmpty(confirmPass)) {
            showPrompt("Please enter confirm password");
            return false;
        }

        if (!(confirmPass).equals(pass)) {
            showPrompt("Password and confirm password must be same");
            return false;
        }

        return true;
    }

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
}

