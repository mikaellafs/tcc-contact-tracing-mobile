package pg.contact_tracing.domain.errors;

public class UserInformationNotFoundException extends Exception {
    String message;

    public UserInformationNotFoundException(String str) {
        message = str;
    }
    public String toString() {
        return ("User information not found: " + message);
    }
}
