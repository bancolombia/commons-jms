package co.com.bancolombia.sample.domain.model;

import reactor.core.publisher.Mono;

public interface RequestGateway {
    Mono<Result> doRequest(Request request);
}
