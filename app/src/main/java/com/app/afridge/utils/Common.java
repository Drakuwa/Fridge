package com.app.afridge.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Common utilities class
 * <p/>
 * Created by drakuwa on 11/10/14.
 */
public class Common {

  public static boolean versionAtLeast(int version) {

    return Build.VERSION.SDK_INT >= version;
  }

  public static List<String> getUserEmailAccounts(Context context) {
    // get all accounts listed in the phone
    List<String> accountSet = new ArrayList<>();
    // match with email address pattern and add to set to avoid duplicates
    Account[] accounts = AccountManager.get(context).getAccounts();
    for (Account account : accounts) {
      if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
        accountSet.add(account.name);
      }
    }
    return accountSet;
  }

  public static int dpToPx(Resources res, int dp) {

    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
  }

  public static float parametric(float t, float min, float max) {

    return t * (max - min) + min;
  }

  /**
   * Clamps a value between lower and upper bounds. Formally, given value v,
   * and an interval [lower, upper], lower <= clamp(v, lower, upper) <= upper holds true
   *
   * @param v     the value to clamp
   * @param lower the lower bound of the interval
   * @param upper the upper bound of the interval
   * @return the clamped value of v in the [lower, upper] interval
   */
  public static float clamp(float v, float lower, float upper) {

    return Math.max(lower, Math.min(upper, v));
  }

  public static void setupMargins(Context context, View view) {

    Point point = SystemUtils.getScreenSize(context);
    // offset the settings icon so it appears above the navigation bar
    // for version that support translucent decor
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
    int navigationBarHeight = SystemUtils.getNavigationBarHeight(context);
    if (SystemUtils.versionAtLeast(Build.VERSION_CODES.KITKAT)
            && !ViewConfiguration.get(context).hasPermanentMenuKey()) {

      // the offset should be different for landscape orientation
      float offset = point.x < point.y ? 1.3f * navigationBarHeight : 0.8f * navigationBarHeight;
      Log.d(Log.TAG, "Margin set for translucent decor: " + (int) offset);
      // layoutParams.setMargins(left, top, right, bottom);
      layoutParams.setMargins(0, (int) offset - navigationBarHeight, 0, (int) offset);
    }
    else {
      Log.d(Log.TAG, "Margin set for normal decor: " + (int) (0.3f * navigationBarHeight));
      layoutParams.setMargins(0, (int) (0.3f * navigationBarHeight), 0, (int) (0.3f * navigationBarHeight));
    }
  }

  public static Bitmap getBitmapFromView(View view) {

    Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(returnedBitmap);
    Drawable bgDrawable = view.getBackground();
    if (bgDrawable != null) {
      bgDrawable.draw(canvas);
    }
    else {
      canvas.drawColor(Color.WHITE);
    }
    view.draw(canvas);
    return returnedBitmap;
  }

  public static Point getLocationInView(View src, View target) {

    final int[] l0 = new int[2];
    src.getLocationOnScreen(l0);

    final int[] l1 = new int[2];
    target.getLocationOnScreen(l1);

    l1[0] = l1[0] - l0[0] + target.getWidth() / 2;
    l1[1] = l1[1] - l0[1] + target.getHeight() / 2;

    return new Point(l1[0], l1[1]);
  }

  public static String getTimestamp(Object item, FridgeApplication application) {
    // "2014-11-20T17:33:38Z"

    String prefix = "";
    Calendar calendar = Calendar.getInstance();
    long currentTimesamp = calendar.getTime().getTime() / 1000; // seconds
    long itemTimestamp = 0;
    try {
      if (item instanceof FridgeItem)
      // itemTimestamp = application.dateFormat.parse(((FridgeItem) item).getExpirationDate()).getTime();
      // itemTimestamp = application.dateFormat.parse(((FridgeItem) item).getExpirationDate()).getTime();
      {
        itemTimestamp = ((FridgeItem) item).getExpirationDate();
      }
      else if (item instanceof HistoryItem) {
        itemTimestamp = ((HistoryItem) item).getTimestamp();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    String relativeTimeSince;
    long delta = currentTimesamp - itemTimestamp;

    if (item instanceof FridgeItem) {
      if (itemTimestamp > currentTimesamp) {
        prefix = application.getString(R.string.label_expires);
      }
      else {
        prefix = application.getString(R.string.label_expired);
      }
      return prefix + " " + TimeSpans.getRelativeTimeSince(itemTimestamp * 1000, currentTimesamp * 1000);
    }

    if (currentTimesamp - itemTimestamp < 60) {
      relativeTimeSince = application.getString(R.string.just_now);
    }
    else if (delta < 3600) {
      long minutes = delta / 60;
      if (minutes == 1) {
        relativeTimeSince = application.getString(R.string.one_minute_ago);
      }
      else {
        relativeTimeSince = minutes + " " + application.getString(R.string.minutes_ago);
      }
    }
    else {
      relativeTimeSince = TimeSpans.getRelativeTimeSince(itemTimestamp * 1000, currentTimesamp * 1000);
    }

    return prefix + relativeTimeSince;
  }

  /**
   * convert the given input stream into a string
   */
  public static String convertStreamToString(InputStream is) {

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();

    String line;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        is.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    return sb.toString();
  }

  public static int getAppVersion(Context context) {

    try {
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return packageInfo.versionCode;
    }
    catch (PackageManager.NameNotFoundException e) {
      // should never happen
      throw new RuntimeException("Could not get package name: " + e);
    }
  }

  public static boolean HaveNetworkConnection(Context ctx) {

    boolean HaveConnectedWifi = false;
    boolean HaveConnectedMobile = false;

    ConnectivityManager cm = (ConnectivityManager) ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
    for (NetworkInfo ni : netInfo) {
      if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
        if (ni.isConnected()) {
          HaveConnectedWifi = true;
        }
      }
      if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
        if (ni.isConnected()) {
          HaveConnectedMobile = true;
        }
      }
    }
    return HaveConnectedWifi || HaveConnectedMobile;
  }
}
