package co.com.bancolombia.commons.jms.mq.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "commons.jms")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MQProperties {
    @Builder.Default
    public static final String DEFAULT_DOMAIN = "app";
    @Builder.Default
    public static final int DEFAULT_CONCURRENCY = 1;
    @Builder.Default
    public static final int DEFAULT_MAX_RETRIES = 10;
    @Builder.Default
    public static final int DEFAULT_INITIAL_RETRY_INTERVAL_MILLIS = 1000;
    @Builder.Default
    public static final double DEFAULT_RETRY_MULTIPLIER = 1.5;
    @Builder.Default
    private int outputConcurrency = DEFAULT_CONCURRENCY;
    @Builder.Default
    private int inputConcurrency = DEFAULT_CONCURRENCY;
    @Builder.Default
    private boolean inputQueueSetQueueManager = false;
    @Builder.Default
    private long producerTtl = 0;
    @Builder.Default
    private boolean reactive = false;
    @Builder.Default
    private int maxRetries = DEFAULT_MAX_RETRIES;
    @Builder.Default
    private int initialRetryIntervalMillis = DEFAULT_INITIAL_RETRY_INTERVAL_MILLIS;
    @Builder.Default
    private double retryMultiplier = DEFAULT_RETRY_MULTIPLIER;
    private String outputQueue;
    private String inputQueue;
}
