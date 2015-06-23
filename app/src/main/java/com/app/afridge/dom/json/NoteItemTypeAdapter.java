package com.app.afridge.dom.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.app.afridge.dom.NoteItem;

import java.lang.reflect.Type;

/**
 * JSON serializable type adapter for {@link com.app.afridge.dom.NoteItem}
 * Created by drakuwa on 5/27/15.
 */
public class NoteItemTypeAdapter implements JsonSerializer<NoteItem> {

    @Override
    public JsonElement serialize(NoteItem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item_id", src.getItemId());
        jsonObject.addProperty("note", src.getNote());
        jsonObject.addProperty("timestamp", src.getTimestamp());
        jsonObject.addProperty("is_checked", src.isChecked());
        jsonObject.addProperty("status", src.isRemoved());
        return jsonObject;
    }
}
