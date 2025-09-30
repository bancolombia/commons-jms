package co.com.bancolombia.samplebuilder.domain.model;

import reactor.core.publisher.Mono;

public interface RequestGateway {
    Mono<Result> doRequest(Request request);

    Mono<Result> doRequestTmp(Request request);
}
