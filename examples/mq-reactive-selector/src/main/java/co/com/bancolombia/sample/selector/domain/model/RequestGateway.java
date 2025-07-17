package co.com.bancolombia.sample.selector.domain.model;

import reactor.core.publisher.Mono;

public interface RequestGateway {
    Mono<Result> send(Request request);
}
