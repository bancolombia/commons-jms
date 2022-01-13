package co.com.bancolombia.jms.sample.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.mq.EnableMQMessageSender;
import co.com.bancolombia.jms.sample.domain.exceptions.ParseMessageException;
import co.com.bancolombia.jms.sample.domain.model.Request;
import co.com.bancolombia.jms.sample.domain.model.RequestGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.jms.MQQueue;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.jms.Message;

@Component
@AllArgsConstructor
@EnableMQMessageSender
public class MyMQSender implements RequestGateway {
    private final MQMessageSender sender;
    private final ObjectMapper mapper;
    private final MQQueuesContainer container;

    @Override
    public Mono<String> send(Request request) {
        String json;
        try {
            json = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new ParseMessageException(e);
        }


        return sender.send(ctx -> {
            Message message = ctx.createTextMessage(json);
            message.setJMSReplyTo(container.get("DEV.QUEUE.1"));
            return message;
        });
    }
}
