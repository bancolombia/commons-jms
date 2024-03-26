package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import co.com.bancolombia.commons.jms.http.replier.api.LocationManager;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.Message;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Log4j2
@AllArgsConstructor
public class HttpReactiveReplyRouter extends ReactiveReplyRouter<JmsMessage> {
    private final ReplyClient client;
    private final LocationManager manager;

    @Override
    public Mono<JmsMessage> wait(String messageId, Duration timeout) {
        return manager.set(messageId, timeout)
                .then(super.wait(messageId, timeout));
    }

    @Override
    public Mono<Void> remoteReply(String correlationID, Message response) {
        if (hasKey(correlationID)) {
            return Mono.fromRunnable(() -> super.reply(correlationID, Utils.fromMessage(response)));
        }
        log.info("Replying with remote call");
        return manager.get(correlationID)
                .flatMap(host -> client.remoteReply(host, Utils.fromMessage(response)));
    }

}
