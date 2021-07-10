package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import lombok.Builder;

import javax.jms.*;

@Builder
public class MQContextListener implements Runnable {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQListenerConfig config;

    @Override
    public void run() {
        JMSContext context = connectionFactory.createContext();
        Destination destination = MQQueueUtils.setupFixedQueue(context, config);
        JMSConsumer consumer = context.createConsumer(destination);//NOSONAR
        consumer.setMessageListener(listener);
    }
}
