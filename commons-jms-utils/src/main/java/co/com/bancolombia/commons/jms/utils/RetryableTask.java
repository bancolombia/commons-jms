package co.com.bancolombia.commons.jms.utils;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RetryableTask {
    private static final int MAX_ATTEMPTS = 10;
    private static final int INITIAL_INTERVAL_MILLIS = 1000;

    public static void runWithRetries(String name, Runnable runnable) {
        RetryConfig retryConfig = RetryConfig.custom()
                .intervalFunction(IntervalFunction.ofExponentialBackoff(INITIAL_INTERVAL_MILLIS))
                .maxAttempts(MAX_ATTEMPTS)
                .build();
        Retry.decorateRunnable(Retry.of(name, retryConfig), runnable).run();
    }
}
