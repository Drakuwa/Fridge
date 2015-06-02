package com.app.afridge.ui.fragments.wizard;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.interfaces.FragmentLifecycle;
import com.app.afridge.ui.FirstTimeWizardActivity;
import com.app.afridge.utils.AnimationsController;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.views.AdvancedTextView;
import com.app.afridge.views.SlideSwitch;
import com.app.afridge.views.Typewriter;
import com.balysv.materialripple.MaterialRippleLayout;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link CustomizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomizeFragment extends Fragment implements FragmentLifecycle {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_MARGIN = "bottomMargin";

    private static final Object itemLock = new Object();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bottomMargin bottom margin for translucent themes.
     * @return A new instance of fragment CustomizeFragment.
     */
    //  public static CustomizeFragment newInstance(int bottomMargin) {
    //
    //    CustomizeFragment fragment = new CustomizeFragment();
    //    Bundle args = new Bundle();
    //    args.putInt(ARG_PARAM_MARGIN, bottomMargin);
    //    fragment.setArguments(args);
    //    return fragment;
    //  }

    // Singleton
    private static volatile CustomizeFragment instance = null;

    @InjectView(R.id.text_title_customize)
    Typewriter textTitle;

    @InjectView(R.id.text_description_customize)
    Typewriter textDescription;

    @InjectView(R.id.text_measurement_type)
    Typewriter textMeasurementType;

    @InjectView(R.id.text_measurement_type_value)
    RelativeLayout textMeasurementTypeValue;

    @InjectView(R.id.text_show_notifications)
    Typewriter textShowNotification;

    @InjectView(R.id.switch_measurement_type)
    SlideSwitch switchMeasurementType;

    @InjectView(R.id.checkbox_show_notifications)
    CheckBox checkBoxShowNotifications;

    @InjectView(R.id.separator1)
    View separator1;

    @InjectView(R.id.separator2)
    View separator2;

    @InjectView(R.id.separator3)
    View separator3;

    @InjectView(R.id.button_done)
    AdvancedTextView buttonDone;

    private FridgeApplication application;

    private int bottomMargin = 0;

    private boolean alreadyShown = false;

    public CustomizeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CustomizeFragment.
     */
    public static CustomizeFragment newInstance(int bottomMargin) {

        if (instance == null) {
            synchronized (CustomizeFragment.class) {
                if (instance == null) {
                    instance = new CustomizeFragment();
                    instance.bottomMargin = bottomMargin;
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bottomMargin = getArguments().getInt(ARG_PARAM_MARGIN);
        }
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        application = ((FridgeApplication) activity.getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View containerView = inflater.inflate(R.layout.fragment_customize, container, false);

        // navigation bar height margin hack
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerView
                .findViewById(R.id.height_hack).getLayoutParams();
        params.height = (int) (bottomMargin * 0.85f);
        containerView.findViewById(R.id.height_hack).setLayoutParams(params);

        // inject and return the view
        ButterKnife.inject(this, containerView);
        return containerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        if (!alreadyShown) {
            textTitle.setVisibility(View.INVISIBLE);
            textDescription.setVisibility(View.INVISIBLE);
            textMeasurementType.setVisibility(View.INVISIBLE);
            textShowNotification.setVisibility(View.INVISIBLE);
            textMeasurementTypeValue.setVisibility(View.INVISIBLE);
            checkBoxShowNotifications.setVisibility(View.INVISIBLE);
            buttonDone.setVisibility(View.INVISIBLE);
            separator1.setAlpha(0f);
            separator2.setAlpha(0f);
            separator3.setAlpha(0f);
        }

        // set values
        String selectedType = application.getResources()
                .getStringArray(R.array.measurement_type_array)[
                application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE)];
        if (selectedType.equalsIgnoreCase(getString(R.string.metric))) {
            switchMeasurementType.setState(false);
        } else {
            switchMeasurementType.setState(true);
        }
        switchMeasurementType.setSlideListener(new SlideSwitch.SlideListener() {

            @Override
            public void open() {
                // imperial is selected
                application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE, 1);
            }

            @Override
            public void close() {
                // metric is selected
                application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE, 0);
            }
        });

        checkBoxShowNotifications.setChecked(
                application.prefStore.getBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION));
        checkBoxShowNotifications
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        application.prefStore
                                .setBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION,
                                        isChecked);
                        checkBoxShowNotifications.setChecked(application.prefStore
                                .getBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION));
                    }
                });

        MaterialRippleLayout.on(buttonDone)
                .rippleOverlay(true)
                .rippleAlpha(0.2f)
                .rippleColor(0xFF585858)
                .create();
    }

    @OnClick(R.id.text_measurement_type_metric)
    public void selectMetric() {

        switchMeasurementType.setState(false);
        application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE, 0);
    }

    @OnClick(R.id.text_measurement_type_imperial)
    public void selectImperial() {

        switchMeasurementType.setState(true);
        application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE, 1);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            animateText();
        }
    }

    private void animateText() {

        synchronized (itemLock) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    if (textTitle != null && textDescription != null && !alreadyShown) {
                        alreadyShown = true;

                        // show items
                        textTitle.setVisibility(View.VISIBLE);
                        textDescription.setVisibility(View.VISIBLE);

                        // set text
                        textTitle.initSpanText(getString(R.string.customize_title),
                                getResources().getColor(R.color.primary_dark));
                        textDescription.initSpanText(getString(R.string.customize_description),
                                getResources().getColor(R.color.text_secondary));

                        // animate
                        textTitle.animateText();
                        textDescription.animateText();
                        textMeasurementType.setVisibility(View.INVISIBLE);
                        AnimationsController.fadeIn(textMeasurementType);
                        AnimationsController.fadeIn(textShowNotification);
                        AnimationsController.fadeIn(textMeasurementTypeValue);
                        AnimationsController.fadeIn(checkBoxShowNotifications);
                        AnimationsController.fadeIn(buttonDone);
                        AnimationsController.fadeInAndScale(separator1);
                        AnimationsController.fadeInAndScale(separator2);
                        AnimationsController.fadeInAndScale(separator3);
                    }
                    // enable the view pager
                    ((FirstTimeWizardActivity) getActivity()).setPagingEnabled(true);
                }
            }, 100);
        }
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }

    @OnClick(R.id.button_done)
    public void startMainActivity() {

        ((FirstTimeWizardActivity) getActivity()).startMainActivity();
    }
}
