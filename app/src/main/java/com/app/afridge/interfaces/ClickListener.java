package com.app.afridge.interfaces;

import android.view.View;


/**
 * Interface for handling clicks - both normal and long ones.
 * <p/>
 * Created by drakuwa on 2/12/15.
 */
public interface ClickListener {

  /**
   * Called when the view is clicked.
   *
   * @param v           view that is clicked
   * @param position    of the clicked item
   * @param isLongClick true if long click, false otherwise
   */
  void onClick(View v, int position, boolean isLongClick);
}