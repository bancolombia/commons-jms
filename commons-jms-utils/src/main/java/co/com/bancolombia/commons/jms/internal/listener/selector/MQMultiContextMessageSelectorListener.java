package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Log4j2
@AllArgsConstructor
public class MQMultiContextMessageSelectorListener implements MQMessageSelectorListener {
    private final MQMessageSelectorListenerSync listenerSync; // MQMultiContextMessageSelectorListenerSync

    @Override
    public Mono<Message> getMessage(String correlationId) {
        return doAsync(() -> listenerSync.getMessage(correlationId));
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout) {
        return doAsync(() -> listenerSync.getMessage(correlationId, timeout));
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout, Destination destination) {
        return doAsync(() -> listenerSync.getMessage(correlationId, timeout, destination));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector) {
        return doAsync(() -> listenerSync.getMessageBySelector(selector));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout) {
        return doAsync(() -> listenerSync.getMessageBySelector(selector, timeout));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout, Destination destination) {
        return doAsync(() -> listenerSync.getMessageBySelector(selector, timeout, destination));
    }

    private Mono<Message> doAsync(Supplier<Message> supplier) {
        return Mono.fromSupplier(supplier)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
