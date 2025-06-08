package exception;

public class NotSupportedMethodException extends RuntimeException {
    public NotSupportedMethodException(final String message) {
        super(message);
    }
}
