package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

public interface MQBrokerUtils {
    void setQueueManager(JMSContext context, Queue queue);
}
