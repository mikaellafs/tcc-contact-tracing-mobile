package pg.contact_tracing.utils;

import android.content.ContentValues;

import pg.contact_tracing.datasource.local.SQLiteContactsStorageStrings;
import pg.contact_tracing.models.Contact;

public class ContactAdapter {
    public static Contact toDomain(ContentValues values) {
        return new Contact(
                values.getAsInteger(SQLiteContactsStorageStrings.ID_COL),
                values.getAsString(SQLiteContactsStorageStrings.TOKEN_COL),
                values.getAsLong(SQLiteContactsStorageStrings.FIRST_TIMESTAMP_COL),
                values.getAsLong(SQLiteContactsStorageStrings.LAST_TIMESTAMP_COL),
                values.getAsLong(SQLiteContactsStorageStrings.DISTANCE_COL),
                values.getAsInteger(SQLiteContactsStorageStrings.RSSI_COL),
                values.getAsInteger(SQLiteContactsStorageStrings.BATTERY_LEVEL_COL)
        );
    }

    public static ContentValues toTable(Contact contact) {
        ContentValues values = new ContentValues();
        if (contact.getId() > 0)
            values.put(SQLiteContactsStorageStrings.ID_COL, contact.getId());

        values.put(SQLiteContactsStorageStrings.TOKEN_COL, contact.getToken());
        values.put(SQLiteContactsStorageStrings.FIRST_TIMESTAMP_COL, contact.getFirstContactTimestamp());
        values.put(SQLiteContactsStorageStrings.LAST_TIMESTAMP_COL, contact.getLastContactTimestamp());
        values.put(SQLiteContactsStorageStrings.DISTANCE_COL, contact.getDistance());
        values.put(SQLiteContactsStorageStrings.RSSI_COL, contact.getRSSI());
        values.put(SQLiteContactsStorageStrings.BATTERY_LEVEL_COL, contact.getBatteryLevel());

        return values;
    }
}
