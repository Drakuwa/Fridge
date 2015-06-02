package com.app.afridge.dom.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.app.afridge.dom.FridgeItem;

import java.lang.reflect.Type;

/**
 * JSON serializable type adapter for {@link FridgeItem}
 * Created by drakuwa on 5/27/15.
 */
public class FridgeItemTypeAdapter implements JsonSerializer<FridgeItem> {

    @Override
    public JsonElement serialize(FridgeItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item_id", src.getItemId());
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("type", src.getType());
        jsonObject.addProperty("quantity", src.getQuantity());
        jsonObject.addProperty("type_of_quantity", src.getTypeOfQuantity());
        jsonObject.addProperty("details", src.getDetails());
        jsonObject.addProperty("expiration_date", src.getExpirationDate());
        jsonObject.addProperty("edit_timestamp", src.getEditTimestamp());
        jsonObject.addProperty("status", src.isRemoved());
        return jsonObject;
    }
}
