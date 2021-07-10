package co.com.bancolombia.commons.jms.api;

import javax.jms.JMSException;
import javax.jms.Queue;

public interface MQQueueCustomizer {
    void customize(Queue queue) throws JMSException;
}
