package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;
import lombok.AllArgsConstructor;

import javax.jms.TemporaryQueue;

/**
 * @deprecated This class will be removed.
 * Use {@link MQQueuesContainer} class instead.
 */
@Deprecated
@AllArgsConstructor
public class MQTemporaryQueuesContainerImp implements MQTemporaryQueuesContainer {
    private final MQQueuesContainer container;

    public void registerTemporaryQueue(String alias, TemporaryQueue queue) {
        container.registerQueue(alias, queue);
    }

    public TemporaryQueue get(String alias) {
        return (TemporaryQueue) container.get(alias);
    }
}
