package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import lombok.experimental.SuperBuilder;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;

@SuperBuilder
public class MQContextMessageSelectorListenerSync extends AbstractJMSReconnectable<MQContextMessageSelectorListenerSync> implements MQMessageSelectorListenerSync {
    public static final long DEFAULT_TIMEOUT = 5000L;
    private final ConnectionFactory connectionFactory;
    private final MQListenerConfig config;
    private Destination destination;
    private JMSContext context;

    @Override
    protected String name() {
        String[] parts = this.toString().split("\\.");
        return parts[parts.length - 1];
    }

    @Override
    protected MQContextMessageSelectorListenerSync connect() {
        context = connectionFactory.createContext();
        context.setExceptionListener(this);
        destination = MQQueueUtils.setupFixedQueue(context, config);
        return this;
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
