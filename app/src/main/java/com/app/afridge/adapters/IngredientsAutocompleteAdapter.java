package com.app.afridge.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.app.afridge.utils.AutocompleteIngredients;

import java.util.ArrayList;


/**
 * Auto-complete ingredients adapter
 * <p/>
 * Created by drakuwa on 2/14/14.
 */
public class IngredientsAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {

  private ArrayList<String> resultList;

  public IngredientsAutocompleteAdapter(Context context, int textViewResourceId) {

    super(context, textViewResourceId);
  }

  @Override
  public int getCount() {

    return resultList.size();
  }

  @Override
  public String getItem(int index) {

    return resultList.get(index);
  }

  @Override
  public Filter getFilter() {

    return new Filter() {

      @Override
      protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults filterResults = new FilterResults();
        if (constraint != null) {
          // Retrieve the autocomplete results.
          AutocompleteIngredients autocompleteIngredients = new AutocompleteIngredients();
          resultList = autocompleteIngredients.autocomplete(constraint
                  .toString());

          // Assign the data to the FilterResults
          filterResults.values = resultList;
          filterResults.count = resultList.size();
        }
        return filterResults;
      }

      @Override
      protected void publishResults(CharSequence constraint,
                                    FilterResults results) {

        if (results != null && results.count > 0) {
          notifyDataSetChanged();
        }
        else {
          notifyDataSetInvalidated();
        }
      }
    };
  }
}
