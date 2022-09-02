package pg.contact_tracing.models;

public class Contact {
    String id;
    String token;
    long timestamp;
    long distance;

    // Extra info
    int RSSI;
    int battery_level;

    public Contact(String token, long timestamp, long distance, int RSSI, int battery_level) {
        this.id = "293iujdeiw384";
        this.token = token;
        this.timestamp = timestamp;
        this.distance = distance;
        this.RSSI = RSSI;
        this.battery_level = battery_level;
    }
}
