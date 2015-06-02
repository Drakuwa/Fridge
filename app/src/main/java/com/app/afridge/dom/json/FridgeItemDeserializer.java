package com.app.afridge.dom.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.app.afridge.dom.FridgeItem;

import java.lang.reflect.Type;

/**
 * JSON de-serializable type adapter for {@link FridgeItem}
 * Created by drakuwa on 5/27/15.
 */
public class FridgeItemDeserializer implements JsonDeserializer<FridgeItem> {

    @Override
    public FridgeItem deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        JsonObject jObject = json.getAsJsonObject();
        FridgeItem item = new FridgeItem();
        item.setItemId(jObject.get("item_id").getAsInt());
        item.setName(jObject.get("name").getAsString());
        item.setType(jObject.get("type").getAsString());
        if (jObject.has("quantity")) {
            item.setQuantity(jObject.get("quantity").getAsString());
        }
        if (jObject.has("type_of_quantity")) {
            item.setTypeOfQuantity(jObject.get("type_of_quantity").getAsInt());
        }
        if (jObject.has("details")) {
            item.setDetails(jObject.get("details").getAsString());
        }
        if (jObject.has("expiration_date")) {
            item.setExpirationDate(jObject.get("expiration_date").getAsLong());
        }
        if (jObject.has("edit_timestamp")) {
            item.setEditTimestamp(jObject.get("edit_timestamp").getAsLong());
        }
        item.setRemoved(jObject.get("status").getAsBoolean());
        return item;
    }
}
