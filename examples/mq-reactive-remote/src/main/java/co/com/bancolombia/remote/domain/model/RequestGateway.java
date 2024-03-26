package co.com.bancolombia.remote.domain.model;

import reactor.core.publisher.Mono;

public interface RequestGateway {
    Mono<Result> doRequest(Request request);
}
