package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Log4j2
public record MQRequestReplySelector(MQMessageSender sender, MQQueuesContainer container, Destination requestQueue,
                                     String replyQueue, SelectorBuilder selector,
                                     MQMessageSelectorListener listener) implements MQRequestReply {
    public static final int SECONDS_TIMEOUT = 30;
    public static final String LOG_REPLY_QUEUE = "Setting queue for reply to: {}";

    @Override
    public Mono<Message> requestReply(String message) {
        return requestReply(defaultCreator(message));
    }

    @Override
    public Mono<Message> requestReply(MQMessageCreator messageCreator) {
        return requestReply(messageCreator, Duration.ofSeconds(SECONDS_TIMEOUT));
    }

    @Override
    public Mono<Message> requestReply(String message, Duration timeout) {
        return requestReply(defaultCreator(message), timeout);
    }

    @Override
    public Mono<Message> requestReply(MQMessageCreator messageCreator, Duration timeout) {
        return sender.send(requestQueue, wrappedCreator(messageCreator))
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(),
                        container.get(replyQueue)));
    }

    @Override
    public Mono<Message> requestReply(String message, Destination request, Duration timeout) {
        return sender.send(request, defaultCreator(message))
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(),
                        container.get(replyQueue)));
    }

    @Override
    public Mono<Message> requestReply(MQMessageCreator messageCreator, Destination request, Duration timeout) {
        return sender.send(request, wrappedCreator(messageCreator))
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(),
                        container.get(replyQueue)));
    }

    @Override
    public Mono<Message> requestReply(String message, Destination request, Destination reply, Duration timeout) {
        return sender.send(request, defaultCreator(message, reply))
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(), reply));
    }

    @Override
    public Mono<Message> requestReply(MQMessageCreator messageCreator, Destination request, Destination reply,
                                      Duration timeout) {
        return sender.send(request, wrappedCreator(messageCreator, reply))
                .flatMap(id -> listener.getMessageBySelector(selector.buildSelector(id), timeout.toMillis(), reply));
    }

    private MQMessageCreator wrappedCreator(MQMessageCreator creator, Destination reply) {
        return ctx -> {
            Message message = creator.create(ctx);
            if (message.getJMSReplyTo() == null) {
                message.setJMSReplyTo(reply);
                logQueue(reply);
            }
            return message;
        };
    }

    private MQMessageCreator wrappedCreator(MQMessageCreator creator) {
        return ctx -> {
            Message message = creator.create(ctx);
            if (message.getJMSReplyTo() == null) {
                setReplyTo(message);
            }
            return message;
        };
    }

    private MQMessageCreator defaultCreator(String message, Destination reply) {
        return ctx -> {
            Message jmsMessage = ctx.createTextMessage(message);
            jmsMessage.setJMSReplyTo(reply);
            logQueue(reply);
            return jmsMessage;
        };
    }

    private MQMessageCreator defaultCreator(String message) {
        return ctx -> {
            Message jmsMessage = ctx.createTextMessage(message);
            setReplyTo(jmsMessage);
            return jmsMessage;
        };
    }

    @SneakyThrows
    private void setReplyTo(Message message) {
        Queue queue = container.get(replyQueue);
        message.setJMSReplyTo(queue);
        logQueue(queue);
    }

    private static void logQueue(Destination destination) throws JMSException {
        if (log.isInfoEnabled() && destination != null) {
            String message;
            if (destination instanceof Queue queue) {
                message = queue.getQueueName();
            } else {
                message = destination.toString();
            }
            log.info(LOG_REPLY_QUEUE, message);
        }
    }
}
