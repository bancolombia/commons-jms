package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;

@Log4j2
@SuperBuilder
public class MQContextListener extends AbstractJMSReconnectable<MQContextListener> {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQListenerConfig config;
    private final MQQueuesContainer container;
    private final MQBrokerUtils utils;

    @Override
    protected String name() {
        String[] parts = Thread.currentThread().getName().split("-");
        String finalName = "mq-listener-fixed-queue-" + parts[parts.length - 1] + "[" + config.getQueue() + "]";
        Thread.currentThread().setName(finalName);
        return finalName;
    }

    @Override
    protected MQContextListener connect() {
        log.info("Starting listener {}", getProcess());
        JMSContext context = connectionFactory.createContext();
        Destination destination = MQQueueUtils.setupFixedQueue(context, config);
        JMSConsumer consumer = context.createConsumer(destination);//NOSONAR
        container.registerQueue(config.getQueue(), (Queue) destination);
        utils.setQueueManager(context, (Queue) destination);
        consumer.setMessageListener(listener);
        context.setExceptionListener(this);
        log.info("Listener {} started successfully", getProcess());
        return this;
    }
}
