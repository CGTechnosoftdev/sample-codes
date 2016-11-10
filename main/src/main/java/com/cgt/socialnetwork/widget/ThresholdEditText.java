package com.cgt.socialnetwork.widget;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by CGTechnosoft
 */
public class ThresholdEditText extends EditText {
	private int threshold = 1000;
	private TextChanged thresholdTextChanged;
	private Handler handler;
	private Runnable invoker;

	public ThresholdEditText(Context context) {
		super(context);
		init();
	}

	public ThresholdEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ThresholdEditText(Context context, AttributeSet attrs,
							 int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	
	public void setThresholdMillis(int millis) {
		threshold = millis;
	}

	public void setOnThresholdTextChanged(TextChanged listener) {
		this.thresholdTextChanged = listener;
	}

	private void init() {
		handler = new Handler();
		invoker = new Runnable() {
			@Override
			public void run() {
				invokeCallback();
			}

		};

		this.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				handler.removeCallbacks(invoker);

				if (s.length() == 0) {
					invoker.run();
				} else {
					handler.postDelayed(invoker, threshold);
				}
			}

		});
	}

	private void invokeCallback() {
		if (thresholdTextChanged != null) {
			thresholdTextChanged.onThersholdTextChanged(this.getText());
		}
	}

	public interface TextChanged {
		void onThersholdTextChanged(Editable text);
	}

}