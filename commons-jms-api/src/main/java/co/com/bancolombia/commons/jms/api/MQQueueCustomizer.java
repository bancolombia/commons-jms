package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;

public interface MQQueueCustomizer {
    void customize(Queue queue) throws JMSException;

    default MQQueueCustomizer andThen(MQQueueCustomizer after) {
        if (after == null) {
            return this;
        }
        return queue -> {
            this.customize(queue);
            after.customize(queue);
        };
    }
}
