package co.com.bancolombia.commons.jms.internal.models;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MQListenerConfig {
    @Builder.Default
    private final String queue = ""; //NOSONAR
    @Builder.Default
    private final int concurrency = 1; //NOSONAR
    @Builder.Default
    private final String connectionFactory = ""; //NOSONAR
    @Builder.Default
    private final String tempQueueAlias = ""; //NOSONAR
    @Builder.Default
    private final MQQueueCustomizer customizer = ignored -> { //NOSONAR
    };
    @Builder.Default
    private final int maxRetries = -1; //NOSONAR
    @Builder.Default
    private final MQQueueManagerSetter qmSetter = (ctx, queue) -> {
    }; //NOSONAR

    @Builder.Default
    SelectorMode selectorMode = SelectorMode.CONTEXT_SHARED;

    public enum SelectorMode {
        CONTEXT_SHARED,
        CONTEXT_PER_MESSAGE
    }
}
