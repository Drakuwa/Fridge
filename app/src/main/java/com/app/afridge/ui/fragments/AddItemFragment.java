package com.app.afridge.ui.fragments;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.adapters.CircularViewAdapter;
import com.app.afridge.adapters.IngredientsAutocompleteAdapter;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.dom.enums.ChangeType;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.interfaces.OnFragmentInteractionListener;
import com.app.afridge.interfaces.Screenshotable;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.utils.FileUtils;
import com.app.afridge.utils.KeyboardUtils;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;
import com.app.afridge.views.AdvancedTextView;
import com.gc.materialdesign.widgets.SnackBar;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.melnykov.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.sababado.circularview.CircularView;
import com.sababado.circularview.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * add new fridge item - A simple {@link android.support.v4.app.Fragment} subclass.
 * <p/>
 * Created by drakuwa on 1/29/15.
 */
public class AddItemFragment extends Fragment implements Screenshotable, ImageChooserListener {

    private static final String KEY_CONTENT = "AddItemFragment:Content";

    private static String PACKAGE_NAME = "MMSDemo";

    // Singleton
    private static volatile AddItemFragment instance = null;

    @InjectView(R.id.circular_view)
    CircularView circularView;

    @InjectView(R.id.edit_ingredient)
    MaterialAutoCompleteTextView autoCompleteTextView;

    @InjectView(R.id.text_item_type)
    AdvancedTextView textType;

    @InjectView(R.id.button_create)
    FloatingActionButton buttonCreate;

    private int bottomMargin = 0;

    private FridgeApplication application;

    private Bitmap bitmap;

    private View containerView;

    // private File mFileTemp = null;

    private CircularViewAdapter adapter;

    private OnFragmentInteractionListener mListener;

    private boolean isPhotoSelected = false;

    private Menu menu;

    private ImageChooserManager imageChooserManager;

    private String filePath;

    private int chooserType;

    private ProgressDialog dialog;

    public AddItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddItemFragment.
     */
    public static AddItemFragment getInstance(int bottomMargin) {

        if (instance == null) {
            synchronized (AddItemFragment.class) {
                if (instance == null) {
                    instance = new AddItemFragment();
                    instance.bottomMargin = bottomMargin;
                }
            }
        }
        return instance;
    }

