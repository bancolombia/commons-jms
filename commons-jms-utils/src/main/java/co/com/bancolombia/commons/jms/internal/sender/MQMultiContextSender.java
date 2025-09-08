package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import jakarta.jms.Destination;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@AllArgsConstructor
public class MQMultiContextSender implements MQMessageSender {
    private final MQMessageSenderSync senderSync; // MQMultiContextSenderSync

    @Override
    public Mono<String> send(String message) {
        return asyncSupplier(() -> senderSync.send(message));
    }

    @Override
    public Mono<String> send(MQMessageCreator messageCreator) {
        return asyncSupplier(() -> senderSync.send(messageCreator));
    }

    @Override
    public Mono<String> send(String destination, String message) {
        return asyncSupplier(() -> senderSync.send(destination, message));
    }

    @Override
    public Mono<String> send(String destination, MQMessageCreator messageCreator) {
        return asyncSupplier(() -> senderSync.send(destination, messageCreator));
    }

    @Override
    public Mono<String> send(Destination destination, String message) {
        return asyncSupplier(() -> senderSync.send(destination, message));
    }

    @Override
    public Mono<String> send(Destination destination, MQMessageCreator messageCreator) {
        return asyncSupplier(() -> senderSync.send(destination, messageCreator));
    }

    private <T> Mono<T> asyncSupplier(Supplier<T> supplier) {
        return Mono.fromSupplier(supplier)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
