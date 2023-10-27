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

@Log4j2
@AllArgsConstructor
public class MQMultiContextMessageSelectorListener implements MQMessageSelectorListener {
    private final MQMessageSelectorListenerSync listenerSync; // MQMultiContextMessageSelectorListenerSync
    private final ExecutorService executorService;
    private final ReactiveReplyRouter<Message> router;

    @Override
    public Mono<Message> getMessage(String correlationId) {
        return router.wait(correlationId)
                .doOnSubscribe(s -> executorService.submit(() -> {
                    try {
                        Message message = listenerSync.getMessage(correlationId);
                        router.reply(correlationId, message);
                    } catch (Exception e) {
                        log.warn("Error getting message with correlationId: {}", correlationId, e);
                        router.error(correlationId, e);
                    }
                }));
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout) {
        return router.wait(correlationId, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> {
                    try {
                        Message message = listenerSync.getMessage(correlationId, timeout);
                        router.reply(correlationId, message);
                    } catch (Exception e) {
                        log.warn("Error getting message with correlationId: {}", correlationId, e);
                        router.error(correlationId, e);
                    }
                }));
    }

    @Override
    public Mono<Message> getMessage(String correlationId, long timeout, Destination destination) {
        return router.wait(correlationId, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> {
                    try {
                        Message message = listenerSync.getMessage(correlationId, timeout, destination);
                        router.reply(correlationId, message);
                    } catch (Exception e) {
                        log.warn("Error getting message with correlationId: {}", correlationId, e);
                        router.error(correlationId, e);
                    }
                }));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector) {
        return router.wait(selector)
                .doOnSubscribe(s -> executorService.submit(() -> {
                    try {
                        Message message = listenerSync.getMessageBySelector(selector);
                        router.reply(selector, message);
                    } catch (Exception e) {
                        log.warn("Error getting message with selector: {}", selector, e);
                        router.error(selector, e);
                    }
                }));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout) {
        return router.wait(selector, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> {
                    try {
                        Message message = listenerSync.getMessageBySelector(selector, timeout);
                        router.reply(selector, message);
                    } catch (Exception e) {
                        log.warn("Error getting message with selector: {}", selector, e);
                        router.error(selector, e);
                    }
                }));
    }

    @Override
    public Mono<Message> getMessageBySelector(String selector, long timeout, Destination destination) {
        return router.wait(selector, Duration.ofMillis(timeout))
                .doOnSubscribe(s -> executorService.submit(() -> {
                    try {
                        Message message = listenerSync.getMessageBySelector(selector, timeout, destination);
                        router.reply(selector, message);
                    } catch (Exception e) {
                        log.warn("Error getting message with selector: {}", selector, e);
                        router.error(selector, e);
                    }
                }));
    }
}
