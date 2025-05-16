package exception;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ManagerSaveException(final String message) {
        super(message);
    }
}
