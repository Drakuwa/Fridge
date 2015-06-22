package com.app.afridge.widget;

import com.activeandroid.query.Select;
import com.app.afridge.R;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.utils.CircleTransform;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.SharedPrefStore;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RemoteViewsService class for the Fridge App Widget
 *
 * Created by drakuwa on 6/17/15.
 */
public class FridgeWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private List<FridgeItem> mWidgetItems = new ArrayList<>();

    private Context mContext;

    private int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        mWidgetItems = new Select()
                .from(FridgeItem.class)
                .where("status != ?", true)
                .execute();
        // We sleep for 0.5 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }

    public int getCount() {
        return mWidgetItems.size();
    }

    public RemoteViews getViewAt(int position) {
        // fix for IndexOutOfBounds exception
        if (position >= getCount()) {
            return getLoadingView();
        }
        // get the current item
        FridgeItem item = mWidgetItems.get(position);
        // position will always range from 0 to getCount() - 1.
        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        final RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_item_fridge);
        remoteViews.setTextViewText(R.id.text_name, item.getName());

        // set the expiration date label
        if (item.getExpirationDate() != 0) {
            remoteViews.setTextViewText(R.id.text_expiration, Common.getTimestamp(item, mContext));
        } else {
            remoteViews.setTextViewText(R.id.text_expiration,
                    "exp. date: " + mContext.getString(R.string.not_set));
        }

        // set the quantity
        if (item.getQuantity() != null && item.getQuantity().length() > 0) {
            String quantity = item.getQuantity() + " ";
            if (item.getTypeOfQuantity() != -1) {
                // get the selected measurement type
                SharedPrefStore prefStore = SharedPrefStore.load(mContext);
                assert prefStore != null;
                int selectedMeasurementType = prefStore.getInt(
                        SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE);
                switch (selectedMeasurementType) {
                    case 0: // metric
                        quantity += mContext.getResources()
                                .getStringArray(R.array.quantity_type_metric)[item
                                .getTypeOfQuantity()];
                        break;
                    case 1: // imperial
                        quantity += mContext.getResources()
                                .getStringArray(R.array.quantity_type_imperial)[item
                                .getTypeOfQuantity()];
                        break;
                }
            }

            remoteViews.setTextViewText(R.id.text_quantity, quantity);
        } else {
            remoteViews.setTextViewText(R.id.text_quantity,
                    "quantity: " + mContext.getString(R.string.not_set));
        }

        // check if the type of the item is one of the predefined, or a file
        // and set the image accordingly - find a better way!
        File itemType = new File(item.getType());
        Picasso loader = Picasso.with(mContext);
        final RequestCreator requestCreator;

        if (TextUtils.isDigitsOnly(item.getType())) {
            requestCreator = loader.load(ItemType.DRAWABLES[Integer.parseInt(item.getType())]);
        } else {
            requestCreator = loader.load(itemType);
        }

        try {
            Bitmap bitmap = requestCreator
                    .resize(mContext.getResources()
                                    .getDimensionPixelSize(R.dimen.item_image_size),
                            mContext.getResources()
                                    .getDimensionPixelSize(R.dimen.item_image_size))
                    .centerInside()
                    .transform(new CircleTransform())
                    .error(R.drawable.fridge_placeholder)
                    .get();
            remoteViews.setImageViewBitmap(R.id.image_item, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(FridgeAppWidgetProvider.EXTRA_ITEM_ID, item.getItemId());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteViews.setOnClickFillInIntent(R.id.stackWidgetItem, fillInIntent);
        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        //        try {
        //            System.out.println("Loading view " + position);
        //            Thread.sleep(500);
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        // Return the remote views object.
        return remoteViews;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
        // return new RemoteViews(mContext.getPackageName(), R.layout.widget_item_loading);
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
        mWidgetItems.clear();
        mWidgetItems = new Select()
                .from(FridgeItem.class)
                .where("status != ?", true)
                .execute();
    }
}
