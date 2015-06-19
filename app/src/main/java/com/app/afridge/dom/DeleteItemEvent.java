package com.app.afridge.dom;

/**
 * Delete fridge item event for the EventBus
 *
 * Created by drakuwa on 5/28/15.
 */
public class DeleteItemEvent {

    public final String message;

    public DeleteItemEvent(String message) {
        this.message = message;
    }
}
