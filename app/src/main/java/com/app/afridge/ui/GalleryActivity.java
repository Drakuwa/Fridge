package com.app.afridge.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.app.afridge.R;
import com.app.afridge.adapters.GalleryAdapter;
import com.app.afridge.adapters.model.GalleryItem;
import com.app.afridge.loaders.GalleryLoader;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.FileUtils;
import com.app.afridge.utils.Log;
import com.app.afridge.views.AdvancedTextView;
import com.etsy.android.grid.StaggeredGridView;
import com.gc.materialdesign.widgets.SnackBar;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Gallery chooser activity
 * <p/>
 * Created by drakuwa on 29.12.2014.
 */
public class GalleryActivity extends AbstractActivity implements LoaderManager.LoaderCallbacks<List<GalleryItem>> {

  @InjectView(R.id.grid)
  StaggeredGridView galleryGrid;
  @InjectView(R.id.toolbar_actionbar)
  Toolbar toolbar;
  @InjectView(R.id.spinner_toolbar)
  Spinner spinnerNavigation;

  private List<String> navigationList;
  private File mFileTemp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gallery);

    // getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    ButterKnife.inject(this);
    getSupportLoaderManager().initLoader(0, null, this);

    // setup the new Lollipop Toolbar
    toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(null);
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
      ((AdvancedTextView) toolbar.findViewById(R.id.toolbar_title)).setText(getString(R.string.choose_image));
    }

    // status bar height margin hack
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(R.id.height_hack).getLayoutParams();
    params.height = statusBarHeight;
    findViewById(R.id.height_hack).setLayoutParams(params);
    // findViewById(R.id.container).setPadding(0, statusBarHeight, 0, bottomMargin);
  }

  @Override
  public Loader<List<GalleryItem>> onCreateLoader(int id, Bundle args) {

    return new GalleryLoader(this);
  }

  @Override
  public void onLoadFinished(Loader<List<GalleryItem>> loader, List<GalleryItem> data) {

    // make the navigation set to switch between galleries
    Set<String> navigationSet = new LinkedHashSet<>();
    navigationSet.add(getString(R.string.hint_all));
    for (GalleryItem item : data) {
      navigationSet.add(item.getBucketName());
    }

    navigationList = new ArrayList<>();
    navigationList.addAll(navigationSet);
    SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner_simple, navigationList);
    spinnerNavigation.setAdapter(spinnerAdapter);
    // getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);

    spinnerNavigation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (navigationList != null) {
          String filter = navigationList.get(position);
          ((GalleryAdapter) galleryGrid.getAdapter()).getFilter().filter(filter);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
    // make the adapter
    GalleryAdapter adapter = new GalleryAdapter(this, data, false);

    galleryGrid.setAdapter(adapter);
    galleryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (position) {
          case 0:
            // take photo
            startCamera();
            break;
          case 1:
            // choose photo
            chooseMediaFile();
            break;
          default:
            // select photo
            Uri fileUri = ((GalleryItem) parent.getAdapter().getItem(position)).getImageUri();
            String filePath = FileUtils.getPath(GalleryActivity.this, fileUri);
            Log.d(Log.TAG, "fileUri: path: " + fileUri.getPath());
            Log.d(Log.TAG, "filePath: " + filePath);
            finishWithResult(Uri.parse(filePath));
            break;
        }
      }
    });

  }

  @Override
  public void onLoaderReset(Loader<List<GalleryItem>> loader) {

    if (galleryGrid != null) {
      galleryGrid.setAdapter(null);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    // cancel handling if the request failed
    if (resultCode != Activity.RESULT_OK) {
      return;
    }

    switch (requestCode) {
      case Constants.REQUEST_IMAGE_CAPTURE:
        finishWithResult(Uri.fromFile(mFileTemp));
        break;
      case Constants.REQUEST_IMAGE_CHOOSE:
        String filePath = null;
        try {
          filePath = FileUtils.getPath(this, data.getData());
        }
        catch (NullPointerException e) {
          // no file selected, ignore exception
        }
        if (filePath == null) {
          SnackBar snackBar = new SnackBar(this, getString(R.string.error_remote_file));
          snackBar.show();
          // Toast.makeText(this, getString(R.string.error_remote_file), Toast.LENGTH_SHORT).show();
        }
        else {
          finishWithResult(Uri.parse(filePath));
        }
        break;
    }
  }

  @Override
  public void onBackPressed() {

    super.onBackPressed();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }

  private void finishWithResult(Uri fileUri) {

    Intent result = new Intent();
    result.setData(fileUri);
    setResult(RESULT_OK, result);
    finish();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }

  @SuppressLint("InlinedApi")
  private void chooseMediaFile() {

    // TODO implement document picker

    String intentType, chooserTitle;
    Uri actionPick;

    int localRequestType;
    intentType = "image/*";
    chooserTitle = getString(R.string.hint_choose_photo);
    localRequestType = Constants.REQUEST_IMAGE_CHOOSE;
    actionPick = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    Intent chooseMediaIntent;
    if (Common.versionAtLeast(Build.VERSION_CODES.KITKAT)) {
      // use the ACTION_OPEN_DOCUMENT
      chooseMediaIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      // Filter to only show results that can be "opened", such as a
      // file (as opposed to a list of contacts or timezones)
      chooseMediaIntent.addCategory(Intent.CATEGORY_OPENABLE);
      // Filter to show only images, using the image MIME data type.
      // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
      // To search for all documents available via installed storage providers,
      // it would be "*/*".
      chooseMediaIntent.setType(intentType);
    }
    else {
      // use the picker dialog
      chooseMediaIntent = new Intent(Intent.ACTION_PICK, actionPick);
    }

    if (chooseMediaIntent.resolveActivity(getPackageManager()) != null) {
      if (Build.VERSION.SDK_INT < 19) {
        startActivityForResult(Intent.createChooser(chooseMediaIntent, chooserTitle), localRequestType);
      }
      else {
        startActivityForResult(chooseMediaIntent, localRequestType);
      }
    }
    else {
      Log.d(Log.TAG, "No gallery available");
    }
  }

  private void startCamera() {

    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    // Ensure that there's a camera activity to handle the intent
    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
      // Create the File where the photo should go
      String filePath = FileUtils.getMediaFile("image/jpg").toString();
      // if there already is an image, delete the old one
      if (null != mFileTemp && mFileTemp.exists()) {
        mFileTemp.delete();
      }
      mFileTemp = new File(filePath);

      // Continue only if the File was successfully created
      if (mFileTemp != null) {
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFileTemp));
        startActivityForResult(cameraIntent, Constants.REQUEST_IMAGE_CAPTURE);
      }
    }
    else {
      // TODO no camera available
      Log.d(Log.TAG, "No camera available");
    }
  }
}
