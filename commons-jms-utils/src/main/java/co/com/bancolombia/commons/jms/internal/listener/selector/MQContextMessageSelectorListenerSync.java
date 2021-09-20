package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;

import javax.jms.*;

public class MQContextMessageSelectorListenerSync implements MQMessageSelectorListenerSync {
    public static final long DEFAULT_TIMEOUT = 5000L;
    private final JMSContext context;
    private final Destination destination;

    public MQContextMessageSelectorListenerSync(ConnectionFactory connectionFactory, MQListenerConfig config) {
        context = connectionFactory.createContext();
        destination = MQQueueUtils.setupFixedQueue(context, config);
    }

    public Message getMessage(String correlationId) {
        return getMessage(correlationId, DEFAULT_TIMEOUT, destination);
    }

    public Message getMessage(String correlationId, long timeout, Destination destination) {
        try (JMSConsumer consumer = context.createConsumer(destination, buildSelector(correlationId))) {
            Message message = consumer.receive(timeout);
            if (message == null) {
                throw new ReceiveTimeoutException("Message not received in " + timeout);
            }
            return message;
        }
    }

    private String buildSelector(String correlationId) {
        return "JMSCorrelationID='" + correlationId + "'";
    }

}
