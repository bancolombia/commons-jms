package co.com.bancolombia.jms.sample.entrypoints;

import co.com.bancolombia.jms.sample.domain.model.Result;
import co.com.bancolombia.jms.sample.domain.usecase.SampleReqReplyUseCase;
import co.com.bancolombia.jms.sample.domain.usecase.SampleUseCase;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
@AllArgsConstructor
public class Rest {
    private final SampleUseCase sampleUseCase;
    private final SampleReqReplyUseCase sampleReqReplyUseCase;

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(RequestPredicates.GET("/api/mq")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), request -> sample())
                .andRoute(RequestPredicates.GET("/api/mq/reqreply")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), request -> sampleReqReply());
    }

    public Mono<ServerResponse> sample() {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(sampleUseCase.sendAndListen(), Result.class);
    }

    public Mono<ServerResponse> sampleReqReply() {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(sampleReqReplyUseCase.sendAndReceive(), Result.class);
    }
}
