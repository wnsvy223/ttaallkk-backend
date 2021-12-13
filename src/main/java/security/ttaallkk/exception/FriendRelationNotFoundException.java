package security.ttaallkk.exception;

public class FriendRelationNotFoundException extends RuntimeException{
    public FriendRelationNotFoundException() {
    }

    public FriendRelationNotFoundException(String message) {
        super(message);
    }

    public FriendRelationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
