package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.jms.Destination;
import javax.jms.Message;

@AllArgsConstructor
public class MQMultiContextMessageSelectorListener implements MQMessageSelectorListener {
    private final MQMessageSelectorListenerSync listenerSync; // MQMultiContextMessageSelectorListenerSync

    @Override
    public Mono<Message> getMessage(String correlationId) {
        return Mono.defer(() -> Mono.just(listenerSync.getMessage(correlationId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout, Destination destination) {
        return Mono.defer(() -> Mono.just(listenerSync.getMessage(correlationId, timeout, destination)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
