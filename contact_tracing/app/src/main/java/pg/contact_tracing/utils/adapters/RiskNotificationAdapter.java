package pg.contact_tracing.utils.adapters;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import pg.contact_tracing.datasource.sqlite.SQLiteContactsStorageStrings;
import pg.contact_tracing.models.Contact;
import pg.contact_tracing.models.RiskNotification;

public class RiskNotificationAdapter {
    public static RiskNotification toDomain(ContentValues values) {
        return new RiskNotification(
                values.getAsInteger(SQLiteContactsStorageStrings.ID_COL),
                values.getAsLong(SQLiteContactsStorageStrings.DATE_COL),
                values.getAsInteger(SQLiteContactsStorageStrings.AMOUNT_PEOPLE_COL),
                values.getAsString(SQLiteContactsStorageStrings.MESSAGE_COL)
        );
    }

    public static ContentValues toTable(RiskNotification notification) {
        ContentValues values = new ContentValues();

        values.put(SQLiteContactsStorageStrings.ID_COL, notification.getId());
        values.put(SQLiteContactsStorageStrings.DATE_COL, notification.getDate());
        values.put(SQLiteContactsStorageStrings.AMOUNT_PEOPLE_COL, notification.getAmountOfPeople());
        values.put(SQLiteContactsStorageStrings.MESSAGE_COL, notification.getMessage());

        return values;
    }

    public static RiskNotification fromJSONObject(JSONObject notificationJSON) throws JSONException {
        return new RiskNotification(
            notificationJSON.getLong("date"),
            notificationJSON.getInt("amount"),
            notificationJSON.getString("message")
        );
    }
}

