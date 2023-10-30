package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.exceptions.RelatedMessageNotFoundException;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Log4j2
public final class MQRequestReplyListener extends MQMessageListenerRetries implements MQRequestReply {
    public static final int SECONDS_TIMEOUT = 30;
    private final MQMessageSender sender;
    private final ReactiveReplyRouter<Message> router;
    private final MQQueuesContainer container;
    private final Destination requestQueue;
    private final String replyQueue;

    public MQRequestReplyListener(MQMessageSender sender,
                                  ReactiveReplyRouter<Message> router,
                                  MQQueuesContainer container,
                                  Destination requestQueue,
                                  String replyQueue,
                                  int maxRetries) {
        super(maxRetries);
        this.sender = sender;
        this.router = router;
        this.container = container;
        this.requestQueue = requestQueue;
        this.replyQueue = replyQueue;
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
        return sender.send(requestQueue, messageCreator).flatMap(id -> router.wait(id, timeout));
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

    @SneakyThrows
    @Override
    protected Mono<Object> process(Message message) {
        try {
            router.reply(message.getJMSCorrelationID(), message);
        } catch (RelatedMessageNotFoundException ex) {
            log.warn("Related message not found, usually cleaned when timeout", ex);
        }
        return Mono.empty();
    }
}
