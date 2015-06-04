package com.app.afridge.dom;

/**
 * Sync event for the EventBus
 *
 * Created by drakuwa on 5/28/15.
 */
public class IngredientsEvent {

    public final String message;

    public IngredientsEvent(String message) {
        this.message = message;
    }
}
