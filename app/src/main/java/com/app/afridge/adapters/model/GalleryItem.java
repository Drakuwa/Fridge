package com.app.afridge.adapters.model;

import android.net.Uri;


/**
 * Custom object for the Gallery implementation
 * <p/>
 * Created by drakuwa on 29.12.2014.
 */
public class GalleryItem {

  private long id;
  private String bucketId;
  private String bucketName;
  private Uri imageUri;

  public long getId() {

    return id;
  }

  public void setId(long id) {

    this.id = id;
  }

  public String getBucketId() {

    return bucketId;
  }

  public void setBucketId(String bucketId) {

    this.bucketId = bucketId;
  }

  public String getBucketName() {

    return bucketName;
  }

  public void setBucketName(String bucketName) {

    this.bucketName = bucketName;
  }

  public Uri getImageUri() {

    return imageUri;
  }

  public void setImageUri(Uri imageUri) {

    this.imageUri = imageUri;
  }
}
