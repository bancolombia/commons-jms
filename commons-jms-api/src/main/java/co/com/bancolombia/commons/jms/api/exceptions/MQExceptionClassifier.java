package co.com.bancolombia.commons.jms.api.exceptions;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;

public interface MQExceptionClassifier {
    boolean isReconnectable(JMSRuntimeException e);

    boolean isReconnectable(JMSException e);
}
