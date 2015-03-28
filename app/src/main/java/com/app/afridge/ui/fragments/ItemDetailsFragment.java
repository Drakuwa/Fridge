package com.app.afridge.ui.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transitions.everywhere.ChangeBounds;
import android.transitions.everywhere.TransitionManager;
import android.transitions.everywhere.hidden.ChangeText;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.ChangeType;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.dom.ItemType;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.interfaces.OnMeasurementTypeChangeListener;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.AnimationsController;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.KeyboardUtils;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.views.AdvancedAutoCompleteTextView;
import com.app.afridge.views.AdvancedTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * Fridge item details fragment
 * <p/>
 * Created by drakuwa on 2/9/15.
 */
public class ItemDetailsFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener,
        OnMeasurementTypeChangeListener {

  @InjectView(R.id.text_name)
  AdvancedTextView textName;
  @InjectView(R.id.edit_name)
  AdvancedAutoCompleteTextView textEditName;
  @InjectView(R.id.image_item)
  ImageView image;
  @InjectView(R.id.text_expiration)
  AdvancedTextView textExpiration;
  @InjectView(R.id.image_name_edit)
  ImageView imageEditName;
  @InjectView(R.id.text_quantity_add)
  AdvancedTextView textQuantityAdd;
  @InjectView(R.id.image_quantity)
  ImageView imageQuantity;
  @InjectView(R.id.text_quantity_label)
  AdvancedTextView textQuantityLabel;
  @InjectView(R.id.text_quantity)
  AdvancedAutoCompleteTextView textQuantity;
  @InjectView(R.id.image_quantity_delete)
  ImageView imageQuantityDelete;
  @InjectView(R.id.spinner_quantity_type)
  Spinner spinnerQuantityType;

  private static final String KEY_CONTENT = "ItemDetailsFragment:Content";
  private static final String KEY_CONTENT_ID = "ItemDetailsFragment:Id";
  private int bottomMargin = 0;
  private FridgeApplication application;
  private FridgeItem item;
  private FridgeItem originalItem;
  private int itemId;
  private View containerView;

  OnFragmentInteractionListener mListener;

  // Singleton
  private static volatile ItemDetailsFragment instance = null;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment ItemDetailsFragment.
   */
  public static ItemDetailsFragment getInstance(int bottomMargin) {

    if (instance == null) {
      synchronized (ItemDetailsFragment.class) {
        if (instance == null) {
          instance = new ItemDetailsFragment();
          instance.bottomMargin = bottomMargin;
        }
      }
    }
    return instance;
  }

  public ItemDetailsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    // check if we have arguments
    if (savedInstanceState == null && getArguments() != null) {
      // add options menu
      setHasOptionsMenu(true);
      // get the extras
      Bundle args = getArguments();
      itemId = args.getInt(Constants.EXTRA_ITEM_ID);
      item = new Select().from(FridgeItem.class).where("item_id = ?", String.valueOf(itemId)).executeSingle();
      try {
        originalItem = (FridgeItem) item.clone();
      }
      catch (CloneNotSupportedException e) {
        e.printStackTrace();
      }
    }

    if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
      bottomMargin = savedInstanceState.getInt(KEY_CONTENT);
      itemId = savedInstanceState.getInt(KEY_CONTENT_ID);
      item = new Select().from(FridgeItem.class).where("item_id = ?", String.valueOf(itemId)).executeSingle();
      try {
        originalItem = (FridgeItem) item.clone();
      }
      catch (CloneNotSupportedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    containerView = inflater.inflate(R.layout.fragment_item_details, container, false);

    // navigation bar height margin hack
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) containerView.findViewById(R.id.height_hack).getLayoutParams();
    params.height = (int) (bottomMargin * 0.85f);
    containerView.findViewById(R.id.height_hack).setLayoutParams(params);

    // inject and return the view
    ButterKnife.inject(this, containerView);
    return containerView;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    super.onViewCreated(view, savedInstanceState);

    // set name and image
    textName.setText(item.getName());
    AnimationsController.fadeInAndScale(textName);
    File itemType = new File(item.getType());

    // get the Picasso loader and request creator
    Picasso loader = Picasso.with(application.getApplicationContext());
    RequestCreator requestCreator;

    if (TextUtils.isDigitsOnly(item.getType())) {
      requestCreator = loader.load(ItemType.DRAWABLES[Integer.parseInt(item.getType())]);
    }
    else {
      requestCreator = loader.load(itemType);
    }
    requestCreator.resize(application.screenWidth / 2, application.screenWidth / 2)
            .centerInside()
            .transform(new CircleTransform())
            .error(R.drawable.fridge_placeholder)
            .into(image);

    // set expiration date
    if (item.getExpirationDate() != 0) {
      textExpiration.setText(application.dateFormat.format(new Date(item.getExpirationDate() * 1000)));
    }
    else {
      textExpiration.setText(application.getString(R.string.not_set));
    }
    AnimationsController.fadeInAndScale(textExpiration);

    // set quantity change listener
    // add a new note on IME_ACTION_DONE
    textQuantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {

      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (actionId == EditorInfo.IME_ACTION_DONE) {
          // save the change
          item.setQuantity(textQuantity.getText().toString());
          item.save();
          KeyboardUtils.hideSoftKeyboard(textQuantity);
          textQuantity.clearFocus();
          updateQuantityValue();
          return true;
        }
        return false;
      }
    });

    AnimationsController.fadeInAndScale(textQuantityAdd);
    if (item.getQuantity() != null && item.getQuantity().length() > 0) {
      showQuantity();
    }
  }

  private void updateQuantityValue() {

    if (item != null) {
      if (item.getQuantity() != null && item.getQuantity().length() > 0) {
        textQuantity.setText(item.getQuantity());
      }
      else {
        textQuantity.setText("");
        textQuantity.setHint(application.getString(R.string.not_set).toUpperCase(Locale.US));
      }
    }
  }

  private void updateQuantityType() {
    // get the selected measurement type
    int selectedMeasurementType = application.prefStore.getInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE);
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter;
    switch (selectedMeasurementType) {
      case 0: // metric
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.quantity_type_metric, R.layout.simple_spinner_item);
        break;
      case 1: // imperial
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.quantity_type_imperial, R.layout.simple_spinner_item);
        break;
      default:
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.quantity_type_metric, R.layout.simple_spinner_item);
        break;
    }

    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinnerQuantityType.setAdapter(adapter);

    spinnerQuantityType.setSelection(item.getTypeOfQuantity());
    spinnerQuantityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // set the new type of quantity
        item.setTypeOfQuantity(position);
        item.save();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
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

  @Override
  public void onSaveInstanceState(Bundle outState) {

    super.onSaveInstanceState(outState);
    outState.putInt(KEY_CONTENT, bottomMargin);
    outState.putInt(KEY_CONTENT_ID, itemId);
  }

  @Override
  public void onResume() {

    isNameEditMode = false;
    try {
      originalItem = (FridgeItem) item.clone();
    }
    catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    ((MainActivity) getActivity()).showContextMenu(true);
    ((MainActivity) getActivity()).setActionBarIcon(R.drawable.ic_arrow_back);
    ((MainActivity) getActivity()).setActionBarTitle(application.getString(R.string.item_details).toLowerCase(Locale.ENGLISH));
    ((MainActivity) getActivity()).setOnMeasurementTypeChangeListener(this);
    updateQuantityValue();
    updateQuantityType();
    super.onResume();
  }

  @Override
  public void onPause() {

    Log.d(Log.TAG, "onPause originalItem: " + originalItem.toString());
    Log.d(Log.TAG, "onPause item: " + item.toString());
    if (!originalItem.equals(item)) {
      // save the change if the item has changed
      HistoryItem historyItem = new HistoryItem(item,
              Calendar.getInstance().getTimeInMillis() / 1000, ChangeType.MODIFY);
      historyItem.save();
      Log.d(Log.TAG, "onPause save changes...");
    }
    super.onPause();
  }

  @OnClick(R.id.image_item)
  public void showImage(View view) {

    Dialog builder = new Dialog(getActivity());
    builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
    builder.getWindow().setBackgroundDrawable(
            new ColorDrawable(android.graphics.Color.TRANSPARENT));
    builder.setContentView(getActivity().getLayoutInflater().inflate(R.layout.dialog_image, (ViewGroup) containerView, false));
    builder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

      @Override
      public void onDismiss(DialogInterface dialogInterface) {
        //nothing;
      }
    });

    ImageView imageView = (ImageView) builder.findViewById(R.id.image);
    File itemType = new File(item.getType());
    if (itemType.exists()) {
      Picasso.with(application.getApplicationContext())
              .load(itemType)
              .resize(application.screenWidth / 2, application.screenWidth / 2)
              .error(R.mipmap.ic_launcher)
              .into(imageView);
    }
    else if (TextUtils.isDigitsOnly(item.getType())) {
      Picasso.with(application.getApplicationContext())
              .load(ItemType.DRAWABLES[Integer.parseInt(item.getType())])
              .error(R.mipmap.ic_launcher)
              .into(imageView);
    }
    else {
      // we have a missing path?
      Picasso.with(application.getApplicationContext())
              .load(itemType)
              .resize(application.screenWidth / 2, application.screenWidth / 2)
              .error(R.mipmap.ic_launcher)
              .into(imageView);
    }

    // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
    new PhotoViewAttacher(imageView);

    // show the dialog
    builder.show();
  }

  @OnClick(R.id.image_expiration_edit)
  public void editExpirationDate(View view) {
    // get the current date to set as default
    final Calendar c = Calendar.getInstance();
    if (item.getExpirationDate() > 0) {
      // if there is an expiration date set, send it as default
      c.setTimeInMillis(item.getExpirationDate() * 1000);
    }
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);

    Bundle b = new Bundle();
    b.putInt(DatePickerDialogFragment.YEAR, year);
    b.putInt(DatePickerDialogFragment.MONTH, month);
    b.putInt(DatePickerDialogFragment.DATE, day);
    DatePickerDialogFragment picker = new DatePickerDialogFragment();
    picker.setArguments(b);
    picker.setListener(this);
    picker.show(getActivity().getSupportFragmentManager(), "frag_date_picker");

    // DatePickerFragment datePickerFragment = new DatePickerFragment();
    // datePickerFragment.setListener(this);
    // datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
  }

  private boolean isNameEditMode = false;

  @OnClick(R.id.image_name_edit)
  public void editItemName(View view) {

    if (isNameEditMode) {
      textEditName.getText().clear();
    }
    else {
      TransitionManager.beginDelayedTransition((ViewGroup) containerView, new ChangeBounds());
      textName.setVisibility(View.GONE);
      textEditName.setVisibility(View.VISIBLE);
      textEditName.setText(item.getName());
      textEditName.requestFocus();
      KeyboardUtils.showSoftKeyboard(textEditName);
      imageEditName.setImageResource(R.drawable.ic_action_content_clear);
      isNameEditMode = true;

      textEditName.setOnEditorActionListener(new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

          if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!TextUtils.isEmpty(textEditName.getText())) {
              // save the item and set the new name
              item.setName(textEditName.getText().toString().trim());
              item.save();

              TransitionManager.beginDelayedTransition((ViewGroup) containerView, new ChangeBounds());
              textName.setVisibility(View.VISIBLE);
              textEditName.setVisibility(View.GONE);
              textName.setText(item.getName());
              KeyboardUtils.hideSoftKeyboard(textEditName);
              imageEditName.setImageResource(R.drawable.ic_edit);
              isNameEditMode = false;
            }
            else {
              textEditName.setError(application.getString(R.string.error_no_item_name));
              textEditName.requestFocus();
            }
            return true;
          }
          return false;
        }
      });

      // we must have a text change listener to clear the error if any is set
      textEditName.addTextChangedListener(new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

          textEditName.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
      });
    }
  }

  @Override
  public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    // Do something with the date chosen by the user
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, monthOfYear);
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

    // save the item and change the date
    item.setExpirationDate(calendar.getTimeInMillis() / 1000);
    item.save();
    TransitionManager.beginDelayedTransition((ViewGroup) containerView, new ChangeText());
    textExpiration.setText(application.dateFormat.format(new Date(item.getExpirationDate() * 1000)));
  }

  @OnClick(R.id.text_quantity_add)
  public void addQuantity(View view) {

    showQuantity();
  }

  private void showQuantity() {
    // show/hide the views
    TransitionManager.beginDelayedTransition((ViewGroup) containerView, new ChangeBounds());
    textQuantityAdd.setVisibility(View.GONE);

    imageQuantity.setVisibility(View.VISIBLE);
    textQuantityLabel.setVisibility(View.VISIBLE);
    textQuantity.setVisibility(View.VISIBLE);
    imageQuantityDelete.setVisibility(View.VISIBLE);
    spinnerQuantityType.setVisibility(View.VISIBLE);

    AnimationsController.fadeInAndScale(textQuantity);
    AnimationsController.fadeInAndScale(imageQuantityDelete);
    AnimationsController.fadeInAndScale(spinnerQuantityType);
  }

  @OnClick(R.id.image_quantity_delete)
  public void deleteQuantity(View view) {
    // show/hide the views
    TransitionManager.beginDelayedTransition((ViewGroup) containerView, new ChangeBounds());
    imageQuantity.setVisibility(View.GONE);
    textQuantityLabel.setVisibility(View.GONE);
    textQuantity.setVisibility(View.GONE);
    imageQuantityDelete.setVisibility(View.GONE);
    spinnerQuantityType.setVisibility(View.GONE);
    textQuantityAdd.setVisibility(View.VISIBLE);

    // delete the actual quantity to the item
    item.setTypeOfQuantity(-1);
    item.setQuantity("");
    item.save();

    // update the values
    updateQuantityValue();
    updateQuantityType();
  }

  @Override
  public void onMeasurementTypeChange() {

    Log.d(Log.TAG, "onMeasurementTypeChange");
    if (isAdded()) {
      updateQuantityType();
    }
  }
}
