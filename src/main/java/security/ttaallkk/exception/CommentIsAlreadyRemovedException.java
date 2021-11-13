package security.ttaallkk.exception;

public class CommentIsAlreadyRemovedException extends RuntimeException{
    public CommentIsAlreadyRemovedException() {
    }

    public CommentIsAlreadyRemovedException(String message) {
        super(message);
    }

    public CommentIsAlreadyRemovedException(String message, Throwable cause) {
        super(message, cause);
    }
}
