package com.app.afridge.ui.fragments;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.adapters.IngredientsAutocompleteAdapter;
import com.app.afridge.adapters.ItemCarouselAdapter;
import com.app.afridge.dom.enums.ChangeType;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.interfaces.Screenshotable;
import com.app.afridge.ui.GalleryActivity;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.FileUtils;
import com.app.afridge.utils.KeyboardUtils;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AdvancedTextView;
import com.app.afridge.views.FancyCoverFlow;
import com.gc.materialdesign.widgets.SnackBar;
import com.melnykov.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Add new fridge item fallback for older OS versions
 * <p/>
 * Created by drakuwa on 1/29/15.
 */
public class AddItemFallbackFragment extends Fragment implements Screenshotable {

  @InjectView(R.id.image_item)
  ImageView imageItem;
  @InjectView(R.id.horizontal_picker)
  FancyCoverFlow horizontalPicker;
  @InjectView(R.id.edit_ingredient)
  MaterialAutoCompleteTextView autoCompleteTextView;
  @InjectView(R.id.text_item_type)
  AdvancedTextView textType;
  @InjectView(R.id.button_create)
  FloatingActionButton buttonCreate;

  private static final String KEY_CONTENT = "AddItemFragment:Content";
  private int bottomMargin = 0;
  private FridgeApplication application;
  private Bitmap bitmap;
  private View containerView;

  private File mFileTemp = null;
  // make sure to set Target as strong reference
  private Target loadTarget;

  private OnFragmentInteractionListener mListener;

  // Singleton
  private static volatile AddItemFallbackFragment instance = null;

  private boolean isPhotoSelected = false;
  private Menu menu;

  private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

