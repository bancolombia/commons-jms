package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
    protected void disconnect() throws JMSException {
        if (context != null) {
            log.warn("STOP: status {}", context.getMetaData());
            context.stop();
            context.close();
        }
    }

    @Override
    protected MQContextMessageSelectorListenerSync connect() {
        log.info("Starting listener {}", getProcess());
        context = connectionFactory.createContext();
        context.setExceptionListener(this);
        destination = MQQueueUtils.setupFixedQueue(context, config);
        log.info("Listener {} started successfully", getProcess());
        return this;
    }

    public Message getMessage(String correlationId) {
        return getMessageBySelector(buildSelector(correlationId));
    }

    @Override
    public Message getMessage(String correlationId, long timeout) {
        return getMessageBySelector(buildSelector(correlationId), timeout);
    }

    @Override
    public Message getMessage(String correlationId, long timeout, Destination destination) {
        return getMessageBySelector(buildSelector(correlationId), timeout, destination);
    }

    public Message getMessageBySelector(String selector) {
        return getMessageBySelector(selector, DEFAULT_TIMEOUT, destination);
    }

    @Override
    public Message getMessageBySelector(String selector, long timeout) {
        return getMessageBySelector(selector, timeout, destination);
    }

    public Message getMessageBySelector(String selector, long timeout, Destination destination) {
        try (JMSConsumer consumer = context.createConsumer(destination, selector)) {
            log.info("Waiting");
            Message message = consumer.receive(timeout);
            if (message == null) {
                throw new ReceiveTimeoutException("Message not received in " + timeout);
            }
            return message;
        } catch (JMSRuntimeException e) {
            // Connection is broken
            if (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains("CONNECTION_BROKEN")) {
                onException(e); // Handle for reconnection
            }
            throw e;
        }
    }

    private String buildSelector(String correlationId) {
        return "JMSCorrelationID='" + correlationId + "'";
    }

}
