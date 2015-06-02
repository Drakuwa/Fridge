package com.app.afridge.services;

import com.activeandroid.query.Select;
import com.app.afridge.FridgeApplication;
import com.app.afridge.R;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.enums.ItemType;
import com.app.afridge.receivers.NotificationReceiver;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Constants;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import java.io.File;
import java.util.Calendar;
import java.util.List;


/**
 * Background service that checks for item expiration dates
 * <p/>
 * Created by drakuwa on 2/19/15.
 */
@SuppressWarnings("UnusedDeclaration")
public class ExpirationDateService extends IntentService {

    private static final int ONE_DAY_SECONDS = 60 * 60 * 24;

    private FridgeApplication application;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public ExpirationDateService() {

        super("ExpirationDateService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ExpirationDateService(String name) {

        super(name);
    }

    @Override
    public void onCreate() {
        // get the application instance
        application = ((FridgeApplication) getApplication());
        application.prefStore.setBoolean(SharedPrefStore.Pref.IS_SERVICE_RUNNING, true);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(Log.TAG, "service started");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // if the application is running do not show the notification
        if (FridgeApplication.isActivityVisible()) {
            Log.d(Log.TAG, "application is running");
        } else {
            // check if the user wants to get notifications
            if (application.prefStore.getBoolean(
                    SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION)) {
                int warningDays = application.prefStore.getInt(
                        SharedPrefStore.Pref.SETTINGS_EXP_DATE_WARNING);
                long warningSeconds = warningDays * ONE_DAY_SECONDS;

                // get the current timestamp
                Calendar calendar = Calendar.getInstance();
                long currentTimestamp = calendar.getTimeInMillis() / 1000;

                // get the fridge items with expiration dates
                List<FridgeItem> items = new Select()
                        .from(FridgeItem.class)
                        .where("expiration_date != 0") // .where("expiration_date != 0")
                        .and("status = ?", false)
                        .orderBy("expiration_date DESC")
                        .execute();

                // look through all the items
                for (FridgeItem item : items) {
                    if ((item.getExpirationDate() - currentTimestamp) < warningSeconds) {
                        // we've found an item that has expired, or warning level
                        Log.d(Log.TAG, item.toString());
                        // show the notification
                        showNotification(item);
                    }
                }

            } else {
                Log.d(Log.TAG, "user has disabled notifications");
            }
        }
    }

    private void showNotification(final FridgeItem item) {

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_photo_camera)
                        .setContentTitle(getString(R.string.title_notification))
                        .setContentText("Item: " + item.getName() + " " + Common
                                .getTimestamp(item, application) +
                                " Would you like to remove it from the Fridge?")
                        .setAutoCancel(true);

        // Sets up the Dismiss action buttons that will appear in the
        // big view of the notification.
        Intent deleteIntent = new Intent(this, NotificationReceiver.class);
        deleteIntent.setAction(Constants.ACTION_DELETE);
        deleteIntent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());
        final PendingIntent piDelete = PendingIntent
                .getBroadcast(this, item.getItemId(), deleteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // set the image
        final Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                mBuilder.setLargeIcon(bitmap);

                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(ExpirationDateService.this, MainActivity.class);
                resultIntent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());

                PendingIntent contentIntent = PendingIntent.getActivity(ExpirationDateService.this,
                        (int) System.currentTimeMillis(), resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentIntent(contentIntent);
                mBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
                mBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.delete), piDelete);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(item.getItemId(), mBuilder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(this.getMainLooper());

        // create the Picasso loader
        Picasso loader = Picasso.with(application.getApplicationContext());
        final RequestCreator requestCreator;
        if (TextUtils.isDigitsOnly(item.getType())) {
            requestCreator = loader.load(ItemType.DRAWABLES[Integer.parseInt(item.getType())]);
        } else {
            requestCreator = loader.load(new File(item.getType()))
                    .resize(application.screenWidth / 2, application.screenWidth / 2);
        }

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

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {

        application.prefStore.setBoolean(SharedPrefStore.Pref.IS_SERVICE_RUNNING, false);
        super.onDestroy();
    }

    public void showNotificationOld(final FridgeItem item) {

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_photo_camera)
                        .setContentTitle("Expiration date warning")
                        .setContentText("Item: " + item.getName() + " is about to expire.")
                        .setAutoCancel(true);

        final NotificationCompat.BigPictureStyle bigPictureStyle
                = new NotificationCompat.BigPictureStyle();
        // Sets a title for the Inbox in expanded layout
        bigPictureStyle.setBigContentTitle("Expired items details:");

        // Sets up the Snooze and Dismiss action buttons that will appear in the
        // big view of the notification.
        Intent dismissIntent = new Intent(this, MainActivity.class);
        dismissIntent.setAction(Constants.ACTION_DELETE);
        final PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

        // set the image
        final Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                bigPictureStyle.bigLargeIcon(bitmap);
                mBuilder.setLargeIcon(bitmap);
                // Moves the expanded layout object into the notification object.
                // mBuilder.setStyle(bigPictureStyle); TODO

                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(ExpirationDateService.this, MainActivity.class);
                resultIntent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(ExpirationDateService.this);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
                mBuilder.addAction(android.R.drawable.ic_delete,
                        getString(R.string.dismiss), piDismiss);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(item.getItemId(), mBuilder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(this.getMainLooper());

        final File itemType = new File(item.getType());
        if (itemType.exists()) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    Picasso.with(application.getApplicationContext())
                            .load(itemType)
                            .resize(application.screenWidth / 2, application.screenWidth / 2)
                            .error(R.mipmap.ic_launcher)
                            .into(target);
                }
            };
            mainHandler.post(runnable);
        } else if (TextUtils.isDigitsOnly(item.getType())) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    Picasso.with(application.getApplicationContext())
                            .load(ItemType.DRAWABLES[Integer.parseInt(item.getType())])
                            .error(R.mipmap.ic_launcher)
                            .into(target);
                }
            };
            mainHandler.post(runnable);
        } else {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    Picasso.with(application.getApplicationContext())
                            .load(itemType)
                            .resize(application.screenWidth / 2, application.screenWidth / 2)
                            .error(R.mipmap.ic_launcher)
                            .into(target);
                }
            };
            mainHandler.post(runnable);
        }
    }
}
