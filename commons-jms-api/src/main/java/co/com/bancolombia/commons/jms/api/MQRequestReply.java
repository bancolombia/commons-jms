package co.com.bancolombia.commons.jms.api;

import reactor.core.publisher.Mono;

import jakarta.jms.Message;
import java.time.Duration;

public interface MQRequestReply {
    Mono<Message> requestReply(String message);

    Mono<Message> requestReply(String message, Duration timeout);

    Mono<Message> requestReply(MQMessageCreator messageCreator);

    Mono<Message> requestReply(MQMessageCreator messageCreator, Duration timeout);
}
