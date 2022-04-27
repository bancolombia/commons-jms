package co.com.bancolombia.jms.sample.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.mq.EnableMQMessageSender;
import co.com.bancolombia.jms.sample.domain.model.Request;
import co.com.bancolombia.jms.sample.domain.model.SenderGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.jms.Message;

@Component
@AllArgsConstructor
@EnableMQMessageSender
public class MyMQSender implements SenderGateway {
    private final MQMessageSender sender;
    private final ObjectMapper mapper;
    private final MQQueuesContainer container;

    @Override
    public Mono<String> send(Request request) {
        return Mono.fromCallable(() -> mapper.writeValueAsString(request))
                .flatMap(json -> sender.send(ctx -> {
                    Message message = ctx.createTextMessage(json);
                    message.setJMSReplyTo(container.get("DEV.QUEUE.2"));
                    return message;
                }));
    }
}
