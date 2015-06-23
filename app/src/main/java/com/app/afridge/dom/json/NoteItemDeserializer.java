package com.app.afridge.dom.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.app.afridge.dom.NoteItem;

import java.lang.reflect.Type;

/**
 * JSON de-serializable type adapter for {@link NoteItem}
 * Created by drakuwa on 5/27/15.
 */
public class NoteItemDeserializer implements JsonDeserializer<NoteItem> {

    @Override
    public NoteItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jObject = json.getAsJsonObject();
        NoteItem noteItem = new NoteItem();
        noteItem.setItemId(jObject.get("item_id").getAsInt());
        noteItem.setNote(jObject.get("note").getAsString());
        noteItem.setChecked(jObject.get("is_checked").getAsBoolean());
        noteItem.setTimestamp(jObject.get("timestamp").getAsLong());
        if (jObject.has("status")) {
            noteItem.setRemoved(jObject.get("status").getAsBoolean());
        }
        return noteItem;
    }
}
