package co.com.bancolombia.commons.jms.api;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface MQRequestReplyBase<T> {
    Mono<T> requestReply(String message);

    Mono<T> requestReply(String message, Duration timeout);

    Mono<T> requestReply(MQMessageCreator messageCreator);

    Mono<T> requestReply(MQMessageCreator messageCreator, Duration timeout);
}
