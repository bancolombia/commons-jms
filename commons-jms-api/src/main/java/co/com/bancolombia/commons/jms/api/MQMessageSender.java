package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Destination;
import reactor.core.publisher.Mono;

public interface MQMessageSender {
    Mono<String> send(Destination destination, MQMessageCreator messageCreator);

    Mono<String> send(MQMessageCreator messageCreator);
}
