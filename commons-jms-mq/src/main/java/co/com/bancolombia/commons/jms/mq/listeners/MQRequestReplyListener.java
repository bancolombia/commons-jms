package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.exceptions.RelatedMessageNotFoundException;
import co.com.bancolombia.commons.jms.internal.listener.reply.CorrelationExtractor;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public final class MQRequestReplyListener extends AbstractMQRequestReplyListener<Message> implements MQRequestReply {

    public MQRequestReplyListener(MQMessageSender sender, ReactiveReplyRouter<Message> router,
                                  MQQueuesContainer queuesContainer, Destination requestQueue,
                                  String replyQueue, CorrelationExtractor correlationExtractor, int maxRetries) {
        super(sender, router, queuesContainer, requestQueue, replyQueue, correlationExtractor, maxRetries);
    }

    @SneakyThrows
    @Override
    protected Mono<Object> process(Message message) {
        try {
            router.reply(getCorrelationId(message), message);
        } catch (RelatedMessageNotFoundException ex) {
            log.warn("Related message not found, usually cleaned when timeout", ex);
        }
        return Mono.empty();
    }
}
