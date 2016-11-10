// Generated code from Butter Knife. Do not modify!
package com.cgt.socialnetwork.fragment;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class FragmentAddPost$$ViewBinder<T extends com.cgt.socialnetwork.fragment.FragmentAddPost> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558632, "field 'iv_user'");
    target.iv_user = finder.castView(view, 2131558632, "field 'iv_user'");
    view = finder.findRequiredView(source, 2131558633, "field 'tv_user_name'");
    target.tv_user_name = finder.castView(view, 2131558633, "field 'tv_user_name'");
    view = finder.findRequiredView(source, 2131558635, "field 'et_post_content'");
    target.et_post_content = finder.castView(view, 2131558635, "field 'et_post_content'");
    view = finder.findRequiredView(source, 2131558636, "field 'tv_remain'");
    target.tv_remain = finder.castView(view, 2131558636, "field 'tv_remain'");
    view = finder.findRequiredView(source, 2131558637, "field 'rl_photo'");
    target.rl_photo = finder.castView(view, 2131558637, "field 'rl_photo'");
    view = finder.findRequiredView(source, 2131558638, "field 'iv_photo_post' and method 'onPostPhotoClick'");
    target.iv_photo_post = finder.castView(view, 2131558638, "field 'iv_photo_post'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onPostPhotoClick();
        }
      });
    view = finder.findRequiredView(source, 2131558639, "field 'iv_remove_photo' and method 'onPhotoRemoveClick'");
    target.iv_remove_photo = finder.castView(view, 2131558639, "field 'iv_remove_photo'");
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onPhotoRemoveClick();
        }
      });
  }

  @Override public void unbind(T target) {
    target.iv_user = null;
    target.tv_user_name = null;
    target.et_post_content = null;
    target.tv_remain = null;
    target.rl_photo = null;
    target.iv_photo_post = null;
    target.iv_remove_photo = null;
  }
}
