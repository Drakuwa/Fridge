package com.app.afridge.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


/**
 * Fridge notes
 * <p/>
 * Created by drakuwa on 1/27/15.
 */
@Table(name = "NoteItems")
public class NoteItem extends Model {

  @Column(name = "note")
  private String note;
  @Column(name = "timestamp")
  private long timestamp;
  @Column(name = "is_checked")
  private boolean isChecked;

  public NoteItem() {
    // empty constructor
    super();
  }

  public NoteItem(String note, long timestamp, boolean isChecked) {

    super();
    this.note = note;
    this.timestamp = timestamp;
    this.isChecked = isChecked;
  }

  public String getNote() {

    return note;
  }

  public void setNote(String note) {

    this.note = note;
  }

  public long getTimestamp() {

    return timestamp;
  }

  public void setTimestamp(long timestamp) {

    this.timestamp = timestamp;
  }

  public boolean isChecked() {

    return isChecked;
  }

  public void setChecked(boolean isChecked) {

    this.isChecked = isChecked;
  }

  @Override
  public String toString() {

    String noteStr = "";
    noteStr += "id: " + getId() + "\n";
    noteStr += "note: " + getNote() + "\n";
    noteStr += "timestamp: " + getTimestamp() + "\n";
    noteStr += "is_checked: " + isChecked() + "\n";
    return noteStr;
  }
}
