package ewm.exception;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String message, Exception exception) {
        super(message);
    }
}
