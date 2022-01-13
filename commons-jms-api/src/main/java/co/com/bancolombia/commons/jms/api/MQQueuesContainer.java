package co.com.bancolombia.commons.jms.api;

import javax.jms.Queue;

public interface MQQueuesContainer {
    void registerQueue(String key, Queue queue);

    Queue get(String alias);
}
