package co.com.bancolombia.sample.selector.domain.usecase;

import co.com.bancolombia.sample.selector.domain.model.Request;
import co.com.bancolombia.sample.selector.domain.model.RequestGateway;
import co.com.bancolombia.sample.selector.domain.model.Result;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@Log4j2
@AllArgsConstructor
public class SampleUseCase {
    private final RequestGateway gateway;

    public Mono<Result> sendAndListen() {
        return gateway.send(Request.builder()
                        .id(UUID.randomUUID().toString())
                        .createdAt(new Date().getTime())
                        .build())
                .doOnSuccess(id -> log.info("Message sent: {}", id))
                .flatMap(gateway::getResult);
    }
}
