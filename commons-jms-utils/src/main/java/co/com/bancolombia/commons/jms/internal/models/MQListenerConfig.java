package co.com.bancolombia.commons.jms.internal.models;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import lombok.Builder;
import lombok.Getter;

import javax.jms.JMSContext;
import javax.jms.Queue;
import java.util.function.BiConsumer;

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
    private final BiConsumer<JMSContext, Queue> qmSetter = (ctx, queue) -> {}; //NOSONAR
}
