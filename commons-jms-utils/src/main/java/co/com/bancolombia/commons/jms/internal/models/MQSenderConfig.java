package co.com.bancolombia.commons.jms.internal.models;

import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import jakarta.jms.ConnectionFactory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MQSenderConfig {
    private final ConnectionFactory connectionFactory;
    private final MQDestinationProvider destinationProvider;
    private final MQHealthListener healthListener;
    private final RetryableConfig retryableConfig;
    private final MQProducerCustomizer producerCustomizer;
    private final int concurrency;
}
