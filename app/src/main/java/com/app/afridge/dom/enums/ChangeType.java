package com.app.afridge.dom.enums;

/**
 * Change type for the history item
 * <p/>
 * Created by drakuwa on 2/6/15.
 */
public enum ChangeType {
  ADD,
  MODIFY,
  DELETE;

  public int getItemOrdinal(ItemType item) {

    return item.getItemOrdinal(item);
  }
}
