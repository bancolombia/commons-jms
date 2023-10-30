package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import jakarta.jms.Queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MQQueuesContainerImp implements MQQueuesContainer {
    private final Map<String, Queue> tempQueues = new ConcurrentHashMap<>();
    private final Map<String, List<Queue>> tempQueueGroups = new ConcurrentHashMap<>();

    public void registerQueue(String alias, Queue queue) {
        tempQueues.put(alias, queue);
    }

    @Override
    public void registerToQueueGroup(String groupId, Queue queue) {
        tempQueueGroups.computeIfAbsent(groupId, _key -> Collections.synchronizedList(new ArrayList<>()))
                .add(queue);
    }

    @Override
    public void unregisterFromQueueGroup(String groupId, Queue queue) {
        List<Queue> group = tempQueueGroups.get(groupId);
        if (group != null) {
            group.remove(queue);
        }
    }

    public Queue get(String alias) {
        List<Queue> queues = tempQueueGroups.get(alias);
        if (queues != null) {
            int selectIndex = (int) (System.currentTimeMillis() % queues.size());
            return queues.get(selectIndex);
        } else {
            return tempQueues.get(alias);
        }
    }

    @Override
    public String toString() {
        return "MQQueuesContainerImp{" +
                "tempQueues=" + tempQueues +
                ", tempQueueGroups=" + tempQueueGroups +
                '}';
    }
}
