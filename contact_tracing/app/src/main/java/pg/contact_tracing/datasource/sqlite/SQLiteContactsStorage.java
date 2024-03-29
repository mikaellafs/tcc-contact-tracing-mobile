package pg.contact_tracing.datasource.sqlite;

import static pg.contact_tracing.datasource.sqlite.SQLiteContactsStorageStrings.WHERE_BY_ID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class SQLiteContactsStorage extends SQLiteOpenHelper {
    private static String SQLITE_CONTACT_STORAGE_LOG = "SQLITE_CONTACT_STORAGE";
    public SQLiteContactsStorage(Context context, String DB_NAME, int DB_VERSION) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Create table " + SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME);
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Create table " + SQLiteContactsStorageStrings.NOTIFICATION_TABLE_NAME);

        db.execSQL(SQLiteContactsStorageStrings.createContactsTable());
        db.execSQL(SQLiteContactsStorageStrings.createNotificationTable());
    }

    public void addNewContact(ContentValues values) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Add new contact with values: " + values.toString());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<ContentValues> getContact(String selection, String[] selectionArgs, String sortBy, String groupBy, Integer limit) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Get contact with selection " + selection + " ordered by " + sortBy + " with limit of " + limit);

        SQLiteDatabase db = this.getWritableDatabase();

        String[] columns = SQLiteContactsStorageStrings.getContactsTableColumns();
        Cursor cursor = db.query(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, columns, selection,
                selectionArgs, groupBy, null, sortBy, limit.toString());

        ArrayList<ContentValues> contacts = parseContactsResults(cursor);
        db.close();

        return contacts;
    }

    public void updateContact(int id, ContentValues values) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Update contact of id " + id + " with values " + values.toString());

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, values, WHERE_BY_ID, new String[]{id + ""});
        db.close();
    }

    public int deleteContact(int id) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Delete contact id: " + id);

        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, WHERE_BY_ID, new String[]{id + ""});
        db.close();

        return deleted;
    }

    public void addNewNotification(ContentValues values) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Add new notification with values: " + values.toString());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(SQLiteContactsStorageStrings.NOTIFICATION_TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<ContentValues> getNotifications(String selection, String[] selectionArgs, Integer limit) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Get "+ limit +" notifications with selection: " + selection + " " + selectionArgs);

        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = SQLiteContactsStorageStrings.getNotificationsTableColumns();

        Cursor cursor = db.query(SQLiteContactsStorageStrings.NOTIFICATION_TABLE_NAME, columns, selection,
                selectionArgs, null, null, null, limit.toString());

        ArrayList<ContentValues> notifications = parseNotificationsResults(cursor);

        return notifications;
    }

    public int deleteNotification(int id) {
        Log.i(SQLITE_CONTACT_STORAGE_LOG, "Delete notification id: " + id);

        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(SQLiteContactsStorageStrings.NOTIFICATION_TABLE_NAME, WHERE_BY_ID, new String[]{id + ""});
        db.close();

        return deleted;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SQLiteContactsStorageStrings.NOTIFICATION_TABLE_NAME);
        onCreate(db);
    }

    private ArrayList<ContentValues> parseContactsResults(Cursor cursor) {
        ArrayList<ContentValues> contacts = new ArrayList<>();
        while(cursor.moveToNext()) {

            ContentValues values = new ContentValues();
            values.put(SQLiteContactsStorageStrings.ID_COL, cursor.getString(0));
            values.put(SQLiteContactsStorageStrings.TOKEN_COL, cursor.getString(1));
            values.put(SQLiteContactsStorageStrings.FIRST_TIMESTAMP_COL, cursor.getLong(2));
            values.put(SQLiteContactsStorageStrings.LAST_TIMESTAMP_COL, cursor.getLong(3));
            values.put(SQLiteContactsStorageStrings.DISTANCE_COL, cursor.getLong(4));
            values.put(SQLiteContactsStorageStrings.RSSI_COL, cursor.getInt(5));
            values.put(SQLiteContactsStorageStrings.BATTERY_LEVEL_COL, cursor.getInt(6));

            contacts.add(values);
        }

        return contacts;
    }

    private ArrayList<ContentValues> parseNotificationsResults(Cursor cursor) {
        ArrayList<ContentValues> notifications = new ArrayList<>();
        while(cursor.moveToNext()) {

            ContentValues values = new ContentValues();
            values.put(SQLiteContactsStorageStrings.ID_COL, cursor.getString(0));
            values.put(SQLiteContactsStorageStrings.AMOUNT_PEOPLE_COL, cursor.getLong(1));
            values.put(SQLiteContactsStorageStrings.MESSAGE_COL, cursor.getString(2));

            notifications.add(values);
        }

        return notifications;
    }
}