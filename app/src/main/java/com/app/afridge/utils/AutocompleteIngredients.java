package com.app.afridge.utils;

import com.activeandroid.query.Select;
import com.app.afridge.dom.Ingredient;

import java.util.ArrayList;
import java.util.List;


/**
 * Auto-complete ingredients
 * <p/>
 * Created by drakuwa on 2/14/14.
 */
public class AutocompleteIngredients {

  private List<Ingredient> ingredients = new ArrayList<>();

  public AutocompleteIngredients() {

    try {
      ingredients = new Select().from(Ingredient.class).execute();
    }
    catch (Exception e) {
      Log.e(Log.TAG, "Exception: " + e.getLocalizedMessage());
    }
  }

  public ArrayList<String> autocomplete(String input) {

    input = input.toLowerCase();
    ArrayList<String> resultList = new ArrayList<String>();
    for (Ingredient ingredient : ingredients) {
      if (ingredient.getName().contains(input)) {
        resultList.add(ingredient.getName());
      }
    }
    return resultList;
  }
}
