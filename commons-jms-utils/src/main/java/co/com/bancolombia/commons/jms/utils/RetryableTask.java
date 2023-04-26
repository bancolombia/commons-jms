package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RetryableTask {

    public static void runWithRetries(String name, RetryableConfig retryableConfig, Runnable runnable) {
        RetryConfig retryConfig = RetryConfig.custom()
                .intervalFunction(IntervalFunction
                        .ofExponentialBackoff(retryableConfig.getInitialRetryIntervalMillis(), retryableConfig.getMultiplier()))
                .maxAttempts(retryableConfig.getMaxRetries())
                .build();
        Retry.decorateRunnable(Retry.of(name, retryConfig), runnable).run();
    }
}
