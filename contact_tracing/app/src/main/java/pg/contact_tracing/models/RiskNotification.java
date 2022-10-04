package pg.contact_tracing.models;

import java.util.Date;

public class RiskNotification {
    private int id = 1;
    private long date;
    private int amountOfPeople;
    private String message;

    public RiskNotification(int id, long date, int amountOfPeople, String message) {
        this.id = id;
        this.date = date;
        this.amountOfPeople = amountOfPeople;
        this.message = message;
    }

    public RiskNotification(long date, int amountOfPeople, String message) {
        this.date = date;
        this.amountOfPeople = amountOfPeople;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public int getAmountOfPeople() {
        return amountOfPeople;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RiskNotification(id: " + id + ", date: " + new Date(date)
                + ", peopleWithCovid: " + amountOfPeople + ", message: " + message;
    }
}
