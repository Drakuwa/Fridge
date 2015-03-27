package com.app.afridge.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.interfaces.FragmentLifecycle;
import com.app.afridge.ui.fragments.wizard.CustomizeFragment;
import com.app.afridge.ui.fragments.wizard.ShoppingListFragment;
import com.app.afridge.ui.fragments.wizard.SocialLoginFragment;
import com.app.afridge.ui.fragments.wizard.WelcomeFragment;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.views.JazzyViewPager;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

// import android.os.PersistableBundle;


/**
 * First time user wizard
 * <p/>
 * Created by drakuwa on 3/17/15.
 */
public class FirstTimeWizardActivity extends AbstractActivity {

  private JazzyViewPager pager;
  private static final int[] TAB_TITLES = new int[] {1, 2, 3, 4};

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_first_time_wizard);

    // initialize the application
    FridgeApplication application = (FridgeApplication) getApplication();

    // set the PREF to true
    application.prefStore.setBoolean(SharedPrefStore.Pref.FIRST_TIME_WIZARD_COMPLETE, true);

    // set up a FragmentTitleAdapter from the support library
    FragmentPagerAdapter adapter = new FragmentTitleAdapter(getSupportFragmentManager());

    // initialize the ViewPager with the FragmentTitleAdapter adapter
    pager = (JazzyViewPager) findViewById(R.id.viewpager);
    pager.setTransitionEffect(JazzyViewPager.TransitionEffect.ZoomOutAndIn);
    pager.setAdapter(adapter);

    // set the page indicator
    PageIndicator mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
    mIndicator.setViewPager(pager);

    mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

      int currentPosition = 0;

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {

        FragmentLifecycle fragmentToShow = (FragmentLifecycle) ((FragmentTitleAdapter) pager.getAdapter()).getItem(position);
        fragmentToShow.onResumeFragment();

        FragmentLifecycle fragmentToHide = (FragmentLifecycle) ((FragmentTitleAdapter) pager.getAdapter()).getItem(currentPosition);
        fragmentToHide.onPauseFragment();

        currentPosition = position;
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

    // status and navigation bar height margin hack
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(R.id.height_hack).getLayoutParams();
    params.height = bottomMargin;
    findViewById(R.id.height_hack).setLayoutParams(params);
    LinearLayout.LayoutParams paramsStatus = (LinearLayout.LayoutParams) findViewById(R.id.status_height_hack).getLayoutParams();
    paramsStatus.height = statusBarHeight;
    findViewById(R.id.status_height_hack).setLayoutParams(paramsStatus);
  }

  @Override
  public void onBackPressed() {

    if (pager.getCurrentItem() == 0) {
      // If the user is currently looking at the first step, allow the system to handle the
      // Back button. This calls finish() on this activity and pops the back stack.
      startMainActivity();
      // super.onBackPressed();
    }
    else {
      // Otherwise, select the previous step.
      pager.setCurrentItem(pager.getCurrentItem() - 1);
    }
  }

  public void startMainActivity() {

    Intent mainIntent = new Intent(FirstTimeWizardActivity.this, MainActivity.class);
    startActivity(mainIntent);
    finish();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }

  /**
   * Titles provider class for the ViewPager
   */
  class FragmentTitleAdapter extends FragmentPagerAdapter {

    public FragmentTitleAdapter(FragmentManager fm) {

      super(fm);
    }

    @Override
    public Fragment getItem(int position) {

      if (position == 0) {
        return WelcomeFragment.newInstance(bottomMargin);
      }
      else if (position == 1) {
        return SocialLoginFragment.newInstance(bottomMargin);
      }
      else if (position == 2) {
        return ShoppingListFragment.newInstance(bottomMargin);
      }
      else if (position == 3) {
        return CustomizeFragment.newInstance(bottomMargin);
      }
      return WelcomeFragment.newInstance(bottomMargin);
    }

    @Override
    public CharSequence getPageTitle(int position) {

      return getString(TAB_TITLES[position % TAB_TITLES.length]);
    }

    @Override
    public int getCount() {

      return TAB_TITLES.length;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

      Object obj = super.instantiateItem(container, position);
      pager.setObjectForPosition(obj, position);
      return obj;
    }
  }
}
