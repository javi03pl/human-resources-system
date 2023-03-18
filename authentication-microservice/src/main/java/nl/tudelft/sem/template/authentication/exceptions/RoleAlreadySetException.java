package nl.tudelft.sem.template.authentication.exceptions;

@SuppressWarnings("PMD")
public class RoleAlreadySetException extends Exception {
    public RoleAlreadySetException() {
        super("The user already has this role");
    }
}
