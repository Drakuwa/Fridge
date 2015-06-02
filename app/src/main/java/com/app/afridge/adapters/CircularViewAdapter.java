package com.app.afridge.adapters;

import com.app.afridge.FridgeApplication;
import com.app.afridge.dom.enums.ItemType;
import com.sababado.circularview.Marker;
import com.sababado.circularview.SimpleCircularViewAdapter;

import java.util.HashMap;


public class CircularViewAdapter extends SimpleCircularViewAdapter {

  private HashMap<Integer, String> markerList = new HashMap<>(getCount());
  private FridgeApplication application;

  public CircularViewAdapter(FridgeApplication application) {

    this.application = application;
    this.markerList.clear();
  }

  @Override
  public int getCount() {
    // This count will tell the circular view how many markers to use.
    return ItemType.DRAWABLES.length;
  }

  @Override
  public void setupMarker(final int position, final Marker marker) {
    // Setup and customize markers here. This is called every time a marker is to be displayed.
    // 0 >= position > getCount()
    // The marker is intended to be reused. It will never be null.
    marker.setSrc(ItemType.DRAWABLES[position]);
    marker.setFitToCircle(true);
    marker.setRadius(40);
    markerList.put(marker.getId(), application.types.get(ItemType.DRAWABLES[position]).name());
  }

  public String getMarkerName(int position) {

    return markerList.get(position);
  }
}