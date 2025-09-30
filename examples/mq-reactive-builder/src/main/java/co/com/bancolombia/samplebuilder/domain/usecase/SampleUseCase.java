package co.com.bancolombia.samplebuilder.domain.usecase;

import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import co.com.bancolombia.samplebuilder.domain.model.Request;
import co.com.bancolombia.samplebuilder.domain.model.Result;
import co.com.bancolombia.samplebuilder.domain.model.SenderGateway;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@Log4j2
@AllArgsConstructor
public class SampleUseCase {
    private final SenderGateway gateway;
    private final ReactiveReplyRouter<Result> replier;

    public Mono<Result> sendAndListen() {
        return gateway.send(Request.builder()
                        .id(UUID.randomUUID().toString())
                        .createdAt(new Date().getTime())
                        .build())
                .doOnSuccess(id -> log.info("Message sent: {}", id))
                .flatMap(replier::wait);
    }
}
