package com.app.afridge.widget;

import com.app.afridge.R;
import com.app.afridge.dom.RandomStats;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.ui.fragments.FridgeFragment;
import com.app.afridge.ui.fragments.NotesFragment;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.Log;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

/**
 * App Widget Provider for the Fridge Widget
 *
 * Created by drakuwa on 6/17/15.
 */
public class FridgeAppWidgetProvider extends AppWidgetProvider {

    public static final String OPEN_ITEM_ACTION = "com.app.afridge.widget.OPEN_ITEM_ACTION";

    public static final String OPEN_FRIDGE_ACTION = "com.app.afridge.widget.OPEN_FRIDGE_ACTION";

    public static final String OPEN_NOTES_ACTION = "com.app.afridge.widget.OPEN_NOTES_ACTION";

    public static final String EXTRA_ITEM_ID = "com.app.afridge.widget.EXTRA_ITEM_ID";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager
                .getAppWidgetIds(new ComponentName(context, FridgeAppWidgetProvider.class));
        for (int id : ids) {
            // RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
            //        R.layout.fridge_appwidget);
            // appWidgetManager.updateAppWidget(id, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.stack_view_fridge);
        }
        //        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
        //                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (intent.getAction().equals(OPEN_ITEM_ACTION)) {
            // get the item ID
            int itemId = intent.getIntExtra(EXTRA_ITEM_ID, 0);

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.putExtra(Constants.EXTRA_ITEM_ID, itemId);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(resultIntent);
        } else if (intent.getAction().equals(OPEN_FRIDGE_ACTION)) {
            // start application
            Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            resultIntent.putExtra(Constants.EXTRA_ACTION, FridgeFragment.class.getCanonicalName());
            context.startActivity(resultIntent);
        } else if (intent.getAction().equals(OPEN_NOTES_ACTION)) {
            // start application and show notes
            Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            resultIntent.putExtra(Constants.EXTRA_ACTION, NotesFragment.class.getCanonicalName());
            context.startActivity(resultIntent);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("FridgeAppWidgetProvider onUpdate called");
        // update each of the widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {
            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            Intent intent = new Intent(context, FridgeWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.fridge_appwidget);
            remoteViews.setRemoteAdapter(appWidgetId, R.id.stack_view_fridge, intent);
            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            remoteViews.setEmptyView(R.id.stack_view_fridge, R.id.empty_view);

            // set item and notes count
            RandomStats.Stat itemCount = RandomStats.with(context).getItemCount();
            RandomStats.Stat noteCount = RandomStats.with(context).getNoteCount();
            remoteViews.setTextViewText(R.id.text_item_count, "Items: " + itemCount.getValue());
            remoteViews.setTextViewText(R.id.text_notes_count, "Notes: " + noteCount.getValue());

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent templateIntent = new Intent(context, FridgeAppWidgetProvider.class);
            templateIntent.setAction(FridgeAppWidgetProvider.OPEN_ITEM_ACTION);
            templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent
                    .getBroadcast(context, 0, templateIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.stack_view_fridge, toastPendingIntent);

            // set OPEN_FRIDGE_ACTION
            Intent fridgeIntent = new Intent(context, FridgeAppWidgetProvider.class);
            fridgeIntent.setAction(FridgeAppWidgetProvider.OPEN_FRIDGE_ACTION);
            PendingIntent pendingFridgeIntent = PendingIntent
                    .getBroadcast(context, 0, fridgeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.text_item_count, pendingFridgeIntent);

            // set OPEN_NOTES_ACTION
            Intent notesIntent = new Intent(context, FridgeAppWidgetProvider.class);
            notesIntent.setAction(FridgeAppWidgetProvider.OPEN_NOTES_ACTION);
            PendingIntent pendingNotesIntent = PendingIntent
                    .getBroadcast(context, 0, notesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.text_notes_count, pendingNotesIntent);

            // update widget manager
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
