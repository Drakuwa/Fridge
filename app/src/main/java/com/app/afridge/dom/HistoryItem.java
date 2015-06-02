package com.app.afridge.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.app.afridge.dom.enums.ChangeType;


/**
 * History item
 * <p/>
 * Created by drakuwa on 1/27/15.
 */
@Table(name = "HistoryItems")
public class HistoryItem extends Model {

  @Column(name = "item")
  private FridgeItem fridgeItem;
  @Column(name = "timestamp", index = true)
  private long timestamp;
  @Column(name = "change_type")
  private ChangeType changeType; // ADD, MODIFY, DELETE

  public HistoryItem() {
    // empty constructor
    super();
  }

  public HistoryItem(FridgeItem fridgeItem, long timestamp, ChangeType changeType) {

    super();
    this.fridgeItem = fridgeItem;
    this.timestamp = timestamp;
    this.changeType = changeType;
  }

  public FridgeItem getFridgeItem() {

    return fridgeItem;
  }

  public void setFridgeItem(FridgeItem fridgeItem) {

    this.fridgeItem = fridgeItem;
  }

  public long getTimestamp() {

    return timestamp;
  }

  public void setTimestamp(long timestamp) {

    this.timestamp = timestamp;
  }

  public ChangeType getChangeType() {

    return changeType;
  }

  public void setChangeType(ChangeType changeType) {

    this.changeType = changeType;
  }

  @Override
  public String toString() {

    String details = "";
    details += "id: " + getId() + "\n";
    details += "fridge item: " + getFridgeItem().toString() + "\n";
    details += "timestamp: " + getTimestamp() + "\n";
    details += "change type: " + getChangeType() + "\n";
    return details;
  }
}
