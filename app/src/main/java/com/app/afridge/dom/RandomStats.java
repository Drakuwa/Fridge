package com.app.afridge.dom;

import android.content.Context;

import com.activeandroid.query.Select;
import com.app.afridge.utils.SharedPrefStore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


/**
 * Random application statistics
 * <p/>
 * Created by drakuwa on 3/30/15.
 */
public class RandomStats {

  // Singleton
  public static RandomStats instance = null;
  private final Context context;
  private List<Stat> stats = new ArrayList<>();

  /**
   * Get instance of RandomStats
   *
   * @param context Context object
   * @return an instance of RandomStats
   */
  public static RandomStats with(Context context) {

    if (instance == null) {
      synchronized (RandomStats.class) {
        if (instance == null) {
          instance = new RandomStats(context);
        }
      }
    }
    return instance;
  }

  /**
   * constructor
   *
   * @param context Context instance
   */
  public RandomStats(Context context) {

    if (context == null) {
      throw new IllegalArgumentException("Context must not be null.");
    }
    this.context = context.getApplicationContext();
  }

  public void generateList(boolean refreshStats) {

    if (stats.size() == 0 || refreshStats) {

      int randomSpinCount = SharedPrefStore.load(context).getInt(SharedPrefStore.Pref.STAT_RANDOM_SPIN);
      int openFridgeCount = SharedPrefStore.load(context).getInt(SharedPrefStore.Pref.STAT_FRIDGE_OPEN);
      int cheeseItemsCount = new Select().from(FridgeItem.class).where("type = ?", "3").and("status = ?", false).count();
      int meatItemsCount = new Select().from(FridgeItem.class).where("type = ?", "4").and("status = ?", false).count();
      int leftoversItemsCount = new Select().from(FridgeItem.class).where("type = ?", "10").and("status = ?", false).count();
      int drinksItemsCount = new Select().from(FridgeItem.class).where("type = ?", "16").and("status = ?", false).count();
      int cakeItemsCount = new Select().from(FridgeItem.class).where("type = ?", "20").and("status = ?", false).count();
      Calendar calendar = Calendar.getInstance();
      long currentTimestamp = calendar.getTimeInMillis() / 1000;
      int expiredItemsCount = new Select().from(FridgeItem.class)
              .where("expiration_date != 0")
              .and("status = ?", false) // still not removed
              .and("expiration_date < ?", currentTimestamp)
              .count();
      int thrownAwayItems = new Select().from(FridgeItem.class).where("status = ?", true).count();

      stats.add(new Stat("Times you've spin the category wheel:", randomSpinCount));
      stats.add(new Stat("How many times you've opened the fridge:", openFridgeCount));
      stats.add(new Stat("Expired items you should consider getting rid of:", expiredItemsCount));
      stats.add(new Stat("Number of items you've thrown away:", thrownAwayItems));
      stats.add(new Stat("Different kinds of cheese in your fridge:", cheeseItemsCount));
      stats.add(new Stat("Different kinds of meat in your fridge:", meatItemsCount));
      stats.add(new Stat("Drinks in your fridge:", drinksItemsCount));
      stats.add(new Stat("Leftover food forgotten in the fridge:", leftoversItemsCount));
      stats.add(new Stat("Different cakes in your fridge:", cakeItemsCount));
      stats.add(new Stat("Total weight of countable items:", getTotalWeight()));
    }
  }

  public Stat getRandomStat() {

    int idx = new Random().nextInt(stats.size());
    return stats.get(idx);
  }

  public Stat getItemCount() {

    int fridgeItemsCount = new Select().from(FridgeItem.class).where("status = ?", false).count();
    return new Stat("Total item count:", fridgeItemsCount);
  }

  public Stat getNoteCount() {

    int noteItemsCount = new Select().from(NoteItem.class).count();
    return new Stat("Sticky notes on your fridge door:", noteItemsCount);
  }

  private String getTotalWeight() {

    List<FridgeItem> items = new Select().from(FridgeItem.class)
            .where("type_of_quantity = ?", 0)
            .or("type_of_quantity = ?", 1)
            .or("type_of_quantity = ?", 4).execute();
    int selectedMeasurementType = SharedPrefStore.load(context)
            .getInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE);
    int totalCount = 0;
    String total;
    if (selectedMeasurementType == 0) {
      // metric - count in grams
      for (FridgeItem item : items) {
        if (item.getQuantity() != null) {
          switch (item.getTypeOfQuantity()) {
            case 0: // kilograms
              try {
                totalCount += Integer.parseInt(item.getQuantity()) * 1000;
              }
              catch (Exception ignored) {
              }
              break;
            case 1: // grams
              try {
                totalCount += Integer.parseInt(item.getQuantity());
              }
              catch (Exception ignored) {
              }
              break;
            case 4: // cups - depending on the item, 1 cup could be from 100 to 300
              try {
                totalCount += Integer.parseInt(item.getQuantity()) * 200;
              }
              catch (Exception ignored) {
              }
              break;
          }
        }
      }
      total = totalCount + " grams";
    }
    else {
      // imperial - count in pounds
      for (FridgeItem item : items) {
        if (item.getQuantity() != null) {
          switch (item.getTypeOfQuantity()) {
            case 0: // pounds
              try {
                totalCount += Integer.parseInt(item.getQuantity()) * 16;
              }
              catch (Exception ignored) {
              }
              break;
            case 1: // ounces
              try {
                totalCount += Integer.parseInt(item.getQuantity());
              }
              catch (Exception ignored) {
              }
              break;
            case 4: // cups - depending on the item, 1 cup is 8 ounces
              try {
                totalCount += Integer.parseInt(item.getQuantity()) * 8;
              }
              catch (Exception ignored) {
              }
              break;
          }
        }
      }
      total = totalCount * 0.0625 + " pounds";
    }

    return total;
  }

  public class Stat {

    private String name;
    private String value;

    public Stat(String name, String value) {

      this.name = name;
      this.value = value;
    }

    public Stat(String name, int value) {

      this.name = name;
      this.value = String.valueOf(value);
    }

    public String getName() {

      return name;
    }

    public void setName(String name) {

      this.name = name;
    }

    public String getValue() {

      return value;
    }

    public void setValue(String value) {

      this.value = value;
    }

    @Override
    public String toString() {

      String details = "";
      details += "name: " + getName() + "\n";
      details += "value: " + getValue() + "\n";
      return details;
    }
  }
}
