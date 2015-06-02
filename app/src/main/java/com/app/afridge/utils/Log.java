package com.app.afridge.utils;

import com.app.afridge.BuildConfig;


/**
 * Override of the default Android Log class that checks if the version
 * is a debug-able build or a release build
 * <p/>
 * Created by drakuwa on 10/11/14.
 */
public class Log {

    public static String TAG = Constants.DEBUG_TAG;

    private static boolean isEnabled = BuildConfig.DEBUG;

    public static void d(String tag, String message) {

        if (isEnabled) {
            android.util.Log.d(tag, message);
        }
    }

    public static void d(String message) {

        if (isEnabled) {
            android.util.Log.d(TAG, message);
        }
    }

    public static void i(String tag, String message) {

        if (isEnabled) {
            android.util.Log.i(tag, message);
        }
    }

    public static void i(String message) {

        if (isEnabled) {
            android.util.Log.i(TAG, message);
        }
    }

    public static void e(String tag, String message) {

        if (isEnabled) {
            android.util.Log.e(tag, message);
        }
    }

    public static void v(String tag, String message) {

        if (isEnabled) {
            android.util.Log.v(tag, message);
        }
    }

    public static void w(String tag, String message) {

        if (isEnabled) {
            android.util.Log.w(tag, message);
        }
    }
}
