package ewm.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
    }
}
