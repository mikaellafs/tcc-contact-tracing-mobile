package pg.contact_tracing.utils.adapters;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import pg.contact_tracing.datasource.sqlite.SQLiteContactsStorageStrings;
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

    public static JSONObject toJSONObject(Contact contact) {
        JSONObject contactJson = new JSONObject();

        try {
            contactJson.put("token", contact.getToken());
            contactJson.put("firstContactTimestamp", contact.getFirstContactTimestamp());
            contactJson.put("lastContactTimestamp", contact.getLastContactTimestamp());
            contactJson.put("distance", contact.getDistance());
            contactJson.put("rssi", contact.getRSSI());
            contactJson.put("batteryLevel", contact.getBatteryLevel());
        } catch (JSONException e) {
            Log.e("CONTACTS_ADAPTER","Failed to parse contact as json");
        }

        return contactJson;
    }
}
