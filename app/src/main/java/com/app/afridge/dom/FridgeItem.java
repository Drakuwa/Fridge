package com.app.afridge.dom;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;


/**
 * Fridge item DOM description
 * <p/>
 * Created by drakuwa on 1/27/15.
 */
@Table(name = "FridgeItems")
public class FridgeItem extends Model implements Cloneable {

    @Column(name = "item_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE, index = true)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "type_of_quantity")
    private int typeOfQuantity;

    @Column(name = "details")
    private String details;

    @Column(name = "expiration_date")
    private long expirationDate;

    @Column(name = "status")
    private boolean isRemoved;

    @Column(name = "edit_timestamp")
    private long editTimestamp;

    public FridgeItem() {
        // empty constructor
        super();
    }

    public FridgeItem(int id, String name, String type, String quantity, int typeOfQuantity,
            String details, long expirationDate, long editTimestamp, boolean isRemoved) {

        super();
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.typeOfQuantity = typeOfQuantity;
        this.details = details;
        this.expirationDate = expirationDate;
        this.editTimestamp = editTimestamp;
        this.isRemoved = isRemoved;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getQuantity() {

        return quantity;
    }

    public void setQuantity(String quantity) {

        this.quantity = quantity;
    }

    public int getTypeOfQuantity() {

        return typeOfQuantity;
    }

    public void setTypeOfQuantity(int typeOfQuantity) {

        this.typeOfQuantity = typeOfQuantity;
    }

    public String getDetails() {

        return details;
    }

    public void setDetails(String details) {

        this.details = details;
    }

    public long getExpirationDate() {

        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {

        this.expirationDate = expirationDate;
    }

    public long getEditTimestamp() {

        return editTimestamp;
    }

    public void setEditTimestamp(long editTimestamp) {

        this.editTimestamp = editTimestamp;
    }

    public int getItemId() {

        return id;
    }

    public void setItemId(int id) {

        this.id = id;
    }

    public boolean isRemoved() {

        return isRemoved;
    }

    public void setRemoved(boolean isRemoved) {

        this.isRemoved = isRemoved;
    }

    // This method is optional, does not affect the foreign key creation.
    @SuppressWarnings("unused")
    public List<HistoryItem> historyItems() {

        return getMany(HistoryItem.class, "item");
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id: ").append(getId()).append("\n");
        stringBuilder.append("item_id: ").append(getItemId()).append("\n");
        stringBuilder.append("name: ").append(getName()).append("\n");
        stringBuilder.append("type: ").append(getType()).append("\n");
        stringBuilder.append("quantity: ").append(getQuantity()).append("\n");
        stringBuilder.append("typeOfQuantity: ").append(getTypeOfQuantity()).append("\n");
        stringBuilder.append("details: ").append(getDetails()).append("\n");
        stringBuilder.append("expiration_date: ").append(getExpirationDate()).append("\n");
        stringBuilder.append("edit_timestamp: ").append(getEditTimestamp()).append("\n");
        stringBuilder.append("isRemoved: ").append(isRemoved()).append("\n");
        return stringBuilder.toString();
    }

    public Object clone() throws CloneNotSupportedException {

        return super.clone();
    }

    public boolean equals(FridgeItem fridgeItem) {

        if (getItemId() != fridgeItem.getItemId()) {
            return false;
        }
        if (!getName().equals(fridgeItem.getName())) {
            return false;
        }
        if (!getType().equals(fridgeItem.getType())) {
            return false;
        }
        try {
            if (!getQuantity().equals(fridgeItem.getQuantity())) {
                return false;
            }
        } catch (NullPointerException ignored) {

        }
        if (getTypeOfQuantity() != fridgeItem.getTypeOfQuantity()) {
            return false;
        }
        try {
            if (!getDetails().equals(fridgeItem.getDetails())) {
                return false;
            }
        } catch (NullPointerException ignored) {

        }
        return getExpirationDate() == fridgeItem.getExpirationDate() && isRemoved() == fridgeItem
                .isRemoved();
    }
}
