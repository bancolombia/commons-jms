package co.com.bancolombia.sample.selector.entrypoints;

import co.com.bancolombia.commons.jms.internal.listener.selector.MQSchedulerProvider;
import co.com.bancolombia.sample.selector.domain.model.Result;
import co.com.bancolombia.sample.selector.domain.usecase.SampleUseCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Configuration
@AllArgsConstructor
public class Rest {
    private final SampleUseCase sampleUseCase;
    private final MQSchedulerProvider service;

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(RequestPredicates.GET("/api/mq")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), request -> sample())
                .andRoute(RequestPredicates.GET("/api/pool"), request -> pool());
    }

    public Mono<ServerResponse> sample() {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(sampleUseCase.sendAndListen(), Result.class);
    }

    public Mono<ServerResponse> pool() {
        Details details = Details.builder()
                .name(service.get().toString())
//                .size(service.getPoolSize())
//                .corePoolSize(service.getCorePoolSize())
//                .maximumPoolSize(service.getMaximumPoolSize())
//                .active(service.getActiveCount())
//                .completedTasks(service.getCompletedTaskCount())
//                .tasks(service.getTaskCount())
//                .queueSize(service.getQueue().size())
                .build();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(details);
    }

    @Data
    @Builder
    public static class Details {
        private final String name;
        private final long tasks;
        private final long completedTasks;
        private final int size;
        private final int corePoolSize;
        private final int maximumPoolSize;
        private final int active;
        private final int queueSize;
    }
}
