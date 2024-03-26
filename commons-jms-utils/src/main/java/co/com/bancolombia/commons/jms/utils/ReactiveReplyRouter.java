package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.exceptions.RelatedMessageNotFoundException;
import jakarta.jms.Message;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Log4j2
public class ReactiveReplyRouter<T> {
    public static final long DEFAULT_TIMEOUT = 30L;
    private final ConcurrentHashMap<String, Sinks.One<T>> processors = new ConcurrentHashMap<>();


    public Mono<T> wait(String messageId) {
        return wait(messageId, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    public Mono<T> wait(String messageId, Duration timeout) {
        final Sinks.One<T> processor = Sinks.one();
        processors.put(messageId, processor);
        log.info("Waiting for: {}", messageId);
        return processor.asMono().timeout(timeout).doOnError(TimeoutException.class, e -> clean(messageId));
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

    public void error(String correlationID, Throwable error) {
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

    public boolean hasKey(String correlationID) {
        return processors.containsKey(correlationID);
    }

    public Mono<Void> remoteReply(String correlationID, Message response) {
        return Mono.error(() -> new UnsupportedOperationException("Not implemented"));
    }
}
