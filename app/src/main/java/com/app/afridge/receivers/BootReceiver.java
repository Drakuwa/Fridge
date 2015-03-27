package com.app.afridge.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.afridge.services.ExpirationDateService;
import com.app.afridge.utils.SharedPrefStore;

import java.util.Calendar;


/**
 * Receive an intent when the device boots up
 * so we can set the alarm service
 * <p/>
 * Created by drakuwa on 2/22/15.
 */
public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent i) {

    // initialize the shared preferences manager
    SharedPrefStore prefStore = SharedPrefStore.load(context);
    if (i.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
      // get the service intent and alarm manager
      Intent intent = new Intent(context, ExpirationDateService.class);
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      PendingIntent pIntent = PendingIntent.getService(context, 0, intent, 0);

      // set the new time
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MINUTE, 0);
      switch (prefStore.getInt(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION_TIME)) {
        case 0:
          cal.set(Calendar.HOUR_OF_DAY, 9);
          break;
        case 1:
          cal.set(Calendar.HOUR_OF_DAY, 13);
          break;
        case 2:
          cal.set(Calendar.HOUR_OF_DAY, 17);
          break;
        case 3:
          cal.set(Calendar.HOUR_OF_DAY, 20);
          break;
      }

      // With setInexactRepeating(), you have to use one of the AlarmManager interval
      // constants - in this case, AlarmManager.INTERVAL_DAY.
      alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
              AlarmManager.INTERVAL_DAY, pIntent);
    }
  }
}