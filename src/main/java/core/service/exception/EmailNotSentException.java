package core.service.exception;

/**
 * Created by Adrian on 14/05/2015.
 */
public class EmailNotSentException extends Exception {

    public EmailNotSentException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public EmailNotSentException(String message) {
        super(message);
    }

    public EmailNotSentException() {
        super();
    }
}
