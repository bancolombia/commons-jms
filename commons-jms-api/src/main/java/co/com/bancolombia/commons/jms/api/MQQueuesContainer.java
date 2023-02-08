package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Queue;

public interface MQQueuesContainer {
    void registerQueue(String key, Queue queue);

    Queue get(String alias);
}
