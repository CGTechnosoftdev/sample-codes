package com.cgt.socialnetwork.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.common.MyVolleyHelper;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by CGTechnosoft
 */
public class FragmentForgetPassword extends BaseFragment {

    private EditText et_email;
    private TextView btnSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forget_psw, container, false);

        et_email = (EditText) rootView.findViewById(R.id.txtEmail);
        btnSend = (TextView) rootView.findViewById(R.id.btnSend);

        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        et_email.setFilters(new InputFilter[]{filterNowhitespace});
    }

    @OnClick(R.id.btnSend)
    void onButtonbtnSendClick() {
        if (validate()) {
            showLoader();
            String email = et_email.getText().toString().trim();

            /*AppModuleManager manager = AppModuleManager.getInstance(MainApp.getInstance().getApplicationContext());
            RequestBuilder requestBuilder = manager.getRequestBuilder();
            MasterGson masterGson1 = manager.getMasterGson();

            Map<String, String> params = requestBuilder.forgotPassword(email);

            new MakeRequestStringResponse("http://54.169.107.156:8080/mobileapi/forgotpassword", params, true) {

                @Override
                public void onPostExecute(String s) {
                    hideLoader();
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        switch (jsonObject.getInt(Constants.KEY_CODE)) {
                            case Constants.SUCCESS:
                                AlertUtils.alertBox(getActivity(), "", getResources().getString(R.string.psw_reset_link_sent), "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().onBackPressed();
                                    }
                                }, false);
                                break;
                            case Constants.INVALID_REQUEST:
                                showPrompt(getResources().getString(R.string.sorry));
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
            };*/

            StringRequest request = new StringRequest(Request.Method.POST, Constants.FORGOT_PASSWORD, new Response.Listener<String>() {

                @Override
                public void onResponse(String s) {
                    hideLoader();
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        switch (jsonObject.getInt(Constants.KEY_CODE)) {
                            case Constants.SUCCESS:
                                AlertDialogFactory.alertBox(getActivity(), "", getResources().getString(R.string.psw_reset_link_sent), "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().onBackPressed();
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
                    RequestBuilder requestBuilder = AppModuleManager.getInstance(getActivity()).getRequestBuilder();
                    Map<String, String> params = requestBuilder.forgotPassword(email);
                    return params;
                }
            };

            MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
        }
    }

    private boolean validate() {
        String email = et_email.getText().toString().trim();
        if (StringUtils.isEmpty(email)) {
            AlertDialogFactory.alertBox(getActivity(), "", "Please enter email address", "OK", null, false);
            return false;
        }

        if (!emailValidator(email)) {
            AlertDialogFactory.alertBox(getActivity(), "", "Please enter valid email address", "OK", null, false);
            return false;
        }

        return true;
    }

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private boolean emailValidator(String email) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    InputFilter filterNowhitespace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
