package ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    public record ApiError(String status, String reason, String message, String timestamp) {
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Throwable e) {
        log.warn("500 {}", e.getMessage(), e);
        return new ApiError("Error ....", e.getMessage(), getStackTrace(e), LocalDateTime.now().toString());
    }

    @ExceptionHandler({InvalidRequestException.class, StatsServerUnavailable.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError onInvalidRequestException(final RuntimeException e) {
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError("Bad request", e.getMessage(), getStackTrace(e), LocalDateTime.now().toString());
    }

    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}
