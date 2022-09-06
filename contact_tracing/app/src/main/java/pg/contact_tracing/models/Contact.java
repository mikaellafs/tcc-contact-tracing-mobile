package pg.contact_tracing.models;

public class Contact {
    private String id;
    private String token;
    private long timestamp;
    private long distance;

    // Extra info
    private int RSSI;
    private int battery_level;

    public Contact(String id, String token, long timestamp, long distance, int RSSI, int battery_level) {
        this.id = id;
        this.token = token;
        this.timestamp = timestamp;
        this.distance = distance;
        this.RSSI = RSSI;
        this.battery_level = battery_level;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDistance() {
        return distance;
    }

    public int getRSSI() {
        return RSSI;
    }

    public int getBattery_level() {
        return battery_level;
    }
}
