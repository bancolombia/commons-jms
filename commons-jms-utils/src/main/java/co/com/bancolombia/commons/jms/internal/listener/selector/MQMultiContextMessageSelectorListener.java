package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@Log4j2
@AllArgsConstructor
public class MQMultiContextMessageSelectorListener implements MQMessageSelectorListener {
    private final MQMessageSelectorListenerSync listenerSync; // MQMultiContextMessageSelectorListenerSync
    private final ExecutorService executorService;
    private final ReactiveReplyRouter<Message> router;

    @Override
    public Mono<Message> getMessage(String correlationId) {
        return router.wait(correlationId)
                .doOnSubscribe(s -> executorService.submit(() -> realGetMessageBySelector(correlationId,
                        () -> listenerSync.getMessage(correlationId))));
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout) {
        return router.wait(correlationId, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> realGetMessageBySelector(correlationId,
                        () -> listenerSync.getMessage(correlationId, timeout))));
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout, Destination destination) {
        return router.wait(correlationId, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> realGetMessageBySelector(correlationId,
                        () -> listenerSync.getMessage(correlationId, timeout, destination))));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector) {
        return router.wait(selector)
                .doOnSubscribe(s -> executorService.submit(() -> realGetMessageBySelector(selector,
                        () -> listenerSync.getMessageBySelector(selector))));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout) {
        return router.wait(selector, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> realGetMessageBySelector(selector,
                        () -> listenerSync.getMessageBySelector(selector, timeout))));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout, Destination destination) {
        return router.wait(selector, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> realGetMessageBySelector(selector,
                        () -> listenerSync.getMessageBySelector(selector, timeout, destination))));
    }

    private void realGetMessageBySelector(String selector, Supplier<Message> supplier) {
        try {
            Message message = supplier.get();
            router.reply(selector, message);
        } catch (Exception e) {
            log.warn("Error getting message with selector: {}", selector, e);
            router.error(selector, e);
        }
    }
}
