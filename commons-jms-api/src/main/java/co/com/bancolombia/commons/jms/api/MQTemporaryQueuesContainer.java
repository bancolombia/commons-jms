package co.com.bancolombia.commons.jms.api;

import javax.jms.TemporaryQueue;

public interface MQTemporaryQueuesContainer {
    void registerTemporaryQueue(String alias, TemporaryQueue queue);

    TemporaryQueue get(String alias);
}
