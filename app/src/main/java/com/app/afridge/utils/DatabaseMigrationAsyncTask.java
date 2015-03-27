package com.app.afridge.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.app.afridge.FridgeApplication;


/**
 * Old database migration async task implementation
 * <p/>
 * Created by drakuwa on 3/19/15.
 */
public class DatabaseMigrationAsyncTask extends AsyncTask<Void, Void, Void> {

  private FridgeApplication application;

  public DatabaseMigrationAsyncTask(FridgeApplication application) {

    this.application = application;
  }

  @Override
  protected Void doInBackground(Void... params) {

    DatabaseMigrationHelper databaseMigrationHelper = new DatabaseMigrationHelper(application);
    databaseMigrationHelper.tryToMigrateDataBase();

    // try to migrate settings
    /**
     * com.app.afridge_preferences.xml
     <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
     <map>
     <string name="PREF_WARNING">3</string>
     <boolean name="PREF_EXP_DATE" value="false" />
     <string name="PREF_MEASURE">imperial</string>
     </map>
     */
    try {
      SharedPreferences sharedPreferences = application.getSharedPreferences("com.app.afridge_preferences", Context.MODE_PRIVATE);
      application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_EXP_DATE_WARNING, Integer.parseInt(sharedPreferences.getString("PREF_WARNING", "0")));
      application.prefStore.setBoolean(SharedPrefStore.Pref.SETTINGS_SHOW_NOTIFICATION, sharedPreferences.getBoolean("PREF_EXP_DATE", false));
      String measurementType = sharedPreferences.getString("PREF_MEASURE", "metric");
      assert measurementType != null;
      if (measurementType.equalsIgnoreCase("imperial")) {
        application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE, 1);
      }
      else {
        application.prefStore.setInt(SharedPrefStore.Pref.SETTINGS_MEASUREMENT_TYPE, 0);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
