package vn.nhom6.registration.exception;

/** A business rejection that the UI may display without changing data. */
public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }
}
