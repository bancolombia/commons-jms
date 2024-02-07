package co.com.bancolombia.commons.jms.internal.models;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MQReqReplyConfig {
    @Builder.Default
    private final String requestQueue = ""; //NOSONAR
    @Builder.Default
    private final String replyQueue = ""; //NOSONAR
    @Builder.Default
    private final int concurrency = 1; //NOSONAR
    @Builder.Default
    private final String connectionFactory = ""; //NOSONAR
    @Builder.Default
    private final MQQueueCustomizer customizer = ignored -> { //NOSONAR
    };
    @Builder.Default
    private final MQQueueManagerSetter qmSetter = (ctx, queueName) -> {
    }; //NOSONAR

    @Builder.Default
    SelectorMode selectorMode = SelectorMode.CONTEXT_SHARED;

    @Builder.Default
    ReplyMode replyMode = ReplyMode.TEMPORARY_QUEUE;

    public enum SelectorMode {
        CONTEXT_SHARED,
        CONTEXT_PER_MESSAGE
    }

    public enum ReplyMode {
        SELECTOR,
        TEMPORARY_QUEUE
    }
}
