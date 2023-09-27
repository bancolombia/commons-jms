package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Destination;
import jakarta.jms.Message;
import reactor.core.publisher.Mono;

public interface MQMessageSelectorListener {
    Mono<Message> getMessage(String correlationId);

    Mono<Message> getMessage(String correlationId, long timeout);

    Mono<Message> getMessage(String correlationId, long timeout, Destination destination);

    Mono<Message> getMessageBySelector(String selector);

    Mono<Message> getMessageBySelector(String selector, long timeout);

    Mono<Message> getMessageBySelector(String selector, long timeout, Destination destination);
}
