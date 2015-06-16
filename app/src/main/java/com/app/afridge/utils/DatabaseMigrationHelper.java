package com.app.afridge.utils;

import com.app.afridge.FridgeApplication;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.NoteItem;
import com.app.afridge.dom.enums.ItemType;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * Database migration helper class - used to migrate old application
 * users to the new database schema, so they don't loose their data
 *
 * @author drakuwa
 */
public class DatabaseMigrationHelper extends SQLiteOpenHelper {

    /**
     * Set initial variables that will be used in the queries.
     */
    public static final String KEY_ROWID = "_id";// 0

    public static final String KEY_NAME = "name";// 1

    public static final String KEY_TYPE = "type";// 2

    public static final String KEY_QUANT = "quant";// 3

    public static final String KEY_QTYPE = "qtype";// 4

    public static final String KEY_DETAILS = "details";// 5

    public static final String KEY_ISEMPTY = "isEmpty";// 6

    public static final String KEY_EXPDATE = "exp_date";// 7

    public static final String KEY_NOTE = "note";// 1

    public static final String KEY_TIMESTAMP = "timestamp";// 1

    public static final String KEY_ITEM_ID = "item_id";// 2

    public static final String KEY_CHANGE = "change";// 9

    private static final String DATABASE_TABLE = "fridge";

    private static final String DATABASE_NOTES_TABLE = "notes";

    private static final String DATABASE_HISTORY_TABLE = "history";

    // The Android's default system path of your application database.
    private static String DB_PATH; // = "/data/data/com.app.afridge/databases/";

    // The database name
    private static String DB_NAME = "items.sqlite";

    private SQLiteDatabase db;

