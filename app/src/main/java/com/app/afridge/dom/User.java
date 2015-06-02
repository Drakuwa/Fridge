package com.app.afridge.dom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.activeandroid.query.Select;
import com.app.afridge.dom.json.NoteItemTypeAdapter;
import com.cloudant.sync.datastore.BasicDocumentRevision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User POJO
 * <p/>
 * Created by drakuwa on 3/5/15.
 */
@SuppressWarnings("UnusedDeclaration")
public class User {

  private String id;
  private String fullName;
  private String imageUrl;
  private String email;

  private String fridgeItemsJson;
  private String noteItemsJson;

  public String getFullName() {

    return fullName;
  }

  public void setFullName(String fullName) {

    this.fullName = fullName;
  }

  public String getImageUrl() {

    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {

    this.imageUrl = imageUrl;
  }

  public String getId() {

    return id;
  }

  public void setId(String id) {

    this.id = id;
  }

  public String getEmail() {

    return email;
  }

  public void setEmail(String email) {

    this.email = email;
  }

  public String getFridgeItemsJson() {

    return fridgeItemsJson;
  }

  public void setFridgeItemsJson(String fridgeItemsJson) {

    this.fridgeItemsJson = fridgeItemsJson;
  }

  public String getNoteItemsJson() {

    return noteItemsJson;
  }

  public void setNoteItemsJson(String noteItemsJson) {

    this.noteItemsJson = noteItemsJson;
  }

  // this is the revision in the database representing this task
  private BasicDocumentRevision rev;

  public BasicDocumentRevision getDocumentRevision() {

    return rev;
  }

  public static User fromRevision(BasicDocumentRevision rev) {

    User user = new User();
    user.rev = rev;
    // this could also be done by a fancy object mapper
    Map<String, Object> map = rev.asMap();
    user.setId((String) map.get("id"));
    user.setFullName((String) map.get("fullName"));
    user.setImageUrl((String) map.get("imageUrl"));
    user.setEmail((String) map.get("email"));
    user.setFridgeItemsJson((String) map.get("items"));
    user.setNoteItemsJson((String) map.get("notes"));
    return user;
  }

  public Map<String, Object> asMap() {

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(NoteItem.class, new NoteItemTypeAdapter())
            .create();
    setFridgeItemsJson(ItemList.getItemList());
    List<FridgeItem> noteItems = new Select()
            .from(NoteItem.class).execute();
    setNoteItemsJson(gson.toJson(noteItems));

    // this could also be done by a fancy object mapper
    HashMap<String, Object> map = new HashMap<>();
    map.put("id", getId());
    map.put("fullName", getFullName());
    map.put("imageUrl", getImageUrl());
    map.put("email", getEmail());
    map.put("items", getFridgeItemsJson());
    map.put("notes", getNoteItemsJson());
    return map;
  }

  @Override
  public String toString() {

    String details = "";
    details += "id: " + getId() + "\n";
    details += "fullName: " + getFullName() + "\n";
    details += "imageUrl: " + getImageUrl() + "\n";
    details += "email: " + getEmail() + "\n";
    details += "items: " + getFridgeItemsJson() + "\n";
    details += "notes: " + getNoteItemsJson() + "\n";
    return details;
  }
}
