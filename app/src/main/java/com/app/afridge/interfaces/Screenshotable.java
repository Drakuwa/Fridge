package com.app.afridge.interfaces;

import android.graphics.Bitmap;


/**
 * Fragment screenshot
 * <p/>
 * Created by Konstantin on 12.01.2015.
 */
public interface Screenshotable {

  public void takeScreenShot();

  public Bitmap getBitmap();
}
