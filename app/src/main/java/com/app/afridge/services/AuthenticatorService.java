package com.app.afridge.services;

import com.app.afridge.sync.Authenticator;
import com.app.afridge.utils.Log;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * A bound Service that instantiates the authenticator
 * when started.
 * <p/>
 * Created by drakuwa on 3/12/15.
 */
public class AuthenticatorService extends Service {

    // The account name
    public static final String ACCOUNT_NAME = "fridgeSync";

    // An account type, in the form of a domain name
    private static final String ACCOUNT_TYPE = "com.app.afridge";

    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;

    /**
     * Obtain a handle to the {@link android.accounts.Account} used for sync in this application.
     *
     * @return Handle to application's account (not guaranteed to resolve unless CreateSyncAccount()
     * has been called)
     */
    public static Account GetAccount() {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        final String accountName = ACCOUNT_NAME;
        return new Account(accountName, ACCOUNT_TYPE);
    }

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
        Log.i(Log.TAG, "Authenticator Service created");
    }

    @Override
    public void onDestroy() {

        Log.i(Log.TAG, "Authenticator Service destroyed");
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {

        return mAuthenticator.getIBinder();
    }
}
