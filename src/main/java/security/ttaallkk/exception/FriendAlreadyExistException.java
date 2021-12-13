package security.ttaallkk.exception;

public class FriendAlreadyExistException extends RuntimeException{
    public FriendAlreadyExistException() {
    }

    public FriendAlreadyExistException(String message) {
        super(message);
    }

    public FriendAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
