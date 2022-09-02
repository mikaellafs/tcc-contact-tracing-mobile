package pg.contact_tracing.datasource.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteContactsStorage extends SQLiteOpenHelper {
    public SQLiteContactsStorage(Context context, String DB_NAME, int DB_VERSION) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLiteContactsStorageStrings.createContactsTable());
    }

    public void addNewContact(String id, String token, long timestamp, long distance, int RSSI, int battery_level) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(SQLiteContactsStorageStrings.TOKEN_COL, token);
        values.put(SQLiteContactsStorageStrings.TIMESTAMP_COL, timestamp);
        values.put(SQLiteContactsStorageStrings.DISTANCE_COL, distance);
        values.put(SQLiteContactsStorageStrings.RSSI_COL, RSSI);
        values.put(SQLiteContactsStorageStrings.BATTERY_LEVEL_COL, battery_level);

        db.insert(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, null, values);
        db.close();
    }

    // Return a map or array of values, repository must use an adapter
    public void getContact(String selection, String[] selectionArgs, String groupBy, Integer limit) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] columns = {"preencher isso aqui corretamente"};
        Cursor cursor = db.query(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, columns, selection,
                selectionArgs, groupBy, null, null, limit.toString());

        while(cursor.moveToNext()) {
            // Passing values
            String column1 = cursor.getString(0);
            String column2 = cursor.getString(1);
            String column3 = cursor.getString(2);
            // Do something Here with values
        }
        db.close();
    }

    public void getContact(String query) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while(cursor.moveToNext()) {
            // Passing values
            String column1 = cursor.getString(0);
            String column2 = cursor.getString(1);
            String column3 = cursor.getString(2);
            // Do something Here with values
        }
        db.close();
    }

    public void updateContact(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        db.update(SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME, values, "id = ?", new String[]{id});

        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + SQLiteContactsStorageStrings.CONTACTS_TABLE_NAME);
        onCreate(db);
    }
}