    /**
     * @return The selected quadrant.
     */
    private static int getQuadrant(double x, double y) {

        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // set the package name for naming the folder
        PACKAGE_NAME = getActivity().getApplicationContext().getPackageName();

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            bottomMargin = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        containerView = inflater.inflate(R.layout.fragment_add_item, container, false);

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        isPhotoSelected = false;
        adapter = new CircularViewAdapter(application);
        circularView.setAdapter(adapter);

        // Allow markers to continuously animate on their own when the highlight animation isn't running.
        // The flag can also be set in XML
        circularView.setAnimateMarkerOnStillHighlight(true);
        // Combine the above line with the following so that the marker at it's position will animate at the start.
        // The highlighted Degree can also be defined in XML
        circularView.setHighlightedDegree(CircularView.RIGHT);

        circularView.setOnHighlightAnimationEndListener(
                new CircularView.OnHighlightAnimationEndListener() {

                    @Override
                    public void onHighlightAnimationEnd(CircularView view, Marker marker,
                            int position) {

                        circularView.getCenterCircle().setSrc(marker.getDrawable());
                        // startAngle = currentAngle;
                        textType.setText(adapter.getMarkerName(marker.getId()));

                        SnackBar snackBar = new SnackBar(getActivity(),
                                "Spin ends on " + adapter.getMarkerName(marker.getId()));
                        snackBar.show();
                        // marker.setVisibility(marker.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        circularView.setTextColor(Color.BLUE);
                    }
                });

        circularView.setOnTouchListener(new MyOnTouchListener());
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // setup initial state
                Marker marker = circularView.getHighlightedMarker();
                if (marker != null) {
                    circularView.getCenterCircle().setSrc(marker.getDrawable());
                    textType.setText(adapter.getMarkerName(marker.getId()));
                }
            }
        }, 200);

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
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.add_item).toLowerCase(Locale.ENGLISH));
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
        } catch (ClassCastException e) {
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

        if (requestCode == ChooserType.REQUEST_PICK_PICTURE
                || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
            showProgressDialog();
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        }
//        else if (requestCode == CROP_PHOTO) {
//            hideProgressDialog();
//            String path = data.getStringExtra(CropImage.IMAGE_PATH);
//            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
//                    .beginTransaction();
//            DialogFragment fragment = EditImageFragment.newInstance(path);
//            fragment.setTargetFragment(this, FILTER_PHOTO);
//            fragment.show(fragmentTransaction, "filters");
//        }

//        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
//            if (data.getData() != null) {
//                // create the image file either from the uri or the path
//                try {
//                    mFileTemp = FileUtils.getFileFromUri(data.getData(), getActivity());
//                } catch (NullPointerException e) {
//                    mFileTemp = new File(data.getData().getPath());
//                }
//                if (mFileTemp != null) {
//
//                    Log.d(Log.TAG, "mFileTemp: " + mFileTemp.getAbsolutePath());
//                    Log.d(Log.TAG, "data.getDataString(): " + data.getDataString());
//                    Log.d(Log.TAG, "data.getData: " + data.getData().getPath());
//                    Log.d(Log.TAG, "data.getEncodedData: " + data.getData().getEncodedPath());
//
//                    // set the image to the circle view center
//                    try {
//                        if (loadTarget == null) {
//                            loadTarget = new Target() {
//
//                                @Override
//                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//
//                                    circularView.getCenterCircle().setSrc(bitmap);
//                                    textType.setText(application.getString(R.string.type_camera));
//                                }
//
//                                @Override
//                                public void onBitmapFailed(Drawable errorDrawable) {
//
//                                    circularView.getCenterCircle().setSrc(errorDrawable);
//                                    textType.setText(application.getString(R.string.type_error));
//                                }
//
//                                @Override
//                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                                }
//                            };
//                        }
//
//                        Picasso.with(getActivity())
//                                .load(mFileTemp)
//                                .resize(circularView.getWidth(), circularView.getHeight())
//                                .centerInside()
//                                .transform(new CircleTransform())
//                                .error(R.mipmap.ic_launcher)
//                                .into(loadTarget);
//                        // circularView.getCenterCircle().setSrc(FileUtils.getCroppedBitmapFromFile(circularView, mFileTemp));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    // change the state of the FAB
//                    menu.findItem(R.id.action_add).setIcon(R.drawable.ic_clear);
//                    // buttonCamera.setImageResource(R.drawable.ic_action_content_clear_white);
//
//                    isPhotoSelected = true;
//                    circularView.setEnabled(false);
//                }
//            }
//        }
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
            SnackBar snackBar = new SnackBar(getActivity(),
                    "Please select another category or image.");
            snackBar.show();
        } else {
            // save the item
            FridgeItem fridgeItem = new FridgeItem();
            String name = autoCompleteTextView.getText().toString().trim();
            String type = textType.getText().toString().trim();
            fridgeItem.setName(name.length() > 0 ? name : type);
            fridgeItem.setType(type.equals(getString(R.string.type_camera)) ?
                    filePath : String.valueOf(ItemType.valueOf(type).ordinal()));
            fridgeItem.setItemId(fridgeItem.hashCode());
            fridgeItem.setEditTimestamp(Calendar.getInstance().getTimeInMillis());
            fridgeItem.save();

            // remove instance to saved file
            filePath = null;

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
            mListener.onFragmentInteraction(false, buttonCenter,
                    FridgeFragment.class.getCanonicalName());
        }
    }

    @OnClick(R.id.random)
    public void randomSpin() {
        // increment the random counter
        int randomSpinCount = application.prefStore.getInt(SharedPrefStore.Pref.STAT_RANDOM_SPIN);
        application.prefStore.setInt(SharedPrefStore.Pref.STAT_RANDOM_SPIN, ++randomSpinCount);
        // Start animation from the bottom of the circle, going clockwise.
        final float start = CircularView.BOTTOM;
        final float end = start + 360f + (float) (Math.random() * 720f);
        // animate the highlighted degree value but also make sure it isn't so fast that it's skipping marker animations.
        final long duration = (long) (Marker.ANIMATION_DURATION * 2L * end / (270L - adapter
                .getCount()));
        circularView.animateHighlightedDegree(start, end, duration);
    }

    public void getPicture() {

        if (isPhotoSelected) {
            isPhotoSelected = false;
            circularView.setEnabled(true);
            FileUtils.deleteFile(new File(filePath));
            filePath = null;
            menu.findItem(R.id.action_add).setIcon(R.drawable.ic_photo_camera);
            // ((FloatingActionButton) view).setImageResource(R.drawable.ic_photo_camera);

            circularView.setHighlightedDegree(CircularView.RIGHT);
            Marker marker = circularView.getHighlightedMarker();
            circularView.getCenterCircle().setSrc(marker.getDrawable());
            textType.setText(adapter.getMarkerName(marker.getId()));
        } else {
            // Show image chooser dialog
            String[] options = {application.getString(R.string.take_photo),
                    application.getString(R.string.choose_image)};
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
            builder.setTitle(R.string.get_picture)
                    .setItems(options, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            switch (which) {
                                case 0:
                                    takePhoto();
                                    break;
                                case 1:
                                    chooseImage();
                                    break;
                            }
                        }
                    });
            builder.create().show();
        }
    }

    /**
     * @return The angle of the unit circle with the image view's center
     */
    private double getAngle(double xTouch, double yTouch) {

        double x = xTouch - (circularView.getWidth() / 2d);
        double y = circularView.getHeight() - yTouch - (circularView.getHeight() / 2d);

        y = -y;
        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
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
                AddItemFragment.this.bitmap = bitmap;
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
        } else if (id == R.id.action_add) {
            getPicture();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Choose an image from the file system
     */
    public void chooseImage() {

        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, PACKAGE_NAME, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            filePath = imageChooserManager.choose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Take a picture with a camera application
     */
    public void takePhoto() {

        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, PACKAGE_NAME, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            filePath = imageChooserManager.choose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call the crop intent when the user chooses an image
     *
     * @param image image that the user has chosen
     */
    @Override
    public void onImageChosen(final ChosenImage image) {

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (image != null) {
                    // set the image to the circle view center
                    try {
                        Log.d("filePath: " + filePath);
                        filePath = image.getFilePathOriginal();
                        Log.d("filePath image.getFilePathOriginal(): " + filePath);
                        final Target loadTarget = new Target() {

                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                circularView.getCenterCircle().setSrc(bitmap);
                                textType.setText(application.getString(R.string.type_camera));
                                hideProgressDialog();
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                                circularView.getCenterCircle().setSrc(errorDrawable);
                                textType.setText(application.getString(R.string.type_error));
                                hideProgressDialog();
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        };

                        // set the image tag
                        circularView.setTag(loadTarget);
                        Picasso.with(getActivity())
                                .load(new File(filePath))
                                .resize(circularView.getWidth(), circularView.getHeight())
                                .centerInside()
                                .transform(new CircleTransform())
                                .error(R.mipmap.ic_launcher)
                                .into(loadTarget);
                        // circularView.getCenterCircle().setSrc(FileUtils.getCroppedBitmapFromFile(circularView, mFileTemp));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // change the state of the FAB
                    menu.findItem(R.id.action_add).setIcon(R.drawable.ic_clear);
                    // buttonCamera.setImageResource(R.drawable.ic_action_content_clear_white);

                    isPhotoSelected = true;
                    circularView.setEnabled(false);
                }
            }
        });
    }

    /**
     * Show the error reason when the image chooser fails
     *
     * @param reason reason for failing
     */
    @Override
    public void onError(final String reason) {

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                hideProgressDialog();
                Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Re-initialize the {@link ImageChooserManager}.
     * <p>Should be called if for some reason the Image Chooser Manager is null
     * (Due to destroying of activity for low memory situations)</p>
     */
    private void reinitializeImageChooser() {

        imageChooserManager = new ImageChooserManager(this, chooserType, PACKAGE_NAME, true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    /**
     * Create and show a progress dialog
     * <p>Create an indeterminate {@link ProgressDialog} and show it</p>
     */
    private void showProgressDialog() {
        // create the indeterminate progress dialog
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.loading));
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        try {
            dialog.show();
        } catch (Exception e) {
            Log.e(Log.TAG, "Exception=" + e.getLocalizedMessage());
        }
    }

    /**
     * Hide the progress dialog
     * <p>hide {@link AddItemFragment#dialog} if it is initialized
     * and showing</p>
     */
    private void hideProgressDialog() {
        // Remove the progress dialog.
        try {
            dialog.dismiss();
            dialog = null;
        } catch (Exception e) {
            Log.e(Log.TAG, "Exception=" + e.getLocalizedMessage());
        }
    }

    /**
     * Simple implementation of an {@link android.view.View.OnTouchListener} for registering the
     * dialer's touch events.
     */
    private class MyOnTouchListener implements View.OnTouchListener {

        // private double startAngle;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    // startAngle = getAngle(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY());
                    circularView.setHighlightedDegree((float) currentAngle);
                    Marker marker = circularView.getHighlightedMarker();
                    circularView.getCenterCircle().setSrc(marker.getDrawable());
                    // startAngle = currentAngle;
                    textType.setText(adapter.getMarkerName(marker.getId()));
                    break;

                case MotionEvent.ACTION_UP:
                    marker = circularView.getHighlightedMarker();
                    Log.d(Log.TAG, "selected: " + marker.getId());
                    // Toast.makeText(getActivity(), "selected: " + marker.getId(), Toast.LENGTH_SHORT).show();
                    break;
            }

            return true;
        }

    }
}
