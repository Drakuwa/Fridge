package com.app.afridge.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import com.app.afridge.adapters.model.GalleryItem;

import java.util.ArrayList;
import java.util.List;


/**
 * Gallery Loader
 * <p/>
 * Created by drakuwa on 29.12.14.
 */
public class GalleryLoader extends AsyncTaskLoader<List<GalleryItem>> {

  private final Uri mediaImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  private final String[] imageProjection = new String[] {
          MediaStore.Images.Media._ID,
          MediaStore.Images.Media.BUCKET_ID,
          MediaStore.Images.Media.BUCKET_DISPLAY_NAME
  };

  private Context context;
  private List<GalleryItem> adapterList;

  public GalleryLoader(Context context) {

    super(context);
    this.context = context;
  }

  @Override
  protected void onStartLoading() {
    // use cached results
    if (adapterList != null) {
      deliverResult(adapterList);
    }
    // reload data
    else {
      forceLoad();
    }
  }

  @Override
  protected void onStopLoading() {
    // cancel the ongoing load if any
    cancelLoad();
  }

  @Override
  public List<GalleryItem> loadInBackground() {

    // list all images from the media provider
    // sort them by recent modification
    Cursor data = context.getContentResolver().query(
            mediaImagesUri,
            imageProjection,
            null,
            null,
            MediaStore.Images.Media.DATE_MODIFIED + " DESC"
    );
    // convert the cursor into a list
    adapterList = cursorToListAdapter(data);
    return adapterList;
  }

  @Override
  public void deliverResult(List<GalleryItem> data) {
    // cache data
    adapterList = data;
    super.deliverResult(data);
  }

  @Override
  protected void onReset() {

    super.onReset();
    // stop loader if running
    onStopLoading();
    // invalidate cache
    adapterList = null;
  }

  @Override
  public void onCanceled(List<GalleryItem> data) {
    // try to cancel the current load and invalidate cache
    super.onCanceled(data);
    adapterList = null;
  }

  private List<GalleryItem> cursorToListAdapter(Cursor data) {

    List<GalleryItem> resultList = new ArrayList<>();

    while (data.moveToNext()) {
      GalleryItem galleryItem = new GalleryItem();
      galleryItem.setId(data.getLong(data.getColumnIndex(MediaStore.Images.Media._ID)));
      galleryItem.setBucketId(data.getString(data.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)));
      galleryItem.setBucketName(data.getString(data.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
      galleryItem.setImageUri(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.toString(galleryItem.getId())));
      resultList.add(galleryItem);
    }
    return resultList;
  }
}
