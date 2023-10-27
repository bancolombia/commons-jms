package co.com.bancolombia.commons.jms.internal.listener.selector.strategy;

import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class ContextSharedStrategy implements SelectorStrategy {
    private final JMSContext context;

    @Override
    public Message getMessageBySelector(String selector, long timeout, Destination destination) {
        try (JMSConsumer consumer = context.createConsumer(destination, selector)) {
            log.info("Waiting message with selector {}", selector);
            Message message = consumer.receive(timeout);
            if (message == null) {
                throw new ReceiveTimeoutException("Message not received in " + timeout);
            }
            return message;
        }
    }
}
