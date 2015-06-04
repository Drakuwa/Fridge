package com.app.afridge.utils;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import android.content.Context;


public class SharedPrefStore {

    private final DB sharedPrefs;

    private SharedPrefStore(DB sharedPrefs) {

        this.sharedPrefs = sharedPrefs;
    }

    public static SharedPrefStore load(Context context) {

        try {
            return new SharedPrefStore(DBFactory.open(context, Constants.SHARED_PREFS_FILE));
        } catch (SnappydbException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void set(Pref pref, String value) {

        Log.i(Constants.SHARED_PREFS_TAG, "SET " + pref.getKey() + "=" + value);
        try {
            sharedPrefs.put(pref.name(), value);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void setInt(Pref pref, int value) {

        Log.i(Constants.SHARED_PREFS_TAG, "SET " + pref.getKey() + "=" + value);
        try {
            sharedPrefs.putInt(pref.name(), value);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void clear(Pref pref) {

        Log.i(Constants.SHARED_PREFS_TAG, "CLEAR " + pref.getKey());
        try {
            sharedPrefs.del(pref.name());
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void clearAll() {

        try {
            sharedPrefs.destroy();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public String getString(Pref pref) {

        String result = null;
        try {
            result = sharedPrefs.get(pref.name());
            Log.i(Constants.SHARED_PREFS_TAG, "GET " + pref.getKey() + "=" + result);
        } catch (Exception ignored) {

        }
        return result;
    }

    public int getInt(Pref pref) {

        int result = 0;
        try {
            result = sharedPrefs.getInt(pref.name());
            Log.i(Constants.SHARED_PREFS_TAG, "GET " + pref.getKey() + "=" + result);
        } catch (Exception ignored) {

        }
        return result;
    }

    public boolean getBoolean(Pref pref) {

        boolean result = false;
        try {
            result = sharedPrefs.getBoolean(pref.name());
            Log.i(Constants.SHARED_PREFS_TAG, "GET " + pref.getKey() + "=" + result);
        } catch (Exception ignored) {

        }
        return result;
    }

    public void setBoolean(Pref pref, boolean value) {

        Log.i(Constants.SHARED_PREFS_TAG, "SET " + pref.getKey() + "=" + value);
        try {
            sharedPrefs.putBoolean(pref.name(), value);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
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
        LAST_SYNC,
        FIRST_TIME_WIZARD_COMPLETE,
        STAT_RANDOM_SPIN,
        STAT_FRIDGE_OPEN;

        private String getKey() {

            return name();
        }
    }
}
