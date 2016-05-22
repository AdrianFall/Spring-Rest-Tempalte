package core.service.exception;

/**
 * Created by Adrian on 14/05/2015.
 */
public class DisabledAccountException extends Exception {
    public DisabledAccountException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DisabledAccountException(String message) {
        super(message);
    }

    public DisabledAccountException() {
        super();
    }
}
