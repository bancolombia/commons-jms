package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.ContextSharedStrategy;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorModeProvider;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorStrategy;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
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
    private final SelectorModeProvider selectorModeProvider;
    private SelectorStrategy strategy;
    private Destination destination;

    @Override
    protected String name() {
        String[] parts = this.toString().split("\\.");
        return parts[parts.length - 1];
    }

    @Override
    protected void disconnect() throws JMSException {
        // do not disconnect to avoid another thread exceptions
    }

    @Override
    protected MQContextMessageSelectorListenerSync connect() {
        long handled = System.currentTimeMillis();
        synchronized (this) {
            if (handled > lastSuccess.get()) {
                log.info("Starting listener {}", getProcess());
                JMSContext context = connectionFactory.createContext();
                context.setExceptionListener(this);
                destination = MQQueueUtils.setupFixedQueue(context, config);
                strategy = selectorModeProvider.get(connectionFactory, context);
                log.info("Listener {} started successfully", getProcess());
                lastSuccess.set(System.currentTimeMillis());
            } else {
                log.warn("Reconnection ignored because already connected");
            }
        }
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
        return getMessageBySelector(selector, timeout, destination, true);
    }

    protected Message getMessageBySelector(String selector, long timeout, Destination destination, boolean retry) {
        try {
            return strategy.getMessageBySelector(selector, timeout, destination);
        } catch (JMSRuntimeException e) {
            // Connection is broken
            if (strategy instanceof ContextSharedStrategy && e.getCause() != null && e.getCause().getMessage() != null
                    && e.getCause().getMessage().contains("CONNECTION_BROKEN")) {
                connect();
            }
            if (retry) {
                log.warn("Retrying because: {}", e.getMessage());
                return getMessageBySelector(selector, timeout, destination, false);
            } else {
                log.warn("Retry has failed with {}, this will rethrow", e.getMessage());
                throw e;
            }
        }
    }

    private String buildSelector(String correlationId) {
        return "JMSCorrelationID='" + correlationId + "'";
    }

}
