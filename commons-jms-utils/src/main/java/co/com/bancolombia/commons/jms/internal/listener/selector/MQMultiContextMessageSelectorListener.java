package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Supplier;

import static co.com.bancolombia.commons.jms.internal.listener.selector.MQContextMessageSelectorListenerSync.DEFAULT_TIMEOUT;

@Log4j2
public class MQMultiContextMessageSelectorListener implements MQMessageSelectorListener {
    private final MQMessageSelectorListenerSync listenerSync; // MQMultiContextMessageSelectorListenerSync
    private final Scheduler scheduler;

    public MQMultiContextMessageSelectorListener(MQMessageSelectorListenerSync listenerSync,
                                                 MQExecutorService executorService) {
        this.listenerSync = listenerSync;
        this.scheduler = Schedulers.fromExecutor(executorService);
    }

    @Override
    public Mono<Message> getMessage(String correlationId) {
        return doAsync(() -> listenerSync.getMessage(correlationId), DEFAULT_TIMEOUT);
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout) {
        return doAsync(() -> listenerSync.getMessage(correlationId, timeout), timeout);
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout, Destination destination) {
        return doAsync(() -> listenerSync.getMessage(correlationId, timeout, destination), timeout);
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector) {
        return doAsync(() -> listenerSync.getMessageBySelector(selector), DEFAULT_TIMEOUT);
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout) {
        return doAsync(() -> listenerSync.getMessageBySelector(selector, timeout), timeout);
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout, Destination destination) {
        return doAsync(() -> listenerSync.getMessageBySelector(selector, timeout, destination), timeout);
    }

    private Mono<Message> doAsync(Supplier<Message> supplier, long timeout) {
        return Mono.fromSupplier(supplier)
                .subscribeOn(scheduler)
                .timeout(Duration.ofMillis(timeout)); // Enforces timeout to ensure scheduler thread is released
    }
}
