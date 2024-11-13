package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Getter
@Log4j2
public final class MQRequestReplySelector implements MQRequestReply {
    public static final int SECONDS_TIMEOUT = 30;
    private final MQMessageSender sender;
    private final MQQueuesContainer container;
    private final Destination requestQueue;
    private final String replyQueue;
    private final SelectorBuilder selector;

    private final MQMessageSelectorListener listener;

    public MQRequestReplySelector(MQMessageSender sender,
                                  MQQueuesContainer container,
                                  Destination requestQueue,
                                  String replyQueue,
                                  SelectorBuilder selector,
                                  MQMessageSelectorListener listener) {
        this.sender = sender;
        this.container = container;
        this.requestQueue = requestQueue;
        this.replyQueue = replyQueue;
        this.selector = selector;
        this.listener = listener;
    }

    @Override
    public Mono<Message> requestReply(String message) {
        return requestReply(defaultCreator(message));
    }

    @Override
    public Mono<Message> requestReply(String message, Duration timeout) {
        return requestReply(defaultCreator(message), timeout);
    }

    @Override
    public Mono<Message> requestReply(MQMessageCreator messageCreator) {
        return requestReply(messageCreator, Duration.ofSeconds(SECONDS_TIMEOUT));
    }

    @Override
    public Mono<Message> requestReply(MQMessageCreator messageCreator, Duration timeout) {
        return sender.send(requestQueue, messageCreator)
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(),
                        container.get(replyQueue)));
    }

    public Mono<Message> requestReply(String message, Destination request, Destination reply, Duration timeout) {
        return sender.send(request, defaultCreator(message))
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(), reply));
    }

    public Mono<Message> requestReply(MQMessageCreator messageCreator, Destination request, Destination reply, Duration timeout) {
        return sender.send(request, messageCreator)
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(), reply));
    }

    private MQMessageCreator defaultCreator(String message) {
        return ctx -> {
            Message jmsMessage = ctx.createTextMessage(message);
            Queue queue = container.get(replyQueue);
            jmsMessage.setJMSReplyTo(queue);
            if (log.isInfoEnabled() && queue != null) {
                log.info("Setting queue for reply to: {}", queue.getQueueName());
            }
            return jmsMessage;
        };
    }
}
