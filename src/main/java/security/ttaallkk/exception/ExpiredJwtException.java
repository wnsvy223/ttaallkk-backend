package security.ttaallkk.exception;

public class ExpiredJwtException extends RuntimeException{
    public ExpiredJwtException(String message) {
        super(message);
    }
}
