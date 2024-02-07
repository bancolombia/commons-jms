package co.com.bancolombia.sample.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.sample.domain.model.Request;
import co.com.bancolombia.sample.domain.model.SenderGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Message;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class MyMQSender implements SenderGateway {
//    private final MQMessageSender sender;
    private final XDomainSender sender2;
    private final ObjectMapper mapper;
    private final MQQueuesContainer container;

    @Override
    public Mono<String> send(Request request) {
        return Mono.fromCallable(() -> mapper.writeValueAsString(request))
                .flatMap(json -> sender2.send(ctx -> {
                    Message message = ctx.createTextMessage(json);
                    message.setJMSReplyTo(container.get("DEV.QUEUE.3"));
                    return message;
                }));
    }
}
