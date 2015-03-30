package com.app.afridge.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * View pager that can be disabled
 * <p/>
 * Created by drakuwa on 2/17/14.
 */
public class NonSwipableViewPager extends JazzyViewPager {

  private boolean isEnabled;

  public NonSwipableViewPager(Context context, AttributeSet attrs) {

    super(context, attrs);
    this.isEnabled = true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    return this.isEnabled && super.onTouchEvent(event);

  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {

    return this.isEnabled && super.onInterceptTouchEvent(event);

  }

  public void setPagingEnabled(boolean enabled) {

    this.isEnabled = enabled;
  }
}
