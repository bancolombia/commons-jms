package co.com.bancolombia.commons.jms.exceptions;

import javax.jms.JMSRuntimeException;

public class RelatedMessageNotFoundException extends JMSRuntimeException {
    public RelatedMessageNotFoundException(String correlationId) {
        super("Processor not found for correlationId: " + correlationId);
    }
}
