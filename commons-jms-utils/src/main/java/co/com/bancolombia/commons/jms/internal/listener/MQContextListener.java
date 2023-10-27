package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SuperBuilder
public class MQContextListener extends AbstractJMSReconnectable<MQContextListener> {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQListenerConfig config;
    private final MQQueuesContainer container;
    private final MQBrokerUtils utils;
    private JMSConsumer consumer;
    private JMSContext context;

    @Override
    protected String name() {
        String[] parts = Thread.currentThread().getName().split("-");
        String finalName = "mq-listener-fixed-queue-" + parts[parts.length - 1] + "[" + config.getQueue() + "]";
        Thread.currentThread().setName(finalName);
        return finalName;
    }

    @Override
    protected void disconnect() throws JMSException {
        if (consumer != null) {
            consumer.close();
        }
        if (context != null) {
            context.close();
        }
    }

    @Override
    protected MQContextListener connect() {
        log.info("Starting listener {}", getProcess());
        context = connectionFactory.createContext();
        Destination destination = MQQueueUtils.setupFixedQueue(context, config);
        consumer = context.createConsumer(destination);//NOSONAR
        container.registerQueue(config.getQueue(), (Queue) destination);
        utils.setQueueManager(context, (Queue) destination);
        consumer.setMessageListener(listener);
        context.setExceptionListener(this);
        log.info("Listener {} started successfully", getProcess());
        return this;
    }
}
