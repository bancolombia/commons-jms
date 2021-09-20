package co.com.bancolombia.commons.jms.api.exceptions;

public class ReceiveTimeoutException extends RuntimeException {

    public ReceiveTimeoutException(String message) {
        super(message);
    }
}