    private FridgeApplication application;

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     */
    @SuppressWarnings("JavaDoc")
    public DatabaseMigrationHelper(FridgeApplication application) throws Exception {

        super(application.getApplicationContext(), DB_NAME, null, 4);

        DB_PATH = application.getDatabasePath(DB_NAME).getPath();
        this.application = application;
        boolean dbExist = checkDataBase();
        if (dbExist) {
            // do nothing
            Log.d(Log.TAG, "Database already exists.");
        } else {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new IOException("Error copying database");
            }
        }
    }

    /**
     * Method that checks the existence of a local assets database in the given path
     * and migrates the values to the new schema
     */
    public void tryToMigrateDataBase() {

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.CREATE_IF_NECESSARY);

            // migrate the items
            migrateItems();

            // migrate the items
            migrateNotes();

            // migrate the items
            migrateHistory();

            // close and delete the database
            closeDatabase();

            // set the preference
            application.prefStore.setBoolean(SharedPrefStore.Pref.HAS_MIGRATED, true);
        } catch (SQLiteException e) {
            e.printStackTrace();
            // application.prefStore.setBoolean(SharedPrefStore.Pref.HAS_MIGRATED, false);
            // even if the migration fails, we can't risk duplicating items...
            application.prefStore.setBoolean(SharedPrefStore.Pref.HAS_MIGRATED, true);
        }
    }

    private void migrateItems() {
        // get the items and copy into the new DB schema
        Cursor items = getItems();
        if (items != null && items.moveToFirst()) {
            do {
                if (items.getInt(items.getColumnIndex(KEY_ISEMPTY)) != 1
                        && !items.getString(2).toUpperCase(Locale.ENGLISH)
                        .equalsIgnoreCase("EMPTY")) {
                    // the item is not empty, migrate it
                    FridgeItem fridgeItem = new FridgeItem();
                    if (items.getString(1).length() > 0) {
                        fridgeItem.setName(items.getString(1));
                    } else {
                        fridgeItem.setName(items.getString(2).toUpperCase(Locale.ENGLISH));
                    }
                    fridgeItem.setDetails(items.getString(5));
                    fridgeItem.setQuantity(items.getString(3));
                    if (items.getString(7).length() > 0) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                        try {
                            fridgeItem.setExpirationDate(
                                    sdf.parse(items.getString(7)).getTime() / 1000);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (items.getString(2).equalsIgnoreCase("ajvar")) {
                        fridgeItem.setType(String.valueOf(ItemType.valueOf("PESTO").ordinal()));
                    } else {
                        fridgeItem.setType(String.valueOf(
                                ItemType.valueOf(items.getString(2).toUpperCase(Locale.ENGLISH))
                                        .ordinal()));
                    }
                    String quantityType = items.getString(4);
                    if (quantityType.length() > 0) {
                        if (quantityType.equalsIgnoreCase("Kilogram[s]") || quantityType
                                .equalsIgnoreCase("Pound[s]")) {
                            fridgeItem.setTypeOfQuantity(0);
                        } else if (quantityType.equalsIgnoreCase("Gram[s]") || quantityType
                                .equalsIgnoreCase("Ounce[s]")) {
                            fridgeItem.setTypeOfQuantity(1);
                        } else if (quantityType.equalsIgnoreCase("Liter[s]") || quantityType
                                .equalsIgnoreCase("Gallon[s]")) {
                            fridgeItem.setTypeOfQuantity(2);
                        } else if (quantityType.equalsIgnoreCase("Peace[s]")) {
                            fridgeItem.setTypeOfQuantity(3);
                        }
                    }
                    fridgeItem.setItemId(fridgeItem.hashCode());
                    fridgeItem.save();
                }
            } while (items.moveToNext());
            items.close();
            // remove old data
            deleteAllItems();
        }
    }

    private void migrateNotes() {
        // get the notes and copy into the new DB schema
        Cursor notes = getNotes();
        if (notes != null && notes.moveToFirst()) {
            do {
                NoteItem note = new NoteItem();
                note.setNote(notes.getString(1));
                note.setChecked(false);
                note.setTimestamp(Calendar.getInstance().getTimeInMillis() / 1000);
                note.save();
            } while (notes.moveToNext());
            notes.close();
            // remove old data
            deleteAllNotes();
        }
    }

    private void migrateHistory() {
        // get the notes and copy into the new DB schema
        Cursor history = getHistoryItems();
        if (history != null && history.moveToFirst()) {
            do {
                // TODO
            } while (history.moveToNext());
            history.close();
            // remove old data
            clearHistory();
        }
    }

    /**
     * Method that copies the database from the "assets" folder into the created
     * empty database in the default system path.
     */
    private void copyDataBase() throws IOException {

        InputStream myInput = application.getAssets().open(DB_NAME);
        Log.d(Log.TAG, "copyDataBase called: " + DB_NAME + " " + DB_PATH);

        OutputStream myOutput = new FileOutputStream(DB_PATH);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    /**
     * Method that checks the existence of a local database in the given path
     * and returns a boolean value.
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH;
            checkDB = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    /**
     * SQL query function that returns a cursor showing all the items from the
     * database.
     */
    private Cursor getItems() {

        return db.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_NAME,
                KEY_TYPE, KEY_QUANT, KEY_QTYPE, KEY_DETAILS, KEY_ISEMPTY,
                KEY_EXPDATE}, null, null, null, null, null);
    }

    private boolean deleteAllItems() {

        return db.delete(DATABASE_TABLE, KEY_ROWID + " like '%'", null) > 0;
    }

    /**
     * SQL query function that returns a cursor showing all the notes from the
     * database.
     */
    private Cursor getNotes() {

        return db.query(DATABASE_NOTES_TABLE, new String[]{KEY_ROWID,
                KEY_NOTE}, null, null, null, null, null);
    }

    private boolean deleteAllNotes() {

        return db.delete(DATABASE_NOTES_TABLE, KEY_ROWID + " like '%'", null) > 0;
    }

    /**
     * SQL query function that returns a cursor showing all the history notes
     * from the database.
     */
    private Cursor getHistoryItems() {

        return db.query(DATABASE_HISTORY_TABLE, new String[]{KEY_ROWID,
                        KEY_TIMESTAMP, KEY_ITEM_ID, KEY_NAME, KEY_TYPE, KEY_QUANT,
                        KEY_QTYPE, KEY_DETAILS, KEY_EXPDATE, KEY_CHANGE}, null, null,
                null, null, null);
    }

    private boolean clearHistory() {

        return db.delete(DATABASE_HISTORY_TABLE, KEY_ROWID + " like '%'", null) > 0;
    }

    private synchronized void closeDatabase() {

        if (db != null) {
            db.close();
        }

        // try to delete the old database file
        FileUtils.deleteFile(new File(DB_PATH));
        FileUtils.deleteFile(new File(DB_PATH + "-journal"));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
