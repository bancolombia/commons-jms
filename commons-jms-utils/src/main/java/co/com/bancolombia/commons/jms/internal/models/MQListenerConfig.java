package co.com.bancolombia.commons.jms.internal.models;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageListener;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MQListenerConfig {
    private final ConnectionFactory connectionFactory;
    private final int concurrency;
    private final MessageListener messageListener;
    private final MQQueueCustomizer queueCustomizer;
    private final String listeningQueue;
    @Builder.Default
    private final QueueType queueType = QueueType.FIXED; //NOSONAR
    @Builder.Default
    private final int maxRetries = -1; //NOSONAR
    @Builder.Default
    private final MQQueueManagerSetter qmSetter = (ctx, queueName) -> {
    }; //NOSONAR
    @Builder.Default
    private final SelectorMode selectorMode = SelectorMode.CONTEXT_SHARED;

    public enum SelectorMode {
        CONTEXT_SHARED,
        CONTEXT_PER_MESSAGE
    }

    public enum QueueType {
        FIXED,
        TEMPORARY
    }
}
