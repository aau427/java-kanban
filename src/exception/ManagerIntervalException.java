package exception;

public class ManagerIntervalException extends RuntimeException {
    public ManagerIntervalException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ManagerIntervalException(final String message) {
        super(message);
    }
}
