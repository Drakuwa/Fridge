package com.app.afridge.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.views.FancyCoverFlow;


/**
 * Fallback implementation of carousel
 * <p/>
 * Created by drakuwa on 2/26/15.
 */

public class ItemCarouselAdapter extends FancyCoverFlowAdapter {

  private FridgeApplication application;

  public ItemCarouselAdapter(FridgeApplication application) {

    this.application = application;
  }

  @Override
  public int getCount() {

    return ItemType.DRAWABLES.length;
  }

  @Override
  public String getItem(int position) {

    return application.types.get(ItemType.DRAWABLES[position]).name();
  }

  @Override
  public long getItemId(int pos) {

    return pos;
  }


  @Override
  public View getCoverFlowItem(int position, View reusableView, ViewGroup parent) {

    ImageView imageView;

    if (reusableView != null) {
      imageView = (ImageView) reusableView;
    }
    else {
      imageView = new ImageView(parent.getContext());
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      imageView.setLayoutParams(new FancyCoverFlow.LayoutParams(
              (int) application.getResources().getDimension(R.dimen.picker_item_size) * 2,
              (int) application.getResources().getDimension(R.dimen.picker_item_size) * 2));
    }

    imageView.setImageResource(ItemType.DRAWABLES[position]);
    imageView.setPadding(5, 5, 5, 5);
    return imageView;
  }
}