package com.app.afridge.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefStore {

  private final SharedPreferences sharedPrefs;

  private SharedPrefStore(SharedPreferences sharedPrefs) {

    this.sharedPrefs = sharedPrefs;
  }

  public static SharedPrefStore load(Context context) {

    return new SharedPrefStore(context.getSharedPreferences(Constants.SHARED_PREFS_FILE, Context.MODE_PRIVATE));
  }

  public void set(Pref pref, String value) {

    Log.i(Constants.SHARED_PREFS_TAG, "SET " + pref.getKey() + "=" + value);
    sharedPrefs.edit()
            .putString(pref.name(), value)
            .apply();
  }

  public void setInt(Pref pref, int value) {

    Log.i(Constants.SHARED_PREFS_TAG, "SET " + pref.getKey() + "=" + value);
    sharedPrefs.edit()
            .putInt(pref.name(), value)
            .apply();
  }

  public void clear(Pref pref) {

    Log.i(Constants.SHARED_PREFS_TAG, "CLEAR " + pref.getKey());
    sharedPrefs.edit()
            .remove(pref.name())
            .apply();
  }

  public void clearAll() {

    sharedPrefs.edit()
            .clear()
            .apply();
  }

  public String getString(Pref pref) {

    String result = null;
    try {
      result = sharedPrefs.getString(pref.name(), null);
      Log.i(Constants.SHARED_PREFS_TAG, "GET " + pref.getKey() + "=" + result);
    }
    catch (Exception ignored) {

    }
    return result;
  }

  public int getInt(Pref pref) {

    int result = 0;
    try {
      result = sharedPrefs.getInt(pref.name(), 0);
      Log.i(Constants.SHARED_PREFS_TAG, "GET " + pref.getKey() + "=" + result);
    }
    catch (Exception ignored) {

    }
    return result;
  }

  public boolean getBoolean(Pref pref) {

    boolean result = false;
    try {
      result = sharedPrefs.getBoolean(pref.name(), false);
      Log.i(Constants.SHARED_PREFS_TAG, "GET " + pref.getKey() + "=" + result);
    }
    catch (Exception ignored) {

    }
    return result;
  }

  public void setBoolean(Pref pref, boolean value) {

    Log.i(Constants.SHARED_PREFS_TAG, "SET " + pref.getKey() + "=" + value);
    sharedPrefs.edit()
            .putBoolean(pref.name(), value)
            .apply();
  }

  public enum Pref {
    IS_SERVICE_RUNNING,
    SETTINGS_SHOW_NOTIFICATION,
    SETTINGS_SHOW_NOTIFICATION_TIME,
    SETTINGS_EXP_DATE_WARNING,
    SETTINGS_MEASUREMENT_TYPE,
    HAS_SHOWN_WELCOME,
    USER_ID,
    REGISTRATION_ID,
    SHOPPING_LIST_NAME,
    SHOPPING_LIST_LAST_EDITED,
    APP_VERSION,
    HAS_MIGRATED,
    USER,
    SYNC_SETUP_COMPLETE,
    FIRST_TIME_WIZARD_COMPLETE,
    STAT_RANDOM_SPIN, STAT_FRIDGE_OPEN;

    private String getKey() {

      return name();
    }
  }
}
