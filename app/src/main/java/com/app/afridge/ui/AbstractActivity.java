package com.app.afridge.ui;

import com.app.afridge.FridgeApplication;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;


/**
 * An abstract activity class that would be implemented in applications activities
 * <p/>
 * Created by drakuwa on 11/11/14.
 */
public abstract class AbstractActivity extends AppCompatActivity {

    // utilities
    protected FridgeApplication application;

    protected int bottomMargin = 0;

    protected int statusBarHeight = 0;

    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // get the application instance
        application = ((FridgeApplication) getApplication());

        // action bar indeterminate progress bar feature
        if (savedInstanceState == null) {
            supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        }

        // set the margin hack for translucent versions of the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            bottomMargin = getResources()
                    .getDimensionPixelSize(getResources()
                            .getIdentifier("navigation_bar_height", "dimen", "android"));
            statusBarHeight = getResources()
                    .getDimensionPixelSize(
                            getResources().getIdentifier("status_bar_height", "dimen", "android"));

        }

        // allow transitions for Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

        // hack for devices with physical menu key to have action bar overflow menu
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        FridgeApplication.activityResumed();
    }

    @Override
    protected void onPause() {

        super.onPause();
        FridgeApplication.activityPaused();
    }

    public void showProgress(boolean visible) {

        setSupportProgressBarIndeterminateVisibility(visible);
    }

    public boolean isTablet() {

        return isTablet;
    }

    public void setIsTablet(boolean isPhone) {

        this.isTablet = isPhone;
    }
}
