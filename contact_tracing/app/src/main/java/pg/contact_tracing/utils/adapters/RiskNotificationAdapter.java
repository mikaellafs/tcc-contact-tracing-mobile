package pg.contact_tracing.utils.adapters;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pg.contact_tracing.datasource.sqlite.SQLiteContactsStorageStrings;
import pg.contact_tracing.models.Contact;
import pg.contact_tracing.models.RiskNotification;

public class RiskNotificationAdapter {
    public static RiskNotification toDomain(ContentValues values) {
        return new RiskNotification(
                values.getAsInteger(SQLiteContactsStorageStrings.ID_COL),
                values.getAsInteger(SQLiteContactsStorageStrings.AMOUNT_PEOPLE_COL),
                values.getAsString(SQLiteContactsStorageStrings.MESSAGE_COL)
        );
    }

    public static ContentValues toTable(RiskNotification notification) {
        ContentValues values = new ContentValues();

        values.put(SQLiteContactsStorageStrings.ID_COL, notification.getId());
        values.put(SQLiteContactsStorageStrings.AMOUNT_PEOPLE_COL, notification.getAmountOfPeople());
        values.put(SQLiteContactsStorageStrings.MESSAGE_COL, notification.getMessage());

        return values;
    }

    public static RiskNotification fromJSONObject(JSONObject notificationJSON) throws JSONException, ParseException {
        return new RiskNotification(
            notificationJSON.getInt("amount"),
            notificationJSON.getString("message")
        );
    }
}

