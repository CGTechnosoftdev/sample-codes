// Generated code from Butter Knife. Do not modify!
package com.cgt.socialnetwork.fragment;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class FragmentForgetPassword$$ViewBinder<T extends com.cgt.socialnetwork.fragment.FragmentForgetPassword> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558668, "method 'onButtonbtnSendClick'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onButtonbtnSendClick();
        }
      });
  }

  @Override public void unbind(T target) {
  }
}
