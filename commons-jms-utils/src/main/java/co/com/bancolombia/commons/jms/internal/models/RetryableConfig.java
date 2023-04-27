package co.com.bancolombia.commons.jms.internal.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class RetryableConfig {

    @Builder.Default
    private final int maxRetries = 10; //NOSONAR
    @Builder.Default
    private final int initialRetryIntervalMillis = 1000; //NOSONAR
    @Builder.Default
    private final double multiplier = 1.5; //NOSONAR

}
