package pg.contact_tracing.utils;

import android.content.ContentValues;

import pg.contact_tracing.datasource.local.SQLiteContactsStorageStrings;
import pg.contact_tracing.models.Contact;

public class ContactAdapter {
    public static Contact toDomain(ContentValues values) {
        return new Contact(
                values.getAsString(SQLiteContactsStorageStrings.ID_COL),
                values.getAsString(SQLiteContactsStorageStrings.TOKEN_COL),
                values.getAsLong(SQLiteContactsStorageStrings.TIMESTAMP_COL),
                values.getAsLong(SQLiteContactsStorageStrings.DISTANCE_COL),
                values.getAsInteger(SQLiteContactsStorageStrings.RSSI_COL),
                values.getAsInteger(SQLiteContactsStorageStrings.BATTERY_LEVEL_COL)
        );
    }

    public static ContentValues toTable(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(SQLiteContactsStorageStrings.ID_COL, contact.getId());
        values.put(SQLiteContactsStorageStrings.TOKEN_COL, contact.getToken());
        values.put(SQLiteContactsStorageStrings.TIMESTAMP_COL, contact.getTimestamp());
        values.put(SQLiteContactsStorageStrings.DISTANCE_COL, contact.getDistance());
        values.put(SQLiteContactsStorageStrings.RSSI_COL, contact.getRSSI());
        values.put(SQLiteContactsStorageStrings.BATTERY_LEVEL_COL, contact.getBattery_level());

        return values;
    }
}
