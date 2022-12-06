package pg.contact_tracing.models;

import java.util.Date;

public class RiskNotification {
    private int id = 1;
    private int amountOfPeople;
    private String message;

    public RiskNotification(int id, int amountOfPeople, String message) {
        this.id = id;
        this.amountOfPeople = amountOfPeople;
        this.message = message;
    }

    public RiskNotification(int amountOfPeople, String message) {
        this.amountOfPeople = amountOfPeople;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmountOfPeople() {
        return amountOfPeople;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RiskNotification(id: " + id
                + ", peopleWithCovid: " + amountOfPeople + ", message: " + message;
    }
}
