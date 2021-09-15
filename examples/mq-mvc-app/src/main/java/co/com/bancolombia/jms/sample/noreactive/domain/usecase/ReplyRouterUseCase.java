package co.com.bancolombia.jms.sample.noreactive.domain.usecase;

import co.com.bancolombia.jms.sample.noreactive.domain.exceptions.RelatedMessageNotFoundException;
import co.com.bancolombia.jms.sample.noreactive.domain.model.Result;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ReplyRouterUseCase {
    private final ConcurrentHashMap<String, CompletableFuture<Result>> processors = new ConcurrentHashMap<>();

    public CompletableFuture<Result> wait(String correlationID) {
        final CompletableFuture<Result> processor = new CompletableFuture<>();
        processors.put(correlationID, processor);
        log.info("Waiting for: {}", correlationID);
        return processor;
    }

    public void reply(String correlationID, Result response) {
        if (correlationID != null) {
            log.info("Replying with id: {}", correlationID);
            final CompletableFuture<Result> processor = processors.remove(correlationID);
            if (processor == null) {
                throw new RelatedMessageNotFoundException();
            } else {
                processor.complete(response);
            }
        }
    }

    public void clean(String correlationId) {
        processors.remove(correlationId);
    }
}
