package co.com.bancolombia.jms.sample.domain.usecase;

import co.com.bancolombia.jms.sample.domain.exceptions.RelatedMessageNotFoundException;
import co.com.bancolombia.jms.sample.domain.model.Result;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ReactiveReplyRouterUseCase {
    private final ConcurrentHashMap<String, Sinks.One<Result>> processors = new ConcurrentHashMap<>();

    public Mono<Result> wait(String correlationID) {
        final Sinks.One<Result> processor = Sinks.one();
        processors.put(correlationID, processor);
        log.info("Waiting for: {}", correlationID);
        return processor.asMono();
    }

    public void reply(String correlationID, Result response) {
        if (correlationID != null) {
            log.info("Replying with id: {}", correlationID);
            final Sinks.One<Result> processor = processors.remove(correlationID);
            if (processor == null) {
                throw new RelatedMessageNotFoundException();
            } else {
                processor.tryEmitValue(response);
            }
        }
    }

    public void clean(String correlationId) {
        processors.remove(correlationId);
    }
}
