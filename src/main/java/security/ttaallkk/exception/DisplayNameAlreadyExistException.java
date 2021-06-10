package security.ttaallkk.exception;

public class DisplayNameAlreadyExistException extends RuntimeException{
    public DisplayNameAlreadyExistException() {
    }

    public DisplayNameAlreadyExistException(String message) {
        super(message);
    }

    public DisplayNameAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}