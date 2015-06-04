package com.app.afridge.sync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.app.afridge.AuthState;
import com.app.afridge.api.CloudantService;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import java.net.URISyntaxException;
import java.util.Calendar;


/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 * <p/>
 * Created by drakuwa on 3/12/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    // Global variables
//    private final AuthState authState;
//
//    private final SharedPrefStore prefStore;
//
    private final Context context;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {

        super(context, autoInitialize);
        this.context = context;
        // application = (FridgeApplication) context.getApplicationContext();
//        Gson gson = new GsonBuilder().create();
//        this.prefStore = SharedPrefStore.load(context);
//        this.authState = AuthState.load(gson, prefStore);
        /**
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        // mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {

        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
        // application = (FridgeApplication) context.getApplicationContext();
//        Gson gson = new GsonBuilder().create();
//        this.prefStore = SharedPrefStore.load(context);
//        this.authState = AuthState.load(gson, prefStore);
    /*
     * If your app uses a content resolver, get an instance of it
     * from the incoming Context
     */
        // mContentResolver = context.getContentResolver();
    }

    /**
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        // get the latest AuthState
        Gson gson = new GsonBuilder().create();
        SharedPrefStore prefStore = SharedPrefStore.load(context);
        AuthState authState = AuthState.load(gson, prefStore);
        /*
         * Put the data transfer code here.
         */
        Log.d(Log.TAG, "onPerformSync called");
        if (authState.isAuthenticated()) {
            Log.d(Log.TAG, "user is authenticated: " + authState.getUser().getId());
            try {
                CloudantService.with(getContext()).reloadReplicationSettings(authState.getUser());
            } catch (URISyntaxException e) {
                Log.d(Log.TAG, "URISyntaxException: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            // start the sync
            CloudantService.with(getContext()).startSynchronization(authState);
            // update sync timestamp
            Calendar calendar = Calendar.getInstance();
            long syncTimestamp = calendar.getTimeInMillis() / 1000;
            prefStore.set(SharedPrefStore.Pref.LAST_SYNC, String.valueOf(syncTimestamp));
        } else {
            Log.d(Log.TAG, "user is NOT authenticated");
        }
    }
}
