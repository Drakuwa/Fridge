package com.app.afridge.dom.enums;

import com.app.afridge.R;


/**
 * Predefined fridge item types
 * <p/>
 * Created by drakuwa on 2/5/15.
 */
public enum ItemType {
  OTHER,
  MILK,
  EGGS,
  CHEESE,
  MEAT,
  HAM,
  SAUSAGE,
  MAYONNAISE,
  KETCHUP,
  MUSTARD,
  LEFTOVERS,
  PICKLES,
  JAM,
  PESTO,
  BUTTER,
  CREAM,
  DRINKS,
  FISH,
  FRUIT,
  VEGETABLES,
  CAKE;

  public int getItemOrdinal(ItemType item) {

    return item.getItemOrdinal(item);
  }

  public static int[] DRAWABLES = {
          R.drawable.ic_other_larger, R.drawable.ic_milk, R.drawable.ic_eggs,
          R.drawable.ic_cheese, R.drawable.ic_meat, R.drawable.ic_ham,
          R.drawable.ic_sausages, R.drawable.ic_mayo, R.drawable.ic_ketchup,
          R.drawable.ic_mustard, R.drawable.ic_leftovers, R.drawable.ic_pickles,
          R.drawable.ic_jam, R.drawable.ic_pesto, R.drawable.ic_butter,
          R.drawable.ic_cream, R.drawable.ic_drinks, R.drawable.ic_fish,
          R.drawable.ic_fruit, R.drawable.ic_vegetable, R.drawable.ic_cake
  };
}
