package security.ttaallkk.exception;

public class UidNotMatchedException extends RuntimeException{
    public UidNotMatchedException() {
    }

    public UidNotMatchedException(String message) {
        super(message);
    }

    public UidNotMatchedException(String message, Throwable cause) {
        super(message, cause);
    }
}
