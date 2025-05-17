package ewm.exception;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
    }
}
