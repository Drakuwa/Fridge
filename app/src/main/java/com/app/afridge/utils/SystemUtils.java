package com.app.afridge.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcelable;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class SystemUtils {

  public static int getStatusBarHeight(Context context) {

    int result = 0;
    int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = context.getResources().getDimensionPixelSize(resourceId);
    }
    Log.d(Log.TAG, "Status bar height: " + result);
    return result;
  }

  public static int getNavigationBarHeight(Context context) {

    int result = 0;
    int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = context.getResources().getDimensionPixelSize(resourceId);
    }
    Log.d(Log.TAG, "Navigation bar height: " + result);
    return result;
  }

  public static int getResourceId(Context context, String variableName, String resourceName) {

    try {
      return context.getResources().getIdentifier(variableName, resourceName, context.getPackageName());
    }
    catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  public static Point getScreenSize(Context context) {

    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();

    Point point = new Point();
    display.getSize(point);
    Log.d(Log.TAG, "Screen width: " + point.x + " height: " + point.y);
    return point;
  }

  public static boolean versionAtLeast(int version) {

    return Build.VERSION.SDK_INT >= version;
  }

  public static Intent createCustomChooser(Context context, Intent prototype, String[] whiteList) {

    List<Intent> targetedShareIntents = new ArrayList<Intent>();
    List<HashMap<String, String>> intentMetaInfo = new ArrayList<HashMap<String, String>>();
    Intent chooserIntent;

    Intent dummyIntent = new Intent(prototype.getAction());
    dummyIntent.setType(prototype.getType());
    List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(dummyIntent, 0);

    // if we cannot resolve the intent return the default intent chooser
    if (resolveInfoList.isEmpty()) {
      Log.d(Log.TAG, "Default chooser created");
      return Intent.createChooser(prototype, "");
    }

    // go through every app that can handle the intent
    for (ResolveInfo resolveInfo : resolveInfoList) {
      if (resolveInfo.activityInfo == null) {
        continue;
      }
      // get all the possible sharers
      HashMap<String, String> info = new HashMap<String, String>();
      info.put("packageName", resolveInfo.activityInfo.packageName);
      info.put("className", resolveInfo.activityInfo.name);
      // load the app label
      String appName = String.valueOf(resolveInfo.activityInfo.loadLabel(context.getPackageManager()));
      info.put("simpleName", appName);
      // add only white listed apps
      if (Arrays.asList(whiteList).contains(appName.toLowerCase())) {
        intentMetaInfo.add(info);
      }
    }

    // return the default chooser if none of the apps that can handle the intent are white listed
    if (intentMetaInfo.isEmpty()) {
      Log.d(Log.TAG, "Default chooser created");
      return Intent.createChooser(prototype, "");
    }

    // sorting for nice readability
    Collections.sort(intentMetaInfo, new Comparator<HashMap<String, String>>() {

      @Override
      public int compare(HashMap<String, String> map, HashMap<String, String> map2) {

        return map.get("simpleName").compareTo(map2.get("simpleName"));
      }
    });

    // create the custom intent list
    for (HashMap<String, String> metaInfo : intentMetaInfo) {
      Intent targetedShareIntent = (Intent) prototype.clone();
      targetedShareIntent.setPackage(metaInfo.get("packageName"));
      targetedShareIntent.setClassName(
              metaInfo.get("packageName"),
              metaInfo.get("className"));
      targetedShareIntents.add(targetedShareIntent);
    }

    // return the filtered intent list
    Log.d(Log.TAG, "Filtered chooser created");
    chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), "");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[targetedShareIntents.size()]));
    return chooserIntent;
  }
}
