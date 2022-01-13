package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import lombok.Builder;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.MessageListener;
import javax.jms.Queue;

@Builder
public class MQContextListener implements Runnable {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQListenerConfig config;
    private final MQQueuesContainer container;
    private final MQBrokerUtils utils;

    @Override
    public void run() {
        JMSContext context = connectionFactory.createContext();
        Destination destination = MQQueueUtils.setupFixedQueue(context, config);
        JMSConsumer consumer = context.createConsumer(destination);//NOSONAR
        container.registerQueue(config.getQueue(), (Queue) destination);
        utils.setQueueManager(context, (Queue) destination);
        consumer.setMessageListener(listener);
    }
}
