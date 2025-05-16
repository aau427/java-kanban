package exception;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ManagerLoadException(final String message) {
        super(message);
    }
}