package pg.contact_tracing.datasource.local;

public class SQLiteContactsStorageStrings {
    public static String CONTACTS_TABLE_NAME = "Contacts";
    public static String ID_COL = "id";
    public static String TOKEN_COL = "token";
    public static String TIMESTAMP_COL = "timestamp";
    public static String DISTANCE_COL = "distance";
    public static String RSSI_COL = "rssi";
    public static String BATTERY_LEVEL_COL = "battery_level";

    public static String createContactsTable() {
        return "CREATE TABLE " + CONTACTS_TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TOKEN_COL + " TEXT,"
                + TIMESTAMP_COL + " TEXT,"
                + DISTANCE_COL + " TEXT,"
                + RSSI_COL + " TEXT,"
                + BATTERY_LEVEL_COL + "TEXT)";
    }

    public static String getContacts() {
        return "";
    }
}
