package core.service.exception;

/**
 * Created by Adrian on 12/05/2015.
 */
public class EmailExistsException extends Exception {

    public EmailExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public EmailExistsException(String message) {
        super(message);
    }

    public EmailExistsException() {
        super();
    }
}
