package exception;

public class LogicalErrorException extends RuntimeException {
    public LogicalErrorException(String message) {
        super(message);
    }
}
