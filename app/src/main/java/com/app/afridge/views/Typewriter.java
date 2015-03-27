package com.app.afridge.views;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import com.app.afridge.utils.Common;


/**
 * Typewriter appearance effect
 * <p/>
 * Created by drakuwa on 4/3/14.
 */
public class Typewriter extends AdvancedTextView {

  private int mColor;

  private static final float DELAY_TIME = 3f;
  private static final float FADE_IN_TIME = 1f;
  private static final float FADE_IN_TIME_2X = FADE_IN_TIME * 2;
  private CharSequence text;

  private float[] offsets;

  private long startTimeMs = 0;
  private long currentTimeMs = 0;

  private ViewTreeObserver.OnPreDrawListener onPreDrawListener;

  public Typewriter(Context context) {

    super(context);
  }

  public Typewriter(Context context, AttributeSet attrs) {

    super(context, attrs);
  }

  private Runnable updateRunnable;

  public void animateText() {

    startTimeMs = 0;

    if (updateRunnable != null) {
      removeCallbacks(updateRunnable);
    }

    updateRunnable = new Runnable() {

      @Override
      public void run() {

        if (doTick()) {
          // Update at 60fps if not every character is at full alpha
          postDelayed(updateRunnable, 1000 / 60);
        }
      }
    };

    post(updateRunnable);

    doTick();
  }

  @SuppressWarnings("unused")
  public void setCharacterColor(int color) {

    mColor = color;
  }

  private Boolean doTick() {

    if (startTimeMs == 0) {
      startTimeMs = SystemClock.uptimeMillis();
    }

    currentTimeMs = SystemClock.uptimeMillis();
    long deltaTimeMs = Math.max(currentTimeMs - startTimeMs, 0);

    int r = (mColor >> 16) & 0xFF;
    int g = (mColor >> 8) & 0xFF;
    int b = (mColor) & 0xFF; // int b = (mColor >> 0) & 0xFF;

    SpannableString mSpanText = new SpannableString(this.text);
    Boolean anyLeft = false;

    // on each tick, for each character, let delta = max(time from start time, 0),
    //
    for (int i = 0; i < offsets.length; i++) {
      float tt = ((deltaTimeMs / 1000f) / offsets[i]);
      tt = Math.min(tt, 1f);
      tt = 1f - (1f - tt) * (1f - tt);

      //      Log.i("typewriter", "" + tt);

      int targetAlpha = (int)
              Common.parametric(Common.clamp(tt, 0f, 1f), 0f, 255f);

      if (targetAlpha < 255) {
        anyLeft = true;
      }

      mSpanText.setSpan(new ForegroundColorSpan(Color.argb(
                      targetAlpha,
                      r,
                      g,
                      b)),
              i, i + 1, 0
      );
    }

    setText(mSpanText, BufferType.SPANNABLE);

    return anyLeft;
  }

  // show the text without animating
  public void showText() {

    SpannableString mSpanText = new SpannableString(text);
    mSpanText.setSpan(new ForegroundColorSpan(mColor), 0, mSpanText.length(), 0);
    setText(mSpanText, BufferType.SPANNABLE);
  }

  public void initSpanText(CharSequence text, int color) {

    this.text = text == null ? "" : text;
    mColor = color;

    startTimeMs = 0;

    // generate an array of floats the size of the length of text
    // each float is random value between 0 and .6f
    assert text != null;
    offsets = new float[text.length()];
    for (int i = 0; i < offsets.length; i++) {
      offsets[i] = ((float) Math.random()) * DELAY_TIME;
    }
  }
}