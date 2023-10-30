package co.com.bancolombia.commons.jms.internal.reconnect;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.utils.RetryableTask;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@SuperBuilder
public abstract class AbstractJMSReconnectable<T> implements ExceptionListener, Callable<T> {
    private final MQHealthListener healthListener;
    private final RetryableConfig retryableConfig;
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    protected final AtomicLong lastSuccess = new AtomicLong();

    @Getter
    private String process;

    protected abstract T connect();

    protected abstract void disconnect();

    protected abstract String name();


    @Override
    public T call() {
        this.process = name();
        healthListener.onInit(process);
        return start();
    }

    protected T start() {
        try {
            this.disconnect();
        } catch (Exception e) {
            log.info("Error disconnecting but ignore it because is in reconnection process", e);
        }
        try {
            T result = connect();
            markAsStarted();
            return result;
        } catch (Exception e) {
            log.warn("Exception in {}", process, e);
            throw e;
        }
    }

    public void onException(JMSRuntimeException exception) {
        onException(new JMSException(exception.getMessage(), exception.getErrorCode(),
                new Exception(exception.getCause())));
    }

    @Override
    public void onException(JMSException exception) {
        log.warn("MQ connection error {}", process, exception);
        healthListener.onException(process, exception);
        long handled = System.currentTimeMillis();
        service.submit(() -> {
            if (handled > lastSuccess.get()) {
                this.reconnect();
            } else {
                log.warn("Reconnection ignored because already reconnected");
            }
        });
    }

    private void reconnect() {
        Thread.currentThread().setName("reconnection-" + process);
        try {
            log.warn("Starting reconnection for {}", process);
            RetryableTask.runWithRetries(process, retryableConfig, this::start);
            log.warn("Reconnection successful for {}", process);
        } catch (JMSRuntimeException ex) {
            log.warn("Reconnection error for {}", process, ex);
        }
    }

    private void markAsStarted() {
        healthListener.onStarted(process);
        lastSuccess.set(System.currentTimeMillis());
    }
}
