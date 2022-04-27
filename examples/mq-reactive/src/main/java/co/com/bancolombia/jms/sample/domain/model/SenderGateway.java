package co.com.bancolombia.jms.sample.domain.model;

import reactor.core.publisher.Mono;

public interface SenderGateway {
    Mono<String> send(Request request);
}
