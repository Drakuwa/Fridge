package com.app.afridge.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.app.afridge.R;
import com.app.afridge.dom.Ingredient;
import com.app.afridge.dom.IngredientHelper;
import com.app.afridge.dom.MenuType;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.interfaces.OnMeasurementTypeChangeListener;
import com.app.afridge.interfaces.Screenshotable;
import com.app.afridge.receivers.BootReceiver;
import com.app.afridge.services.ExpirationDateService;
import com.app.afridge.ui.fragments.AddItemFallbackFragment;
import com.app.afridge.ui.fragments.AddItemFragment;
import com.app.afridge.ui.fragments.FridgeFragment;
import com.app.afridge.ui.fragments.HistoryFragment;
import com.app.afridge.ui.fragments.ItemDetailsFragment;
import com.app.afridge.ui.fragments.NotesFragment;
import com.app.afridge.ui.fragments.ProfileFragment;
import com.app.afridge.ui.fragments.SettingsFragment;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.utils.animations.SupportAnimator;
import com.app.afridge.utils.animations.ViewAnimationUtils;
import com.app.afridge.views.AdvancedTextView;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AbstractActivity implements OnMenuItemClickListener,
        OnFragmentInteractionListener, DialogInterface.OnDismissListener {

  @SuppressWarnings("UnusedDeclaration")
  private static final long ONE_DAY = 24 * 60 * 60 * 1000;
  private Screenshotable screenshotable;

  boolean isDatabaseChanged = false;
  Toolbar toolbar;
  MenuItem settingsMenuItem, notesMenuItem, historyMenuItem, profileMenuItem;
  ContextMenuDialogFragment mMenuDialogFragment;
  ArrayList<MenuObject> menuObjects = new ArrayList<>();
  OnMeasurementTypeChangeListener listener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    // check if the welcome wizard has been shows
    if (!application.prefStore.getBoolean(SharedPrefStore.Pref.FIRST_TIME_WIZARD_COMPLETE)) {
      Intent wizardIntent = new Intent(MainActivity.this, FirstTimeWizardActivity.class);
      startActivity(wizardIntent);
      finish();
      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    else {
      setContentView(R.layout.activity_main);

      // setup the new Lollipop Toolbar
      toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
      if (toolbar != null) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        setActionBarIcon(R.mipmap.ic_launcher);
      }

      // increment the fridge opened counter
      int fridgeOpenCount = application.prefStore.getInt(SharedPrefStore.Pref.STAT_FRIDGE_OPEN);
      application.prefStore.setInt(SharedPrefStore.Pref.STAT_FRIDGE_OPEN, ++fridgeOpenCount);

      // setup context menu
      menuObjects.add(MenuType.CLOSE.ordinal(), new MenuObject(R.drawable.ic_close));
      menuObjects.add(MenuType.NOTES.ordinal(), new MenuObject(R.drawable.ic_note, getString(R.string.menu_fridge_notes)));
      menuObjects.add(MenuType.HISTORY.ordinal(), new MenuObject(R.drawable.ic_history, getString(R.string.menu_history)));
      if (application.authState.isAuthenticated()) {
        menuObjects.add(MenuType.PROFILE.ordinal(), new MenuObject(R.drawable.ic_user, getString(R.string.menu_profile)));
      }
      else {
        menuObjects.add(MenuType.PROFILE.ordinal(), new MenuObject(R.drawable.ic_user, getString(R.string.menu_login)));
      }
      menuObjects.add(MenuType.SETTINGS.ordinal(), new MenuObject(R.drawable.ic_settings, getString(R.string.menu_settings)));
      menuObjects.add(MenuType.FEEDBACK.ordinal(), new MenuObject(R.drawable.ic_mail, getString(R.string.menu_feedback)));

      // status bar height margin hack
      LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(R.id.height_hack).getLayoutParams();
      params.height = statusBarHeight;
      findViewById(R.id.height_hack).setLayoutParams(params);

      mMenuDialogFragment = ContextMenuDialogFragment.newInstance(
              (int) getResources().getDimension(R.dimen.toolbar_height), menuObjects, 0, 50);

      // get or initialize the ingredients
      if (new Select().from(Ingredient.class).execute().size() == 0) {
        // no ingredients...
        application.api.fcService.getIngredients(new Callback<List<IngredientHelper>>() {

          @Override
          public void success(final List<IngredientHelper> ingredientHelpers, Response response) {
            // run code on background thread
            new Handler().post(new Runnable() {

              @Override
              public void run() {
                // Moves the current Thread into the background
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                // save the ingredient list
                ActiveAndroid.beginTransaction();
                try {
                  for (IngredientHelper ingredient : ingredientHelpers) {
                    new Ingredient(Integer.parseInt(ingredient.getId()), ingredient.getNaziv()).save();
                  }
                  ActiveAndroid.setTransactionSuccessful();
                }
                finally {
                  ActiveAndroid.endTransaction();
                }
              }
            });
          }

          @Override
          public void failure(RetrofitError error) {

          }
        });
      }

      // initialize the service if it is not already running
      if (!application.prefStore.getBoolean(SharedPrefStore.Pref.IS_SERVICE_RUNNING)) {
        initExpirationDateService();
      }

      // TODO remove this, only for testing
      //      showNotificatgionTest();

      if (savedInstanceState == null) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, FridgeFragment.getInstance(bottomMargin))
                .commit();
      }

      // if we're starting from a notification
      if (getIntent().hasExtra(Constants.EXTRA_ITEM_ID)) {
        Fragment fragment = ItemDetailsFragment.getInstance(bottomMargin);

        // set the item id
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRA_ITEM_ID, getIntent().getIntExtra(Constants.EXTRA_ITEM_ID, 0));
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0)
                .addToBackStack(null)
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();
      }
    }
  }

  public void setActionBarIcon(int iconRes) {

    toolbar.setNavigationIcon(iconRes);
  }

  public void setActionBarTitle(String title) {

    ((AdvancedTextView) toolbar.findViewById(R.id.toolbar_title)).setText(title);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    if (Common.versionAtLeast(11)) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
    }
    else {
      getMenuInflater().inflate(R.menu.menu_main_fallback, menu);
      if (application.authState.isAuthenticated()) {
        profileMenuItem.setTitle(getString(R.string.menu_profile));
      }
      else {
        profileMenuItem.setTitle(getString(R.string.menu_login));
      }
    }
    settingsMenuItem = menu.findItem(R.id.action_settings);
    notesMenuItem = menu.findItem(R.id.action_notes);
    historyMenuItem = menu.findItem(R.id.action_history);
    profileMenuItem = menu.findItem(R.id.action_profile);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == android.R.id.home) {
      getSupportFragmentManager().popBackStack();
      findViewById(R.id.container).setBackgroundResource(0);
      return true;
    }
    else if (id == R.id.action_settings) {
      Fragment f = getSupportFragmentManager().findFragmentByTag(getString(R.string.context_menu_dialog_fragment));
      if (f != null && f instanceof ContextMenuDialogFragment) {
        // menu is added, do nothing
        Log.d(Log.TAG, "menu fragment is already added");
      }
      else {
        mMenuDialogFragment.show(getSupportFragmentManager(), getString(R.string.context_menu_dialog_fragment));
      }
    }
    else if (id == R.id.action_notes) {
      // Notes
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction
              .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
              .replace(R.id.container, NotesFragment.getInstance(bottomMargin))
              .setTransition(FragmentTransaction.TRANSIT_NONE)
              .commit();
    }
    else if (id == R.id.action_history) {
      // History
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction
              .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
              .replace(R.id.container, HistoryFragment.getInstance(bottomMargin))
              .setTransition(FragmentTransaction.TRANSIT_NONE)
              .commit();
    }
    else if (id == R.id.action_menu_settings) {
      // Settings
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      DialogFragment fragment = SettingsFragment.getInstance();
      fragment.show(fragmentTransaction, "settings");
    }
    else if (id == R.id.action_feedback) {
      // Feedback
      Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
              "mailto", Constants.FEEDBACK_EMAIL, null));
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.FEEDBACK_SUBJECT);
      startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }
    else if (id == R.id.action_profile) {
      // Login/Profile
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      DialogFragment fragment = ProfileFragment.getInstance();
      fragment.show(fragmentTransaction, "profile");
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    // getSupportFragmentManager().popBackStack();
    findViewById(R.id.container).setBackgroundResource(0);
    super.onBackPressed();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    /**
     * This is required only if you are using Google Plus, the issue is that there SDK
     * require Activity to launch Auth, so library can't receive onActivityResult in fragment
     */
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(Constants.SOCIAL_NETWORK_TAG);
    if (fragment != null) {


      fragment.onActivityResult(requestCode, resultCode, data);
    }
  }

  //    @Override TODO needed by ASNE fragment for orientation change?
  //    public void onConfigurationChanged(Configuration newConfig) {
  //        super.onConfigurationChanged(newConfig);
  //        Fragment fragment = getSupportFragmentManager().findFragmentByTag(Constants.SOCIAL_NETWORK_TAG);
  //        if (fragment != null) {
  //            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
  //            getSupportFragmentManager().executePendingTransactions();
  //        }
  //    }

  @Override
  public void onMenuItemClick(View view, int i) {
    // handle menu item clicks
    if (i == MenuType.CLOSE.ordinal()) {
      Log.d(Log.TAG, "close menu");
    }
    else if (i == MenuType.NOTES.ordinal()) {
      // Notes
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction
              .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
              .replace(R.id.container, NotesFragment.getInstance(bottomMargin))
              .setTransition(FragmentTransaction.TRANSIT_NONE)
              .commit();
    }
    else if (i == MenuType.SETTINGS.ordinal()) {
      // Settings
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      DialogFragment fragment = SettingsFragment.getInstance();
      fragment.show(fragmentTransaction, "settings");
      //            fragment.onDismiss(new DialogInterface() {
      //
      //                @Override
      //                public void cancel() {
      //
      //                }
      //
      //                @Override
      //                public void dismiss() {
      //
      //                }
      //
      //            });
    }
    else if (i == MenuType.HISTORY.ordinal()) {
      // History
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction
              .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
              .replace(R.id.container, HistoryFragment.getInstance(bottomMargin))
              .setTransition(FragmentTransaction.TRANSIT_NONE)
              .commit();
    }
    else if (i == MenuType.FEEDBACK.ordinal()) {
      // Feedback
      Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
              "mailto", Constants.FEEDBACK_EMAIL, null));
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.FEEDBACK_SUBJECT);
      startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }
    else if (i == MenuType.PROFILE.ordinal()) {
      // Login/Profile
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.addToBackStack(null);
      DialogFragment fragment = ProfileFragment.getInstance();
      fragment.show(fragmentTransaction, "profile");
    }
  }

  @Override
  public void onFragmentInteraction(boolean addToBackstack, Point buttonCenter, Object fragmentClass) {
    // get a fragment transaction object
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    // start the circular reveal animation
    View view = findViewById(R.id.container);
    int finalRadius = Math.max(view.getWidth(), view.getHeight());
    // createCircularReveal(View view, int centerX, int centerY, float startRadius, float endRadius)
    SupportAnimator animator = ViewAnimationUtils.createCircularReveal(view, buttonCenter.x, buttonCenter.y, 0, finalRadius);
    animator.setInterpolator(new AccelerateInterpolator());
    animator.setDuration(350);
    if (Common.versionAtLeast(16)) {
      view.setBackground(new BitmapDrawable(getResources(), screenshotable.getBitmap()));
    }
    else {
      view.setBackgroundDrawable(new BitmapDrawable(getResources(), screenshotable.getBitmap()));
    }

    animator.start();

    if (addToBackstack) {
      fragmentTransaction.addToBackStack(null);
    }
    else {
      // dirty magic hack ... should check if we should pop back stack or not
      // if calling the reveal animation from AddItemFragment, we should do it ...
      getSupportFragmentManager().popBackStackImmediate();
    }

    if (fragmentClass.equals(FridgeFragment.class.getCanonicalName())) {
      Fragment fragment = FridgeFragment.getInstance(bottomMargin);
      fragmentTransaction.replace(R.id.container, fragment).commit();
      view.setBackgroundResource(0);
    }
    else if (fragmentClass.equals(AddItemFragment.class.getCanonicalName())) {
      // if we have an older version, get the fallback fragment
      if (Common.versionAtLeast(16)) {
        fragmentTransaction.replace(R.id.container,
                AddItemFragment.getInstance(bottomMargin)).commit();
      }
      else {
        fragmentTransaction.replace(R.id.container,
                AddItemFallbackFragment.getInstance(bottomMargin)).commit();
      }
      view.setBackgroundResource(0);
    }
  }

  public void setScreenshotable(Screenshotable screenshotable) {

    this.screenshotable = screenshotable;
  }

  public void setDatabaseChanged(boolean databaseChanged) {

    this.isDatabaseChanged = databaseChanged;
  }

  public boolean isDatabaseChanged() {

    return isDatabaseChanged;
  }

  public void showContextMenu(final boolean showContextMenu) {
    // if we don't wait for 100ms, and call this method in onResume,
    // the menu item is null...
    new Handler().postDelayed(new Runnable() {

      @Override
      public void run() {

        if (Common.versionAtLeast(11) && settingsMenuItem != null) {
          settingsMenuItem.setVisible(showContextMenu);
        }
        else {
          if (notesMenuItem != null) {
            notesMenuItem.setVisible(showContextMenu);
          }
          if (historyMenuItem != null) {
            historyMenuItem.setVisible(showContextMenu);
          }
        }
      }
    }, 120);
  }

  public void setOnMeasurementTypeChangeListener(OnMeasurementTypeChangeListener listener) {

    this.listener = listener;
  }

  /**
   * Dismiss listener for settings and profile fragment dialogs
   *
   * @param dialog fragment dialog
   */
  @Override
  public void onDismiss(DialogInterface dialog) {

    if (listener != null) {
      listener.onMeasurementTypeChange();
    }
    if (Common.versionAtLeast(11)) {
      if (application.authState.isAuthenticated()) {
        menuObjects.get(MenuType.PROFILE.ordinal()).setTitle(getString(R.string.menu_profile));
      }
      else {
        menuObjects.get(MenuType.PROFILE.ordinal()).setTitle(getString(R.string.menu_login));
      }
    }
    else {
      // could take a while for the profileMenuItem to initialize, that's why we use this hack
      new Handler().postDelayed(new Runnable() {

        @Override
        public void run() {

          if (application.authState.isAuthenticated()) {
            if (profileMenuItem != null) {
              profileMenuItem.setTitle(getString(R.string.menu_profile));
            }
          }
          else {
            if (profileMenuItem != null) {
              profileMenuItem.setTitle(getString(R.string.menu_login));
            }
          }
        }
      }, 100);
    }
  }

  /**
   * Start the service which will run every day
   */
  public void initExpirationDateService() {
    // get the service intent and alarm manager
    Intent intent = new Intent(this, ExpirationDateService.class);
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);

    // stop the alarm if running
    alarmManager.cancel(pIntent);

    // enable the boot receiver
    ComponentName receiver = new ComponentName(this, BootReceiver.class);
    PackageManager pm = this.getPackageManager();

    pm.setComponentEnabledSetting(receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);

    // set the new time
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MINUTE, 0);
    switch (application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION_TIME)) {
      case 0:
        cal.set(Calendar.HOUR_OF_DAY, 9);
        break;
      case 1:
        cal.set(Calendar.HOUR_OF_DAY, 13);
        break;
      case 2:
        cal.set(Calendar.HOUR_OF_DAY, 17);
        break;
      case 3:
        cal.set(Calendar.HOUR_OF_DAY, 20);
        break;
    }

    // With setInexactRepeating(), you have to use one of the AlarmManager interval
    // constants - in this case, AlarmManager.INTERVAL_DAY.
    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY, pIntent);
  }

  //  private void showNotificatgionTest() {
  //    // get the service intent and alarm manager
  //    Intent intent = new Intent(this, ExpirationDateService.class);
  //    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
  //    PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);
  //
  //    // set the new time
  //    Calendar cal = Calendar.getInstance();
  //    cal.add(Calendar.SECOND, 10);
  //
  //    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
  //            AlarmManager.INTERVAL_DAY, pIntent);
  //  }
}
