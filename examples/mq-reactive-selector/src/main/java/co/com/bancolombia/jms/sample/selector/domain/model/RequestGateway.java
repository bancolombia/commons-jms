package co.com.bancolombia.jms.sample.selector.domain.model;

import reactor.core.publisher.Mono;

public interface RequestGateway {
    Mono<String> send(Request request);

    Mono<Result> getResult(String correlationId);
}
