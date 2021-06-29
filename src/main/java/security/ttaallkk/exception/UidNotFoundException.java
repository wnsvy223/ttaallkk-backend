package security.ttaallkk.exception;

public class UidNotFoundException extends RuntimeException{
    public UidNotFoundException() {
    }

    public UidNotFoundException(String message) {
        super(message);
    }

    public UidNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
