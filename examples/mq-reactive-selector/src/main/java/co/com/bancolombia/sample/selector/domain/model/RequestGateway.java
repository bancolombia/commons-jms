package co.com.bancolombia.sample.selector.domain.model;

import reactor.core.publisher.Mono;

public interface RequestGateway {
    Mono<String> send(Request request);

    Mono<Result> getResult(String correlationId);
}
