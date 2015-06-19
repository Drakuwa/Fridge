package com.app.afridge.utils;

/**
 * Static variables used in the application
 * <p/>
 * Created by drakuwa on 10/22/14.
 */
public class Constants {

    public static final String FACEBOOK_APP_ID = "808847859167603";

    public static final String[] FACEBOOK_PERMISSIONS = {
            "public_profile, email, user_likes, user_friends"};

    // Shared preferences variables
    public static final String DEBUG_TAG = "Fridge";

    public static final String SHARED_PREFS_FILE = "fridgePrefs";

    public static final String SHARED_PREFS_TAG = DEBUG_TAG + " PREF";

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final int REQUEST_IMAGE_CHOOSE = 2;

    public static final String FEEDBACK_EMAIL = "drakuwa@gmail.com";

    public static final String FEEDBACK_SUBJECT = "Hey there, here’s an idea for you.";

    public static final String EXTRA_ITEM_ID = "itemId";

    public static final String EXTRA_HISTORY_ITEM_ID = "historyItemId";

    public static final String EXTRA_RESTART_LOADER = "isDatabaseChanged";

    public static final String EXTRA_FILTER_TYPE = "filterType";

    public static final String EXTRA_NOTIFICATION_ID = "notificationId";

    public static final String EXTRA_ACTION = "widgetAction";

    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";

    public static final int DISMISS_NOTIFICATION_LENGTH = 5000;

    public static String SERVER_HOST = "http://fridgecheck.com/fc";

    public static String ACTION_DELETE = "com.app.afridge.DELETE_ITEM";

    public static String ACTION_UNDO = "com.app.afridge.UNDO_DELETE";

    public static String BITCOIN_WALLET_ADDRESS = "1wmbiP5H29Jtiw7iYzRLAUMzU4zJDvabp";
}
