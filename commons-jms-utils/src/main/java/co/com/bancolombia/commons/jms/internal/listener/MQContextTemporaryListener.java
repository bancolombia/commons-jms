package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;
import jakarta.jms.TemporaryQueue;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SuperBuilder
public class MQContextTemporaryListener extends AbstractJMSReconnectable<MQContextTemporaryListener> {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQListenerConfig config;
    private final MQQueuesContainer container;
    private JMSConsumer consumer;
    private JMSContext context;
    private TemporaryQueue tempQueue;

    @Override
    protected String name() {
        String[] parts = Thread.currentThread().getName().split("-");
        String finalName = "mq-listener-tmp-queue-" + parts[parts.length - 1] + "[" + config.getTempQueueAlias() + "]";
        Thread.currentThread().setName(finalName);
        return finalName;
    }

    @Override
    protected void disconnect() {
        container.unregisterFromQueueGroup(config.getTempQueueAlias(), tempQueue);
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception ignored) {
                // ignore because disconnection
            }
        }
        if (context != null) {
            try {
                context.close();
            } catch (Exception ignored) {
                // ignore because disconnection
            }
        }
    }

    @Override
    protected MQContextTemporaryListener connect() {
        log.info("Starting listener {}", getProcess());
        context = connectionFactory.createContext();
        context.setExceptionListener(this);
        tempQueue = MQQueueUtils.setupTemporaryQueue(context, config);
        consumer = context.createConsumer(tempQueue);//NOSONAR
        consumer.setMessageListener(listener);
        container.registerToQueueGroup(config.getTempQueueAlias(), tempQueue);
        log.info("Listener {} started successfully with queue: {}", getProcess(), shortDestinationName());
        return this;
    }

    private String shortDestinationName() {
        try {
            return tempQueue.getQueueName().split("\\?")[0];
        } catch (JMSException e) {
            log.warn("Error getting temp queue name", e);
            return "Error getting queue name";
        }
    }
}
