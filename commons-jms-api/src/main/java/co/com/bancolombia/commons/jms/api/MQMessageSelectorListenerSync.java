package co.com.bancolombia.commons.jms.api;

import javax.jms.Destination;
import javax.jms.Message;

public interface MQMessageSelectorListenerSync {
    Message getMessage(String correlationId);

    Message getMessage(String correlationId, long timeout, Destination destination);
}
