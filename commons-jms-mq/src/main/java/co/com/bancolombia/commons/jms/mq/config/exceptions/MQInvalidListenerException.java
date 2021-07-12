package co.com.bancolombia.commons.jms.mq.config.exceptions;

public class MQInvalidListenerException extends RuntimeException {
    public MQInvalidListenerException(String message) {
        super(message);
    }
}
