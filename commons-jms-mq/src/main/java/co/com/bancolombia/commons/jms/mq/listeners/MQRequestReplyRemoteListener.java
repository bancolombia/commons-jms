package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQRequestReplyRemote;
import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import co.com.bancolombia.commons.jms.internal.listener.reply.CorrelationExtractor;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public final class MQRequestReplyRemoteListener extends AbstractMQRequestReplyListener<JmsMessage>
        implements MQRequestReplyRemote {


    public MQRequestReplyRemoteListener(MQMessageSender sender, ReactiveReplyRouter<JmsMessage> router,
                                        MQQueuesContainer queuesContainer, Destination requestQueue,
                                        String replyQueue, CorrelationExtractor correlationExtractor, int maxRetries) {
        super(sender, router, queuesContainer, requestQueue, replyQueue, correlationExtractor, maxRetries);
    }

    @SneakyThrows
    @Override
    protected Mono<Object> process(Message message) {
        return router.remoteReply(getCorrelationId(message), message)
                .then(Mono.empty());
    }
}
