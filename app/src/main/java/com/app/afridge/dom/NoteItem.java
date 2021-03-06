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

    @Column(name = "item_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE, index = true)
    private int id;

    @Column(name = "note")
    private String note;

    @Column(name = "timestamp")
    private long timestamp;

    @Column(name = "is_checked")
    private boolean isChecked;

    @Column(name = "status")
    private boolean isRemoved = false;

    public NoteItem() {
        // empty constructor
        super();
    }

    public NoteItem(String note, long timestamp, boolean isChecked, boolean isRemoved) {

        super();
        this.note = note;
        this.timestamp = timestamp;
        this.isChecked = isChecked;
        this.isRemoved = isRemoved;
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

    public int getItemId() {

        return id;
    }

    public void setItemId(int id) {

        this.id = id;
    }

    public boolean isChecked() {

        return isChecked;
    }

    public void setChecked(boolean isChecked) {

        this.isChecked = isChecked;
    }

    public boolean isRemoved() {

        return isRemoved;
    }

    public void setRemoved(boolean isRemoved) {

        this.isRemoved = isRemoved;
    }

    @Override
    public String toString() {

        String noteStr = "";
        noteStr += "id: " + getId() + "\n";
        noteStr += "note: " + getNote() + "\n";
        noteStr += "timestamp: " + getTimestamp() + "\n";
        noteStr += "is_checked: " + isChecked() + "\n";
        noteStr += "is_removed: " + isRemoved() + "\n";
        return noteStr;
    }
}
