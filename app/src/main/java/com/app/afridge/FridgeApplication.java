package com.app.afridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.app.Application;
import com.app.afridge.api.RestService;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.CustomDateFormatter;
import com.app.afridge.utils.DatabaseMigrationAsyncTask;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;
import com.crashlytics.android.Crashlytics;

import android.content.Context;
import android.graphics.Point;
import android.support.multidex.MultiDex;
import android.view.Display;
import android.view.WindowManager;

import io.fabric.sdk.android.Fabric;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


/**
 * Singleton application class that holds reusable app components
 * <p/>
 * Created by drakuwa on 1/27/15.
 */
public class FridgeApplication extends Application {

    private static boolean activityVisible;

    // storage
    public SharedPrefStore prefStore;

    // utils
    public SimpleDateFormat dateFormat;

    // api
    public RestService api;

    public Gson gson;

    public AuthState authState;

    public HashMap<Integer, ItemType> types = new HashMap<>(ItemType.DRAWABLES.length);

    public int screenWidth, screenHeight;

    public static boolean isActivityVisible() {

        return activityVisible;
    }

    public static void activityResumed() {

        activityVisible = true;
    }

    public static void activityPaused() {

        activityVisible = false;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        // initialize the crash reporting system
        if (BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        // initialize the ActiveAndroid library
        ActiveAndroid.initialize(this);

        // initialize the API
        gson = new GsonBuilder().create();
        RestAdapter loginAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SERVER_HOST)
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new RestAdapter.Log() {

                    @Override
                    public void log(String msg) {

                        Log.d(Log.TAG, "RETROFIT: " + msg);
                    }
                })
                .build();
        api = new RestService(loginAdapter);

        // initialize the shared preferences manager
        prefStore = SharedPrefStore.load(this);

        // initialize the user authentication state
        authState = AuthState.load(gson, prefStore);

        // initialize the custom date formatter
        dateFormat = CustomDateFormatter.getInstance().getDateFormat();

        // initialize the HashMap
        for (ItemType item : ItemType.values()) {
            types.put(ItemType.DRAWABLES[item.ordinal()], item);
        }

        // get screen size
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        if (Common.versionAtLeast(13)) {
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        } else {
            screenWidth = display.getWidth();  // deprecated
            screenHeight = display.getHeight(); // deprecated
        }

        // try to migrate old database users
        if (!prefStore.getBoolean(SharedPrefStore.Pref.HAS_MIGRATED)) {
            new DatabaseMigrationAsyncTask(this).execute();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
