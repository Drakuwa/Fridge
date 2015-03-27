package com.app.afridge.dom;

/**
 * Change type for the history item
 * <p/>
 * Created by drakuwa on 2/6/15.
 */
public enum MenuType {
  CLOSE,
  NOTES,
  HISTORY,
  PROFILE,
  SETTINGS,
  FEEDBACK;

  public int getItemOrdinal(ItemType item) {

    return item.getItemOrdinal(item);
  }
}
