package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.TemporaryQueue;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SuperBuilder
public class MQContextListener extends AbstractJMSReconnectable<MQContextListener> {
    private final MQListenerConfig listenerConfig;
    private final MQQueuesContainer container;
    private final MQBrokerUtils utils;
    private JMSConsumer consumer;
    private JMSContext context;
    private TemporaryQueue tempQueue;

    @Override
    protected String name() {
        String[] parts = Thread.currentThread().getName().split("-");
        String finalName;
        if (listenerConfig.getQueueType() == MQListenerConfig.QueueType.TEMPORARY) {
            finalName = "mq-listener-tmp-queue-" + parts[parts.length - 1] + "[" + listenerConfig.getListeningQueue() + "]";
        } else {
            finalName = "mq-listener-fixed-queue-" + parts[parts.length - 1] + "[" + listenerConfig.getListeningQueue() + "]";
        }
        Thread.currentThread().setName(finalName);
        return finalName;
    }

    @Override
    protected void disconnect() {
        if (listenerConfig.getQueueType() == MQListenerConfig.QueueType.TEMPORARY) {
            container.unregisterFromQueueGroup(listenerConfig.getListeningQueue(), tempQueue);
        }
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception ignored) {
                // ignore because disconnection
            }
        }
        if (tempQueue != null) {
            try {
                tempQueue.delete();
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
    protected MQContextListener connect() {
        log.info("Starting listener {}", getProcess());
        context = listenerConfig.getConnectionFactory().createContext();
        context.setExceptionListener(this);
        if (listenerConfig.getQueueType() == MQListenerConfig.QueueType.TEMPORARY) {
            tempQueue = MQQueueUtils.setupTemporaryQueue(context, listenerConfig);
            container.registerToQueueGroup(listenerConfig.getListeningQueue(), tempQueue);
            consumer = context.createConsumer(tempQueue);//NOSONAR
            consumer.setMessageListener(listenerConfig.getMessageListener());
            log.info("Listener {} started successfully with queue: {}", getProcess(), shortDestinationName());
        } else {
            Destination destination = MQQueueUtils.setupFixedQueue(context, listenerConfig);
            utils.setQueueManager(context, (Queue) destination);
            container.registerQueue(listenerConfig.getListeningQueue(), (Queue) destination);
            consumer = context.createConsumer(destination);//NOSONAR
            consumer.setMessageListener(listenerConfig.getMessageListener());
            log.info("Listener {} started successfully with queue: {}", getProcess(), listenerConfig.getListeningQueue());
        }
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
