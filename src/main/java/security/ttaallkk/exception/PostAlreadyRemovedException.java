package security.ttaallkk.exception;

public class PostAlreadyRemovedException extends RuntimeException{
    public PostAlreadyRemovedException() {
    }

    public PostAlreadyRemovedException(String message) {
        super(message);
    }

    public PostAlreadyRemovedException(String message, Throwable cause) {
        super(message, cause);
    }
}
