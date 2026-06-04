package co.com.bancolombia.commons.jms.internal.reconnect;

import co.com.bancolombia.commons.jms.api.exceptions.MQExceptionClassifier;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.utils.RetryableTask;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@SuperBuilder
public abstract class AbstractJMSReconnectable<T> implements ExceptionListener, Callable<T> {
    @Builder.Default
    private final Stats stats = new Stats();
    @Builder.Default
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    @Builder.Default
    private final AtomicBoolean connecting = new AtomicBoolean();
    @Builder.Default
    private final AtomicLong lastSuccess = new AtomicLong();
    private final MQHealthListener healthListener;
    private final RetryableConfig retryableConfig;
    private final MQExceptionClassifier exceptionClassifier;
    protected JMSContext context;

    @Getter
    private String process;

    protected abstract String name();

    protected abstract T self();

    protected abstract void connect();

    protected void disconnect() {
        if (context != null) {
            context.close();
        }
    }

    @Override
    public T call() {
        this.process = name();
        healthListener.onInit(process);
        log.info("Setup CommonsJMS shutdown for {}", process);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, process + "-down"));
        return start();
    }

    protected T start() {
        long handled = System.currentTimeMillis();
        synchronized (this) {
            if (handled > lastSuccess.get()) {
                safeDisconnect();
                try {
                    stats.connections++;
                    connect();
                    markAsStarted();
                    return self();
                } catch (Exception e) {
                    safeDisconnect();
                    log.warn("Exception in {}", process, e);
                    throw e;
                }
            } else {
                return self();
            }
        }
    }

    public void onException(JMSRuntimeException exception) {
        onException(new JMSException(exception.getMessage(), exception.getErrorCode(),
                new Exception(exception.getCause())));
    }

    @Override
    public void onException(JMSException exception) {
        log.warn("MQ connection error {}", process, exception);
        if (!isReconnectable(exception)) {
            return;
        }
        stats.exceptions++;
        healthListener.onException(process, exception);
        if (!connecting.compareAndSet(false, true)) {
            log.warn("Reconnection already in progress for {}, exception ignored", process);
            return;
        }
        long handled = System.currentTimeMillis();
        service.submit(() -> {
            try {
                if (handled > lastSuccess.get()) {
                    reconnect();
                } else {
                    log.warn("Reconnection ignored because already reconnected for {}", process);
                }
            } finally {
                // Always release the gate so future exceptions can trigger a new reconnection.
                connecting.set(false);
            }
        });
    }

    protected void shutdown() {
        log.warn("Commencing graceful CommonsJMS shutdown for {}", process);
        safeDisconnect();
        log.warn("Graceful CommonsJMS shutdown completed for {}", process);
    }

    protected boolean isReconnectable(Exception ex) {
        if (ex instanceof JMSRuntimeException runtimeException) {
            return exceptionClassifier.isReconnectable(runtimeException);
        } else if (ex instanceof JMSException jmsException) {
            return exceptionClassifier.isReconnectable(jmsException);
        }
        return true;
    }

    private void reconnect() {
        Thread.currentThread().setName("reconnection-" + process);
        try {
            log.warn("Starting reconnection for {}", process);
            RetryableTask.runWithRetries(process, retryableConfig, this::start);
            log.warn("Reconnection successful for {}", process);
        } catch (RuntimeException ex) {
            log.warn("Reconnection error for {}", process, ex);
        }
    }

    private void safeDisconnect() {
        try {
            stats.disconnections++;
            disconnect();
        } catch (Exception e) {
            log.warn("Error disconnecting {}", process, e);
        }
    }

    private void markAsStarted() {
        stats.reconnections++;
        if (stats.reconnections % 10 == 0) {
            log.error("Connection started successfully, total reconnections: {}", stats.toString());
        }
        healthListener.onStarted(process);
        lastSuccess.set(System.currentTimeMillis());
    }

    @Data
    public static class Stats {
        private long connections;
        private long disconnections;
        private long reconnections;
        private long exceptions;
    }
}
