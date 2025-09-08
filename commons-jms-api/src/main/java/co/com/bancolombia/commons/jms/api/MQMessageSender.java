package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Destination;
import reactor.core.publisher.Mono;

public interface MQMessageSender {
    Mono<String> send(String message);

    Mono<String> send(MQMessageCreator messageCreator);

    Mono<String> send(String destination, String message);

    Mono<String> send(String destination, MQMessageCreator messageCreator);

    Mono<String> send(Destination destination, String message);

    Mono<String> send(Destination destination, MQMessageCreator messageCreator);
}
