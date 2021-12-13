package security.ttaallkk.exception;

public class FriendNotAllowSelfException extends RuntimeException{
    public FriendNotAllowSelfException() {
    }

    public FriendNotAllowSelfException(String message) {
        super(message);
    }

    public FriendNotAllowSelfException(String message, Throwable cause) {
        super(message, cause);
    }
}