package com.app.afridge.utils;

/**
 * Static variables used in the application
 * <p/>
 * Created by drakuwa on 10/22/14.
 */
public class Constants {

  public static final String GCM_SENDER_ID = "620360100574";
  public static final String FACEBOOK_APP_ID = "808847859167603";
  public static final String[] FACEBOOK_PERMISSIONS = {"public_profile, email, user_likes, user_friends"};

  public static final long SPLASH_SCREEN_DURATION = 1000; // duration in milliseconds

  // Shared preferences variables
  public static final String DEBUG_TAG = "Fridge";
  public static final String SHARED_PREFS_FILE = "fridgePrefs";
  public static final String SHARED_PREFS_TAG = DEBUG_TAG + " PREF";

  public static final int REQUEST_IMAGE_CAPTURE = 1;
  public static final int REQUEST_IMAGE_CHOOSE = 2;

  public static final String FEEDBACK_EMAIL = "drakuwa@gmail.com";
  public static final String FEEDBACK_SUBJECT = "Hey there, here’s an idea for you.";

  public static final String EXTRA_FRIEND_ID = "extraFriendId";
  public static final String EXTRA_FRIEND_NAME = "extraFriendName";
  public static final String EXTRA_FRIEND_IMAGE = "extraFriendImage";
  public static final String EXTRA_FRIEND_LASTNAME = "extraFriendLastName";
  public static final String EXTRA_FRIEND_EMAIL = "extraFriendEmail";
  public static final String EXTRA_FRIEND_GENDER = "extraFriendGender";
  public static final String EXTRA_IS_FRIEND = "extraIsFriend";
  public static final String EXTRA_ITEM_ID = "itemId";
  public static final String EXTRA_RESTART_LOADER = "isDatabaseChanged";
  public static final String EXTRA_FILTER_TYPE = "filterType";
  public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";

  // intent extras
  public static String EXTRA_FRAGMENT_NAME = "extraFragmentName";
  public static String EXTRA_ABOUT = "extraAbout";
  public static String FRAGMENT_FEED = "Feed";
  public static String FRAGMENT_SEND = "Send";
  public static String FRAGMENT_ME = "Me";
  public static String ABOUT_STORY = "Story";
  public static String ABOUT_PRIVACY = "Privacy";
  public static String ABOUT_TERMS = "Terms";
  public static final int PROFILE_ACTIVITY_REQUEST = 100;

  // main activity fragments
  public static String[] MAIN_FRAGMENTS = {FRAGMENT_FEED, FRAGMENT_SEND, FRAGMENT_ME};

  // in-app billing constants
  public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl6eufcrftaGX7oQyxGvWNYEBj07J22pQgqWuvQskGQ+vpaancU8mf95ue1OaqYvgWV/cAw2bfwbUgb9oTUPlF2ELmHH13/SY3mka5b9LgtsVumhlcYwG91MyeuGjdUHKEmgYyp+EUVqHGCHNZWiGsezzxkpu+d6TjBX25m3Ta+Kc2K5VgS8JI1zO6Z5jWuB1aXnzen11pR+VoiRgKJ1EzWBXu5EABgJZwFio/LyBOdYLd/k9XKlBrjPJC29OQ+pjWtJsEl3WzUZmLAGUjgOBeaszwbjNdAnbyo6IwL166/sZX0KdUt/6ss4XVUcKe2jCUxuxE8QHAi0DviSW4NW+3wIDAQAB";
  public static final String SKU_THOUGHTS = "more_thoughts"; // "android.test.purchased" for testing; "more_thoughts" for production
  public static final int PURCHASE_REQUEST = 101;
  public static String SERVER_HOST = "http://fridgecheck.com/fc";
  public static String ACTION_DISMISS = "dismiss";


  // types of fragments
  public static enum FragmentTypeSignIn {
    SIGN_IN,
    WELCOME,
    GET_STARTED,
    GOTO_FEED,
    GOTO_SEND
  }


  // send thought fragments
  public static enum FragmentTypeSend {
    SEND,
    OUT_OF_THOUGHTS,
    SUCCESS
  }
}
