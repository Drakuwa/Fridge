package com.app.afridge.ui.fragments;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.views.AdvancedTextView;
import com.balysv.materialripple.MaterialRippleLayout;
import com.gc.materialdesign.views.Slider;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Custom settings dialog
 * <p/>
 * Created by drakuwa on 2/16/15.
 */
public class SettingsFragment extends DialogFragment {

  @InjectView(R.id.spinner_time_period)
  Spinner spinnerTimePeriod;
  @InjectView(R.id.checkbox_show_notifications)
  CheckBox checkBoxShowNotifications;
  @InjectView(R.id.slider_warning_days)
  Slider sliderWarningDays;
  @InjectView(R.id.text_warning_days_label)
  AdvancedTextView textWarningDays;
  @InjectView(R.id.text_measurement_type_value)
  AdvancedTextView textMeasurementTypeValue;
  @InjectView(R.id.button_done)
  AdvancedTextView buttonDone;

  private FridgeApplication application;

  OnFragmentInteractionListener mListener;

  // Singleton
  private static volatile SettingsFragment instance = null;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment SettingsFragment.
   */
  public static SettingsFragment getInstance() {

    if (instance == null) {
      synchronized (SettingsFragment.class) {
        if (instance == null) {
          instance = new SettingsFragment();
        }
      }
    }
    return instance;
  }

  public SettingsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme); // 0 is the default theme for the selected style
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View containerView = inflater.inflate(R.layout.fragment_settings, container, false);
    // inject and return the view
    ButterKnife.inject(this, containerView);
    return containerView;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    super.onViewCreated(view, savedInstanceState);

    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
            R.array.time_period_array, R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinnerTimePeriod.setAdapter(adapter);

    spinnerTimePeriod.setSelection(application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION_TIME));
    spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION_TIME, position);
        // re-initialize the service when we change time
        ((MainActivity) getActivity()).initExpirationDateService();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    textWarningDays.setText(String.format(application.getString(R.string.hint_warning_days_label),
            application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_EXP_DATE_WARNING)));
    sliderWarningDays.setValue(application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_EXP_DATE_WARNING));
    sliderWarningDays.setOnValueChangedListener(new Slider.OnValueChangedListener() {

      @Override
      public void onValueChanged(int i) {

        application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_EXP_DATE_WARNING, sliderWarningDays.getValue());
        textWarningDays.setText(String.format(application.getString(R.string.hint_warning_days_label),
                application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_EXP_DATE_WARNING)));
      }
    });

    checkBoxShowNotifications.setChecked(application.prefStore.getBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION));
    checkBoxShowNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        application.prefStore.setBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION, isChecked);
        checkBoxShowNotifications.setChecked(application.prefStore.getBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION));
        spinnerTimePeriod.setEnabled(application.prefStore.getBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION));
      }
    });

    textMeasurementTypeValue.setText(application.getResources().getStringArray(R.array.measurement_type_array)[
            application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE)]);

    MaterialRippleLayout.on(buttonDone)
            .rippleOverlay(true)
            .rippleAlpha(0.2f)
            .rippleColor(0xFF585858)
            .create();
  }

  @Override
  public void onDestroyView() {

    super.onDestroyView();
    ButterKnife.reset(this);
  }

  @Override
  public void onAttach(Activity activity) {

    super.onAttach(activity);
    application = ((FridgeApplication) activity.getApplication());
    try {
      mListener = (OnFragmentInteractionListener) activity;
    }
    catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
              + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {

    super.onDetach();
    mListener = null;
  }

  @OnClick(R.id.button_done)
  public void done(View view) {

    this.dismiss();
  }

  @OnClick(R.id.text_measurement_type)
  public void measurementTypeDialog(View view) {

    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
    builder.setTitle(R.string.pick_measurement_type)
            .setItems(R.array.measurement_type_array, new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE, which);
                textMeasurementTypeValue.setText(application.getResources().getStringArray(R.array.measurement_type_array)[which]);
              }
            });
    builder.create().show();
  }

  @Override
  public void onDismiss(final DialogInterface dialog) {

    super.onDismiss(dialog);
    final Activity activity = getActivity();
    if (activity != null && activity instanceof DialogInterface.OnDismissListener) {
      ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
    }
  }
}
