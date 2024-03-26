package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import co.com.bancolombia.commons.jms.http.replier.api.LocationManager;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.Message;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Duration;

@AllArgsConstructor
public class HttpReactiveReplyRouter extends ReactiveReplyRouter<JmsMessage> {
    private final ReplyClient client;
    private final LocationManager manager;
    private final String endpoint;

    @Override
    public Mono<JmsMessage> wait(String messageId, Duration timeout) {
        return manager.set(messageId, endpoint, timeout)
                .then(super.wait(messageId, timeout));
    }

    @Override
    public Mono<Void> remoteReply(String correlationID, Message response) {
        if (hasKey(correlationID)) {
            return Mono.fromRunnable(() -> super.reply(correlationID, Utils.fromMessage(response)));
        }
        return manager.get(correlationID)
                .flatMap(host -> client.remoteReply(host, Utils.fromMessage(response)));
    }

}
