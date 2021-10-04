package security.ttaallkk.exception;

public class AuthenticatedFailureException extends RuntimeException{
    public AuthenticatedFailureException() {
    }

    public AuthenticatedFailureException(String message) {
        super(message);
    }

    public AuthenticatedFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
