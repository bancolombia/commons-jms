package co.com.bancolombia.commons.jms.internal.models;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MQListenerConfig {
    @Builder.Default
    private final String queue = "";
    @Builder.Default
    private final int concurrency = 1;
    @Builder.Default
    private final String connectionFactory = "";
    @Builder.Default
    private final String tempQueueAlias = "";
    @Builder.Default
    private final MQQueueCustomizer customizer = ignored -> {
    };
}
