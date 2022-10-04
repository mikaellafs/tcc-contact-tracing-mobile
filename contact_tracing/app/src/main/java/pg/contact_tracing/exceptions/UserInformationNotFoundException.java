package pg.contact_tracing.exceptions;

public class UserInformationNotFoundException extends Exception {
    String message;

    public UserInformationNotFoundException(String str) {
        message = str;
    }

    @Override
    public String getMessage() {
        return ("User information not found: " + message);
    }
}
