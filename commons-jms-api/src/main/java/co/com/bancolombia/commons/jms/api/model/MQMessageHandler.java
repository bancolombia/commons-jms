package co.com.bancolombia.commons.jms.api.model;

import jakarta.jms.Message;
import reactor.core.publisher.Mono;

public interface MQMessageHandler {
    Mono<Object> handleMessage(String sourceQueue, Message message);
}
