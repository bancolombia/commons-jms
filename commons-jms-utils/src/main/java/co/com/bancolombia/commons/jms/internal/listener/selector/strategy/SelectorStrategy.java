package co.com.bancolombia.commons.jms.internal.listener.selector.strategy;

import jakarta.jms.Destination;
import jakarta.jms.Message;

public interface SelectorStrategy {
    Message getMessageBySelector(String selector, long timeout, Destination destination);
}
