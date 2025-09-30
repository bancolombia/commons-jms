package co.com.bancolombia.commons.jms.api;

import co.com.bancolombia.commons.jms.api.exceptions.InvalidUsageException;
import jakarta.jms.Destination;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface MQRequestReplyBase<T> {
    Mono<T> requestReply(String message);

    Mono<T> requestReply(MQMessageCreator messageCreator);

    Mono<T> requestReply(String message, Duration timeout);

    Mono<T> requestReply(MQMessageCreator messageCreator, Duration timeout);

    Mono<T> requestReply(String message, Destination request, Duration timeout);

    Mono<T> requestReply(MQMessageCreator messageCreator, Destination request, Duration timeout);

    default Mono<T> requestReply(String message, Destination request, Destination reply, Duration timeout) {
        return Mono.error(() -> new InvalidUsageException("This method is not supported"));
    } // For fixed queues only

    default Mono<T> requestReply(MQMessageCreator messageCreator, Destination request, Destination reply,
                                 Duration timeout) {
        return Mono.error(() -> new InvalidUsageException("This method is not supported"));
    } // For fixed queues only
}
