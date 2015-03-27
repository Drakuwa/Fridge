package com.app.afridge.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.app.afridge.R;
import com.app.afridge.adapters.model.GalleryItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * Gallery chooser adapter
 * <p/>
 * Created by drakuwa on 29.12.2014.
 */
public class GalleryAdapter extends BaseAdapter implements Filterable {

  private static final int TAKE_PHOTO = -2;
  private static final int CHOOSE_PHOTO = -1;

  private List<GalleryItem> allItems;
  private List<GalleryItem> items;
  private LayoutInflater inflater;
  private boolean isVideo = false;

  public GalleryAdapter(Context context, List<GalleryItem> items, boolean isVideo) {

    // add the chose photo and select from gallery items at position 0 and 1
    GalleryItem choseFromGalleryItem = new GalleryItem();
    choseFromGalleryItem.setId(CHOOSE_PHOTO);
    items.add(0, choseFromGalleryItem);

    GalleryItem takePhotoItem = new GalleryItem();
    takePhotoItem.setId(TAKE_PHOTO);
    items.add(0, takePhotoItem);

    this.inflater = LayoutInflater.from(context);
    this.allItems = items;
    this.items = items;
    this.isVideo = isVideo;
  }

  @Override
  public int getCount() {

    return items.size();
  }

  @Override
  public GalleryItem getItem(int position) {

    return items.get(position);
  }

  @Override
  public long getItemId(int position) {

    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_gallery, null);
    }

    GalleryItem item = items.get(position);

    ImageView image = (ImageView) convertView.findViewById(R.id.image_thumb);
    if (position > 1) {
      Picasso.with(inflater.getContext())
              .load(item.getImageUri())
              .fit()
              .centerCrop()
              .into(image);
    }
    else if (position == 1) {
      image.setImageResource(R.drawable.ic_action_picture);
    }
    else {
      if (isVideo) {
        image.setImageResource(R.drawable.ic_action_video);
      }
      else {
        image.setImageResource(R.drawable.ic_action_camera);
      }
    }

    return convertView;
  }

  @Override
  public Filter getFilter() {

    return new Filter() {

      @SuppressWarnings("unchecked")
      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {

        items = (List<GalleryItem>) results.values;
        notifyDataSetChanged();
      }

      @Override
      protected FilterResults performFiltering(CharSequence constraint) {

        List<GalleryItem> filteredResults = getFilteredResults(constraint);

        FilterResults results = new FilterResults();
        results.values = filteredResults;

        return results;
      }

      private List<GalleryItem> getFilteredResults(CharSequence constraint) {

        // this is the top level filter
        // when all galleries are selected all items should be shown
        if (constraint.toString().equalsIgnoreCase(inflater.getContext().getString(R.string.hint_all))) {
          return allItems;
        }

        // filter for specific gallery
        List<GalleryItem> filteredList = new ArrayList<>();
        for (GalleryItem item : allItems) {
          if (item.getId() == TAKE_PHOTO || item.getId() == CHOOSE_PHOTO || item.getBucketName().equalsIgnoreCase(constraint.toString())) {
            filteredList.add(item);
          }
        }
        return filteredList;
      }
    };
  }


}
