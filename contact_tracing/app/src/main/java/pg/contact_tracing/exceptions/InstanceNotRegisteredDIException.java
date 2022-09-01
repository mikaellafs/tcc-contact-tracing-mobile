package pg.contact_tracing.exceptions;

public class InstanceNotRegisteredDIException extends Exception {
    String className;

    public InstanceNotRegisteredDIException(String className) {
        this.className = className;
    }
    public String toString() {
        return ("Dependency Ingection Container does not have the class registered: " + className);
    }
}
