package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Destination;
import jakarta.jms.Message;

public interface MQMessageSelectorListenerSync {
    Message getMessage(String correlationId);

    Message getMessage(String correlationId, long timeout);

    Message getMessage(String correlationId, long timeout, Destination destination);

    Message getMessageBySelector(String selector);

    Message getMessageBySelector(String selector, long timeout);

    Message getMessageBySelector(String selector, long timeout, Destination destination);
}
