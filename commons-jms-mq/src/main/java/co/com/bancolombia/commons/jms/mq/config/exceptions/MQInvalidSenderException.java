package co.com.bancolombia.commons.jms.mq.config.exceptions;

public class MQInvalidSenderException extends RuntimeException {
    public MQInvalidSenderException(String message) {
        super(message);
    }
}
