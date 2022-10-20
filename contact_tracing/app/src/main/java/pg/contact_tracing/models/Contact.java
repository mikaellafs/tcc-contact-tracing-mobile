package pg.contact_tracing.models;

public class Contact {
    private int id;
    private String token;
    private long firstContactTimestamp;
    private long lastContactTimestamp;
    private double distance;

    // Extra info
    private int RSSI;
    private float batteryLevel;

    public Contact(String token, long timestamp, double distance, int RSSI, float battery_level) {
        this.id = -1;
        this.token = token;
        this.firstContactTimestamp = timestamp;
        this.lastContactTimestamp = timestamp;
        this.distance = distance;
        this.RSSI = RSSI;
        this.batteryLevel = battery_level;
    }

    public Contact(int id, String token, long firstTimestamp, long lastTimestamp, double distance, int RSSI, float battery_level) {
        this.id = id;
        this.token = token;
        this.firstContactTimestamp = firstTimestamp;
        this.lastContactTimestamp = lastTimestamp;
        this.distance = distance;
        this.RSSI = RSSI;
        this.batteryLevel = battery_level;
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public double getDistance() {
        return distance;
    }

    public int getRSSI() {
        return RSSI;
    }

    public long getFirstContactTimestamp() {
        return firstContactTimestamp;
    }

    public long getLastContactTimestamp() {
        return lastContactTimestamp;
    }

    public void setLastContactTimestamp(long lastContactTimestamp) {
        this.lastContactTimestamp = lastContactTimestamp;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public void undoFormatToken() {
        token = token.replace("-", "");
    }

    @Override
    public String toString() {
        return "- Contato \n\tid: " + id + "\n\ttoken: " + token + "\n\tdistance: " + distance +
                "\n\trssi: " + RSSI + "\n\tfirstContact: " + firstContactTimestamp + "\n\tlastContact: " + lastContactTimestamp +
                "\n\tbatteryLevel: " + batteryLevel;
    }
}
