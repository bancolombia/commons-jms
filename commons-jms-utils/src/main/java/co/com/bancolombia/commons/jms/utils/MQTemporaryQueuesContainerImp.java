package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;

import javax.jms.TemporaryQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MQTemporaryQueuesContainerImp implements MQTemporaryQueuesContainer {
    private final Map<String, TemporaryQueue> tempQueues = new ConcurrentHashMap<>();

    public void registerTemporaryQueue(String alias, TemporaryQueue queue) {
        tempQueues.put(alias, queue);
    }

    public TemporaryQueue get(String alias) {
        return tempQueues.get(alias);
    }
}
