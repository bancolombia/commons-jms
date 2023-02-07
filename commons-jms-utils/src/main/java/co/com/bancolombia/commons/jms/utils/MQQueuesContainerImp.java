package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;

import jakarta.jms.Queue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MQQueuesContainerImp implements MQQueuesContainer {
    private final Map<String, Queue> tempQueues = new ConcurrentHashMap<>();

    public void registerQueue(String alias, Queue queue) {
        tempQueues.put(alias, queue);
    }

    public Queue get(String alias) {
        return tempQueues.get(alias);
    }
}
