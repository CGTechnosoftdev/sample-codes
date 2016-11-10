// Generated code from Butter Knife. Do not modify!
package com.cgt.socialnetwork.ui;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class ActivitySplash$$ViewBinder<T extends com.cgt.socialnetwork.ui.ActivitySplash> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558568, "field 'imgSplashLogo'");
    target.imgSplashLogo = finder.castView(view, 2131558568, "field 'imgSplashLogo'");
  }

  @Override public void unbind(T target) {
    target.imgSplashLogo = null;
  }
}
