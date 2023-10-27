package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.exceptions.RelatedMessageNotFoundException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Log4j2
public class ReactiveReplyRouter<T> {
    private final ConcurrentHashMap<String, Sinks.One<T>> processors = new ConcurrentHashMap<>();

    public Mono<T> wait(String messageId) {
        final Sinks.One<T> processor = Sinks.one();
        processors.put(messageId, processor);
        log.info("Waiting for: {}", messageId);
        return processor.asMono();
    }

    public Mono<T> wait(String messageId, Duration timeout) {
        return this.wait(messageId).timeout(timeout).doOnError(TimeoutException.class, e -> clean(messageId));
    }

    public void reply(String correlationID, T response) {
        if (correlationID != null) {
            log.info("Replying with id: {}", correlationID);
            final Sinks.One<T> processor = processors.remove(correlationID);
            if (processor == null) {
                throw new RelatedMessageNotFoundException(correlationID);
            } else {
                processor.tryEmitValue(response);
            }
        }
    }

    public void error(String correlationID, Throwable error){
        if (correlationID != null) {
            log.info("Replying with id: {}", correlationID);
            final Sinks.One<T> processor = processors.remove(correlationID);
            if (processor == null) {
                throw new RelatedMessageNotFoundException(correlationID);
            } else {
                processor.tryEmitError(error);
            }
        }
    }

    public void clean(String correlationId) {
        processors.remove(correlationId);
    }
}
