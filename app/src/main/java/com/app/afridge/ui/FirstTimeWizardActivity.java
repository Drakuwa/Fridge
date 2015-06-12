package com.app.afridge.ui;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.Ingredient;
import com.app.afridge.dom.IngredientHelper;
import com.app.afridge.dom.IngredientsEvent;
import com.app.afridge.interfaces.FragmentLifecycle;
import com.app.afridge.ui.fragments.wizard.CustomizeFragment;
import com.app.afridge.ui.fragments.wizard.ShoppingListFragment;
import com.app.afridge.ui.fragments.wizard.SocialLoginFragment;
import com.app.afridge.ui.fragments.wizard.WelcomeFragment;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.views.JazzyViewPager;
import com.app.afridge.views.NonSwipableViewPager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import de.greenrobot.event.EventBus;
import me.relex.circleindicator.CircleIndicator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * First time user wizard
 * <p/>
 * Created by drakuwa on 3/17/15.
 */
public class FirstTimeWizardActivity extends AbstractActivity {

    private static final int[] TAB_TITLES = new int[]{1, 2, 3, 4};

    private NonSwipableViewPager pager;

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
        pager = (NonSwipableViewPager) findViewById(R.id.viewpager);
        pager.setTransitionEffect(JazzyViewPager.TransitionEffect.ZoomOutAndIn);
        pager.setAdapter(adapter);

        // set the page indicator
        CircleIndicator mIndicator = (CircleIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(pager);

        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int currentPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                setPagingEnabled(false);

                FragmentLifecycle fragmentToShow = (FragmentLifecycle) ((FragmentTitleAdapter) pager
                        .getAdapter()).getItem(position);
                fragmentToShow.onResumeFragment();

                FragmentLifecycle fragmentToHide = (FragmentLifecycle) ((FragmentTitleAdapter) pager
                        .getAdapter()).getItem(currentPosition);
                fragmentToHide.onPauseFragment();

                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // status and navigation bar height margin hack
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(
                R.id.height_hack).getLayoutParams();
        params.height = bottomMargin;
        findViewById(R.id.height_hack).setLayoutParams(params);
        LinearLayout.LayoutParams paramsStatus = (LinearLayout.LayoutParams) findViewById(
                R.id.status_height_hack).getLayoutParams();
        paramsStatus.height = statusBarHeight;
        findViewById(R.id.status_height_hack).setLayoutParams(paramsStatus);

        // get or initialize the ingredients
        if (new Select().from(Ingredient.class).execute().size() == 0) {
            // no ingredients...
            application.api.fcService.getIngredients(new Callback<List<IngredientHelper>>() {

                @Override
                public void success(final List<IngredientHelper> ingredientHelpers,
                        Response response) {
                    EventBus.getDefault().post(new IngredientsEvent("success"));
                    // run code on background thread
                    new SaveIngredientsAsyncTask(ingredientHelpers).execute();
                    //                    new Handler().post(new Runnable() {
                    //
                    //                        @Override
                    //                        public void run() {
                    //                            // Moves the current Thread into the background
                    //                            android.os.Process.setThreadPriority(
                    //                                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    //                            // save the ingredient list
                    //                            ActiveAndroid.beginTransaction();
                    //                            try {
                    //                                for (IngredientHelper ingredient : ingredientHelpers) {
                    //                                    new Ingredient(Integer.parseInt(ingredient.getId()),
                    //                                            ingredient.getNaziv()).save();
                    //                                }
                    //                                ActiveAndroid.setTransactionSuccessful();
                    //                            } finally {
                    //                                ActiveAndroid.endTransaction();
                    //                                EventBus.getDefault().post(new IngredientsEvent("success"));
                    //                            }
                    //                        }
                    //                    });
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {

        if (pager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            startMainActivity();
            // super.onBackPressed();
        } else {
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

    public void setPagingEnabled(boolean isPagingEnabled) {

        pager.setPagingEnabled(isPagingEnabled);
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
            } else if (position == 1) {
                return SocialLoginFragment.newInstance(bottomMargin);
            } else if (position == 2) {
                return ShoppingListFragment.newInstance(bottomMargin);
            } else if (position == 3) {
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

    /**
     * AsyncTask that saves all the ingredients in the local database
     */
    private class SaveIngredientsAsyncTask extends AsyncTask<Void, Void, Void> {

        private List<IngredientHelper> ingredientHelpers;

        public SaveIngredientsAsyncTask(
                List<IngredientHelper> ingredientHelpers) {
            this.ingredientHelpers = ingredientHelpers;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Moves the current Thread into the background
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
            // save the ingredient list
            ActiveAndroid.beginTransaction();
            try {
                for (IngredientHelper ingredient : ingredientHelpers) {
                    new Ingredient(Integer.parseInt(ingredient.getId()),
                            ingredient.getNaziv()).save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
                EventBus.getDefault().post(new IngredientsEvent("success"));
            }
            return null;
        }
    }
}
