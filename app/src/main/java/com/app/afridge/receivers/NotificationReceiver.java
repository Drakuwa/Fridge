package com.app.afridge.receivers;

import com.activeandroid.query.Select;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.dom.enums.ChangeType;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.io.File;
import java.util.Calendar;


/**
 * Handle "delete" action from notification
 * <p/>
 * Created by drakuwa on 3/30/15.
 */
public class NotificationReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(final Context context, Intent intent) {
    // get the action
    String action = intent.getAction();

    // get the bundle arguments
    Bundle args = intent.getExtras();

    // get the itemId and item
    int itemId = args.getInt(Constants.EXTRA_ITEM_ID);
    final FridgeItem item = new Select().from(FridgeItem.class).where("item_id = ?", String.valueOf(itemId)).executeSingle();

    // get the history item id if it exists
    long historyItemId = -1;
    if (intent.hasExtra(Constants.EXTRA_HISTORY_ITEM_ID)) {
      historyItemId = args.getLong(Constants.EXTRA_HISTORY_ITEM_ID);
    }

    // if you want cancel notification
    final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    if (action.equalsIgnoreCase(Constants.ACTION_DELETE)) {

      // recreate the notification with the same id
      final NotificationCompat.Builder mBuilder =
              new NotificationCompat.Builder(context)
                      .setSmallIcon(R.drawable.ic_photo_camera)
                      .setContentTitle(context.getString(R.string.title_notification_deleted))
                      .setContentText("Item: " + item.getName() + " has been successfully deleted. " +
                              "Click on the UNDO button to revert the changes.")
                      .setAutoCancel(true);

      // create the history item in advance, so we can set the HistoryItem id to the undo action
      HistoryItem historyItem = new HistoryItem(item,
              Calendar.getInstance().getTimeInMillis() / 1000, ChangeType.DELETE);
      // save the delete in history
      historyItem.save();

      // Sets up the Undo action buttons that will appear in the
      // big view of the notification.
      Intent undoIntent = new Intent(context, NotificationReceiver.class);
      undoIntent.setAction(Constants.ACTION_UNDO);
      undoIntent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());
      undoIntent.putExtra(Constants.EXTRA_HISTORY_ITEM_ID, historyItem.getId());
      final PendingIntent piUndo = PendingIntent.getBroadcast(context, item.getItemId(), undoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      // set the image
      final Target target = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

          mBuilder.setLargeIcon(bitmap);
          mBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
          mBuilder.addAction(android.R.drawable.ic_menu_revert, context.getString(R.string.undo), piUndo);

          // FIRE ZE MISSILES!
          item.setRemoved(true);
          item.save();

          // mId allows you to update the notification later on.
          manager.notify(item.getItemId(), mBuilder.build());

          // create the notification dismiss runnable
          Runnable deleteItem = new Runnable() {

            @Override
            public void run() {
              // hide the notification and delete the item
              manager.cancel(item.getItemId());
            }
          };

          // dismiss the notification after the specified time
          new Handler().postDelayed(deleteItem, Constants.DISMISS_NOTIFICATION_LENGTH);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
      };
      // Get a handler that can be used to post to the main thread
      Handler mainHandler = new Handler(context.getMainLooper());

      // create the Picasso loader and request creator
      Picasso loader = Picasso.with(context.getApplicationContext());
      final RequestCreator requestCreator;
      if (TextUtils.isDigitsOnly(item.getType())) {
        requestCreator = loader.load(ItemType.DRAWABLES[Integer.parseInt(item.getType())]);
      }
      else {
        requestCreator = loader.load(new File(item.getType()))
                .resize(
                        ((FridgeApplication) (context.getApplicationContext())).screenWidth / 2,
                        ((FridgeApplication) (context.getApplicationContext())).screenWidth / 2);
      }

      // load the image into the Target object
      Runnable runnable = new Runnable() {

        @Override
        public void run() {

          requestCreator
                  .error(R.mipmap.ic_launcher)
                  .into(target);
        }
      };
      mainHandler.post(runnable);
    }
    else if (action.equalsIgnoreCase(Constants.ACTION_UNDO)) {
      Log.d(Log.TAG, "undo clicked on : " + item.getName());
      // hide the notification
      manager.cancel(item.getItemId());

      // remove the delete item in history
      if (historyItemId != -1) {
        HistoryItem historyItem = HistoryItem.load(HistoryItem.class, historyItemId);
        historyItem.delete();
      }

      // return the item
      item.setRemoved(false);
      item.save();
    }
  }
}
