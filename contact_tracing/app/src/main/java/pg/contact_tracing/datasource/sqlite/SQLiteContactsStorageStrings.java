package pg.contact_tracing.datasource.sqlite;

public class SQLiteContactsStorageStrings {
    public static String CONTACTS_TABLE_NAME = "Contacts";
    public static String ID_COL = "id";
    public static String TOKEN_COL = "token";
    public static String FIRST_TIMESTAMP_COL = "first_timestamp";
    public static String LAST_TIMESTAMP_COL = "last_timestamp";
    public static String DISTANCE_COL = "distance";
    public static String RSSI_COL = "rssi";
    public static String BATTERY_LEVEL_COL = "battery_level";

    public static String ORDER_BY_FIRST_CONTACT_ASC = FIRST_TIMESTAMP_COL + " ASC";
    public static String ORDER_BY_LAST_CONTACT_DESC = LAST_TIMESTAMP_COL + " DESC";

    public static String[] getColumns() {
        return new String[]{ID_COL, TOKEN_COL, FIRST_TIMESTAMP_COL, LAST_TIMESTAMP_COL, DISTANCE_COL, RSSI_COL, BATTERY_LEVEL_COL};
    }

    public static String WHERE_BY_ID = SQLiteContactsStorageStrings.ID_COL + " = ?";

    public static String createContactsTable() {
        return "CREATE TABLE " + CONTACTS_TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TOKEN_COL + " TEXT,"
                + FIRST_TIMESTAMP_COL + " REAL,"
                + LAST_TIMESTAMP_COL + " REAL,"
                + DISTANCE_COL + " REAL,"
                + RSSI_COL + " INTEGER,"
                + BATTERY_LEVEL_COL + " REAL)";
    }
}
