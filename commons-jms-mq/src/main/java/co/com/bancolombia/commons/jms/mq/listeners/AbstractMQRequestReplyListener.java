package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Getter
@Log4j2
public abstract class AbstractMQRequestReplyListener<T> extends MQMessageListenerRetries {
    public static final int SECONDS_TIMEOUT = 30;
    private final MQMessageSender sender;
    protected final ReactiveReplyRouter<T> router;
    private final MQQueuesContainer queuesContainer;
    private final Destination requestQueue;
    private final String replyQueue;

    protected AbstractMQRequestReplyListener(MQMessageSender sender,
                                             ReactiveReplyRouter<T> router,
                                             MQQueuesContainer queuesContainer,
                                             Destination requestQueue,
                                             String replyQueue,
                                             int maxRetries) {
        super(maxRetries);
        this.sender = sender;
        this.router = router;
        this.queuesContainer = queuesContainer;
        this.requestQueue = requestQueue;
        this.replyQueue = replyQueue;
    }

    public Mono<T> requestReply(String message) {
        return requestReply(defaultCreator(message));
    }

    public Mono<T> requestReply(String message, Duration timeout) {
        return requestReply(defaultCreator(message), timeout);
    }

    public Mono<T> requestReply(MQMessageCreator messageCreator) {
        return requestReply(messageCreator, Duration.ofSeconds(SECONDS_TIMEOUT));
    }

    public Mono<T> requestReply(MQMessageCreator messageCreator, Duration timeout) {
        return sender.send(requestQueue, messageCreator).flatMap(id -> router.wait(id, timeout));
    }

    private MQMessageCreator defaultCreator(String message) {
        return ctx -> {
            Message jmsMessage = ctx.createTextMessage(message);
            Queue queue = queuesContainer.get(replyQueue);
            jmsMessage.setJMSReplyTo(queue);
            if (log.isInfoEnabled() && queue != null) {
                log.info("Setting queue for reply to: {}", queue.getQueueName());
            }
            return jmsMessage;
        };
    }
}
