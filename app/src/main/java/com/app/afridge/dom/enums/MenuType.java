package com.app.afridge.dom.enums;

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
  ABOUT,
  FEEDBACK;

  public int getItemOrdinal(ItemType item) {

    return item.getItemOrdinal(item);
  }
}
