package pg.contact_tracing.datasource.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import pg.contact_tracing.models.Contact;

public class SQLiteContactsStorage extends SQLiteOpenHelper {
    public SQLiteContactsStorage(Context context, String DB_NAME, int DB_VERSION) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLiteContactsStorageStrings.createContactsTable());
    }

    public void addNewContact(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<ContentValues> getContact(String selection, String[] selectionArgs, String groupBy, Integer limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] columns = SQLiteContactsStorageStrings.getColumns();
        Cursor cursor = db.query(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, columns, selection,
                selectionArgs, groupBy, null, SQLiteContactsStorageStrings.TIMESTAMP_COL, limit.toString());

        ArrayList<ContentValues> contacts = parseResults(cursor);
        db.close();

        return contacts;
    }

    public void updateContact(String id, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, values, SQLiteContactsStorageStrings.ID_COL + " = ?", new String[]{id});
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    private ArrayList<ContentValues> parseResults(Cursor cursor) {
        ArrayList<ContentValues> contacts = new ArrayList<>();
        while(cursor.moveToNext()) {

            ContentValues values = new ContentValues();
            values.put(SQLiteContactsStorageStrings.ID_COL, cursor.getString(0));
            values.put(SQLiteContactsStorageStrings.TOKEN_COL, cursor.getString(1));
            values.put(SQLiteContactsStorageStrings.TIMESTAMP_COL, cursor.getLong(2));
            values.put(SQLiteContactsStorageStrings.DISTANCE_COL, cursor.getLong(3));
            values.put(SQLiteContactsStorageStrings.RSSI_COL, cursor.getInt(4));
            values.put(SQLiteContactsStorageStrings.BATTERY_LEVEL_COL, cursor.getInt(5));

            contacts.add(values);
        }

        return contacts;
    }
}