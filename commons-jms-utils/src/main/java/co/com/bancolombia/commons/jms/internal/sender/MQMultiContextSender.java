package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.jms.Destination;

@AllArgsConstructor
public class MQMultiContextSender implements MQMessageSender {
    private final MQMessageSenderSync senderSync; // MQMultiContextSenderSync

    @Override
    public Mono<String> send(MQMessageCreator messageCreator) {
        return Mono.defer(() -> Mono.just(senderSync.send(messageCreator)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> send(Destination destination, MQMessageCreator messageCreator) {
        return Mono.defer(() -> Mono.just(senderSync.send(destination, messageCreator)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
