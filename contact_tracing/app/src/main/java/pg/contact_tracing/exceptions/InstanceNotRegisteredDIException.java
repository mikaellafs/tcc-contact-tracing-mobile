package pg.contact_tracing.exceptions;

public class InstanceNotRegisteredDIException extends Exception {
    String className;

    public InstanceNotRegisteredDIException(String className) {
        this.className = className;
    }

    @Override
    public String getMessage() {
        return ("Dependency Injection Container does not have the class registered: " + className);
    }
}
