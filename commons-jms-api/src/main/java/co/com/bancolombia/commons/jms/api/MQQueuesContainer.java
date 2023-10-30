package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Queue;

public interface MQQueuesContainer {
    void registerQueue(String key, Queue queue);
    void registerToQueueGroup(String groupId, Queue queue);
    void unregisterFromQueueGroup(String groupId, Queue queue);

    Queue get(String alias);
}
