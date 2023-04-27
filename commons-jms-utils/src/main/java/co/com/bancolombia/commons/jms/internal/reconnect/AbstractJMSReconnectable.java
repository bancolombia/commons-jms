package co.com.bancolombia.commons.jms.internal.reconnect;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.utils.RetryableTask;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SuperBuilder
public abstract class AbstractJMSReconnectable<T> implements ExceptionListener, Callable<T> {
    private final MQHealthListener healthListener;
    private final RetryableConfig retryableConfig;

    @Getter
    private String process;

    protected abstract T connect();

    protected abstract String name();


    @Override
    public T call() {
        this.process = name();
        healthListener.onInit(process);
        T result = connect();
        markAsStarted();
        return result;
    }

    @Override
    public void onException(JMSException exception) {
        log.warn("MQ connection error {}", process, exception);
        healthListener.onException(process, exception);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            Thread.currentThread().setName("reconnection-" + process);
            try {
                log.warn("Starting reconnection for {}", process);
                RetryableTask.runWithRetries(process, retryableConfig, this::connect);
                markAsStarted();
                log.warn("Reconnection successful for {}", process);
            } catch (JMSRuntimeException ex) {
                log.warn("Reconnection error for {}", process, ex);
            }
            service.shutdown();
        });
    }

    private void markAsStarted() {
        healthListener.onStarted(process);
    }
}
