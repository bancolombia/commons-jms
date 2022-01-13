package co.com.bancolombia.commons.jms.api;

import javax.jms.TemporaryQueue;

/**
 * @deprecated This class will be removed.
 * Use {@link MQQueuesContainer} class instead.
 */
@Deprecated
public interface MQTemporaryQueuesContainer {
    void registerTemporaryQueue(String alias, TemporaryQueue queue);

    TemporaryQueue get(String alias);
}
