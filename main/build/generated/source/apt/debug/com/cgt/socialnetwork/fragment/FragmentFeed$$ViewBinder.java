// Generated code from Butter Knife. Do not modify!
package com.cgt.socialnetwork.fragment;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class FragmentFeed$$ViewBinder<T extends com.cgt.socialnetwork.fragment.FragmentFeed> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558589, "field 'recycle_post_list'");
    target.recycle_post_list = finder.castView(view, 2131558589, "field 'recycle_post_list'");
    view = finder.findRequiredView(source, 2131558590, "field 'tv_empty'");
    target.tv_empty = finder.castView(view, 2131558590, "field 'tv_empty'");
  }

  @Override public void unbind(T target) {
    target.recycle_post_list = null;
    target.tv_empty = null;
  }
}
