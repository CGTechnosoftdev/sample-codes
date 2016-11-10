package com.cgt.socialnetwork.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cgt.socialnetwork.R;

/**
 * Created by CGTechnosoft
 */
public class AVLoadingIndicatorDialog extends AlertDialog {

    private TextView mMessageView;

    public AVLoadingIndicatorDialog(Context context) {
        super(context);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.progress_avld, null);
        mMessageView = (TextView) view.findViewById(R.id.message);
        setView(view);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


    @Override
    public void setMessage(CharSequence message) {
        mMessageView.setText(message);
    }
}
