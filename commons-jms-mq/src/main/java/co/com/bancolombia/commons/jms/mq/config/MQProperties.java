package co.com.bancolombia.commons.jms.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "commons.jms")
public class MQProperties {
    public static final int DEFAULT_CONCURRENCY = 1;
    public static final int DEFAULT_MAX_RETRIES = 10;
    public static final int DEFAULT_INITIAL_RETRY_INTERVAL_MILLIS = 1000;
    public static final double DEFAULT_RETRY_MULTIPLIER = 1.5;
    private int outputConcurrency = DEFAULT_CONCURRENCY;
    private String outputQueue;
    private int inputConcurrency = DEFAULT_CONCURRENCY;
    private String inputQueue;
    private String inputQueueAlias;
    private boolean inputQueueSetQueueManager = false;
    private long producerTtl = 0;
    private boolean reactive = false;
    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int initialRetryIntervalMillis = DEFAULT_INITIAL_RETRY_INTERVAL_MILLIS;
    private double retryMultiplier = DEFAULT_RETRY_MULTIPLIER;
}
