package co.com.bancolombia.commons.jms.api;

import reactor.core.publisher.Mono;

import jakarta.jms.Destination;

public interface MQMessageSender {
    Mono<String> send(Destination destination, MQMessageCreator messageCreator);

    Mono<String> send(MQMessageCreator messageCreator);
}
