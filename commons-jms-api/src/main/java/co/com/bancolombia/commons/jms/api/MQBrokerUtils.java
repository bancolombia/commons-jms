package co.com.bancolombia.commons.jms.api;

import javax.jms.JMSContext;
import javax.jms.Queue;

public interface MQBrokerUtils {
    void setQueueManager(JMSContext context, Queue queue);
}
