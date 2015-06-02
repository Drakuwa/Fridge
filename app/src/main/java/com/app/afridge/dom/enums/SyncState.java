package com.app.afridge.dom.enums;

/**
 * Change type for the history item
 * <p/>
 * Created by drakuwa on 5/31/15.
 */
public enum SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    FAILED;

    public int getItemOrdinal(ItemType item) {

        return item.getItemOrdinal(item);
    }
}
