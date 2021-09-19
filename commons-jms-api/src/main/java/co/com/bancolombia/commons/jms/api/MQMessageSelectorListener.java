package co.com.bancolombia.commons.jms.api;

import reactor.core.publisher.Mono;

import javax.jms.Destination;
import javax.jms.Message;

public interface MQMessageSelectorListener {
    Mono<Message> getMessage(String correlationId);

    Mono<Message> getMessage(String correlationId, long timeout, Destination destination);
}
