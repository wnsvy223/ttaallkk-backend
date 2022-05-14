package security.ttaallkk.exception;

public class InvalidFileMimeTypeException extends RuntimeException {

    public InvalidFileMimeTypeException() {
    }

    public InvalidFileMimeTypeException(String message) {
        super(message);
    }

    public InvalidFileMimeTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
