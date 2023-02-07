package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;

public interface MQQueueCustomizer {
    void customize(Queue queue) throws JMSException;
}
