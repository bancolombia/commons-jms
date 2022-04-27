package co.com.bancolombia.jms.sample.domain.usecase;

import co.com.bancolombia.jms.sample.domain.model.Request;
import co.com.bancolombia.jms.sample.domain.model.RequestGateway;
import co.com.bancolombia.jms.sample.domain.model.Result;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@Log4j2
@AllArgsConstructor
public class SampleReqReplyUseCase {
    private final RequestGateway gateway;

    public Mono<Result> sendAndReceive() {
        return gateway.doRequest(Request.builder()
                        .id(UUID.randomUUID().toString())
                        .createdAt(new Date().getTime())
                        .build())
                .doOnSuccess(result -> log.info("Response got {}", result.toString()));
    }
}
