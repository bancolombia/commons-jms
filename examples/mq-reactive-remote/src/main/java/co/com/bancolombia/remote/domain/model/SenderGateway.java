package co.com.bancolombia.remote.domain.model;

import reactor.core.publisher.Mono;

public interface SenderGateway {
    Mono<String> send(Request request);
}
