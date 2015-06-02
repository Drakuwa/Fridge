package com.app.afridge.dom;

import com.app.afridge.dom.enums.SyncState;

/**
 * Sync event for the EventBus
 *
 * Created by drakuwa on 5/28/15.
 */
public class SyncEvent {

    public final SyncState message;

    public SyncEvent(SyncState message) {
        this.message = message;
    }
}