      Picasso.with(getActivity())
              .load(ItemType.DRAWABLES[position])
              // .centerInside()
              .transform(new CircleTransform())
              .into(imageItem);
      textType.setText(((ItemCarouselAdapter) horizontalPicker.getAdapter()).getItem(position));
      if (isPhotoSelected) {
        removeSelectedImage();
      }
    }
  };

  private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      // CoverFlow stopped to position
      Picasso.with(getActivity())
              .load(ItemType.DRAWABLES[position])
              // .centerInside()
              .transform(new CircleTransform())
              .into(imageItem);
      textType.setText(((ItemCarouselAdapter) horizontalPicker.getAdapter()).getItem(position));
      if (isPhotoSelected) {
        removeSelectedImage();
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
  };

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment AddItemFragment.
   */
  public static AddItemFallbackFragment getInstance(int bottomMargin) {

    if (instance == null) {
      synchronized (AddItemFallbackFragment.class) {
        if (instance == null) {
          instance = new AddItemFallbackFragment();
          instance.bottomMargin = bottomMargin;
        }
      }
    }
    return instance;
  }

  public AddItemFallbackFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
      bottomMargin = savedInstanceState.getInt(KEY_CONTENT);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    containerView = inflater.inflate(R.layout.fragment_add_item_fallback, container, false);

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

    isPhotoSelected = false;
    ItemCarouselAdapter adapter = new ItemCarouselAdapter(application);
    horizontalPicker.setAdapter(adapter);
    horizontalPicker.setReflectionEnabled(false);

    horizontalPicker.setOnItemClickListener(onItemClickListener);
    horizontalPicker.setOnItemSelectedListener(onItemSelectedListener);

    horizontalPicker.setSelection(0);
    autoCompleteTextView.setAdapter(
            new IngredientsAutocompleteAdapter(getActivity(),
                    android.R.layout.simple_dropdown_item_1line));
  }

  @Override
  public void onDestroyView() {

    super.onDestroyView();
    ButterKnife.reset(this);
  }

  @Override
  public void onResume() {

    ((MainActivity) getActivity()).showContextMenu(true);
    ((MainActivity) getActivity()).setActionBarIcon(R.drawable.ic_arrow_back);
    ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.add_item).toLowerCase(Locale.ENGLISH));
    // clear the text if any
    autoCompleteTextView.setText("");
    super.onResume();
  }

  @Override
  public void onAttach(Activity activity) {

    super.onAttach(activity);
    application = ((FridgeApplication) activity.getApplication());
    // ((MainActivity) getActivity()).setOnFeedRefreshCallback(this);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    }
    catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
              + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // cancel handling if the request failed
    if (resultCode != Activity.RESULT_OK) {
      return;
    }

    if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
      if (data.getData() != null) {
        // create the image file either from the uri or the path
        try {
          mFileTemp = FileUtils.getFileFromUri(data.getData(), getActivity());
        }
        catch (NullPointerException e) {
          mFileTemp = new File(data.getData().getPath());
        }
        if (mFileTemp != null) {

          Log.d(Log.TAG, "mFileTemp: " + mFileTemp.getAbsolutePath());
          Log.d(Log.TAG, "data.getDataString(): " + data.getDataString());
          Log.d(Log.TAG, "data.getData: " + data.getData().getPath());
          Log.d(Log.TAG, "data.getEncodedData: " + data.getData().getEncodedPath());

          // set the image to the circle view center
          try {
            if (loadTarget == null) {
              loadTarget = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                  imageItem.setImageBitmap(bitmap);
                  textType.setText(application.getString(R.string.type_camera));
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                  imageItem.setImageDrawable(errorDrawable);
                  textType.setText(application.getString(R.string.type_error));
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
              };
            }

            Picasso.with(getActivity())
                    .load(mFileTemp)
                    .resize(imageItem.getWidth(), imageItem.getHeight())
                    .centerInside()
                    .transform(new CircleTransform())
                    .error(R.mipmap.ic_launcher)
                    .into(loadTarget);
            // circularView.getCenterCircle().setSrc(FileUtils.getCroppedBitmapFromFile(circularView, mFileTemp));
          }
          catch (Exception e) {
            e.printStackTrace();
          }
          // change the state of the FAB
          menu.findItem(R.id.action_add).setIcon(R.drawable.ic_clear);
          // buttonCamera.setImageResource(R.drawable.ic_action_content_clear_white);

          isPhotoSelected = true;
          horizontalPicker.setEnabled(false);
        }
      }
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
  }

  @OnClick(R.id.image_clear)
  public void clearText(View view) {

    autoCompleteTextView.setText("");
  }

  @OnClick(R.id.button_create)
  public void createItem(View view) {

    if (textType.getText().toString().equals(getString(R.string.type_error))
            || TextUtils.isEmpty(textType.getText())) {
      // there was an error, show it
      SnackBar snackBar = new SnackBar(getActivity(), "Please select another category or image.");
      snackBar.show();
    }
    else {
      // save the item
      FridgeItem fridgeItem = new FridgeItem();
      String name = autoCompleteTextView.getText().toString().trim();
      String type = textType.getText().toString().trim();
      fridgeItem.setName(name.length() > 0 ? name : type);
      fridgeItem.setType(type.equals(getString(R.string.type_camera)) ?
              mFileTemp.getAbsolutePath() : String.valueOf(ItemType.valueOf(type).ordinal()));
      fridgeItem.setItemId(fridgeItem.hashCode());
      fridgeItem.setEditTimestamp(Calendar.getInstance().getTimeInMillis());
      fridgeItem.save();

      // remove instance to saved file
      mFileTemp = null;

      // add the saved item to history
      HistoryItem historyItem = new HistoryItem(fridgeItem,
              Calendar.getInstance().getTimeInMillis() / 1000, ChangeType.ADD);
      historyItem.save();

      // hide the keyboard if shown
      KeyboardUtils.hideSoftKeyboard(autoCompleteTextView);

      // clear the text
      autoCompleteTextView.setText("");
      autoCompleteTextView.clearFocus();

      // restart the loader on DB change
      ((MainActivity) getActivity()).setDatabaseChanged(true);

      // set the screenshot
      takeScreenShot();
      ((MainActivity) getActivity()).setScreenshotable(this);

      Rect rect = new Rect();
      buttonCreate.getLocalVisibleRect(rect);

      Point buttonCenter = new Point((int) buttonCreate.getX() + buttonCreate.getWidth() / 2,
              (int) buttonCreate.getY() + buttonCreate.getHeight() / 2);
      mListener.onFragmentInteraction(false, buttonCenter, FridgeFragment.class.getCanonicalName());
    }
  }

  public void takePicture() {

    if (isPhotoSelected) {
      removeSelectedImage();
    }
    else {
      Intent choosePhotoIntent = new Intent(getActivity(), GalleryActivity.class);
      startActivityForResult(choosePhotoIntent, Constants.REQUEST_IMAGE_CAPTURE);
      getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
  }

  public void removeSelectedImage() {

    isPhotoSelected = false;
    horizontalPicker.setEnabled(true);
    if (mFileTemp != null && mFileTemp.exists()) {
      FileUtils.deleteFile(mFileTemp);
    }
    mFileTemp = null;
    menu.findItem(R.id.action_add).setIcon(R.drawable.ic_photo_camera);

    int position = horizontalPicker.getSelectedItemPosition() != -1 ? horizontalPicker.getSelectedItemPosition() : 0;

    Picasso.with(getActivity())
            .load(ItemType.DRAWABLES[position])
            .centerInside()
            .transform(new CircleTransform())
            .into(imageItem);
    // imageItem.setImageResource(ItemType.DRAWABLES[position]);

    textType.setText(((ItemCarouselAdapter) horizontalPicker.getAdapter()).getItem(position));
    horizontalPicker.setSelection(position);
  }

  @Override
  public void takeScreenShot() {

    Thread thread = new Thread() {

      @Override
      public void run() {

        Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                containerView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        containerView.draw(canvas);
        AddItemFallbackFragment.this.bitmap = bitmap;
      }
    };
    thread.start();
  }

  @Override
  public Bitmap getBitmap() {

    return bitmap;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    inflater.inflate(R.menu.menu_add, menu);
    this.menu = menu;
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == android.R.id.home) {
      // Toast.makeText(getActivity(), "Back", Toast.LENGTH_SHORT).show();
      Log.d(Log.TAG, "Back");
    }
    else if (id == R.id.action_add) {
      takePicture();
    }

    return super.onOptionsItemSelected(item);
  }
}
