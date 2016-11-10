// Generated code from Butter Knife. Do not modify!
package com.cgt.socialnetwork.fragment;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class FragmentComments$$ViewBinder<T extends com.cgt.socialnetwork.fragment.FragmentComments> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558589, "field 'recycle_post_list'");
    target.recycle_post_list = finder.castView(view, 2131558589, "field 'recycle_post_list'");
    view = finder.findRequiredView(source, 2131558590, "field 'tv_empty'");
    target.tv_empty = finder.castView(view, 2131558590, "field 'tv_empty'");
    view = finder.findRequiredView(source, 2131558645, "field 'et_message'");
    target.et_message = finder.castView(view, 2131558645, "field 'et_message'");
    view = finder.findRequiredView(source, 2131558646, "field 'iv_send' and method 'onSendClick'");
    target.iv_send = finder.castView(view, 2131558646, "field 'iv_send'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onSendClick();
        }
      });
  }

  @Override public void unbind(T target) {
    target.recycle_post_list = null;
    target.tv_empty = null;
    target.et_message = null;
    target.iv_send = null;
  }
}
