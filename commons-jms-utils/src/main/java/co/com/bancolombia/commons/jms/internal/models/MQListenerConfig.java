package co.com.bancolombia.commons.jms.internal.models;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
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
}
