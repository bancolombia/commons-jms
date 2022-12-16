package co.com.bancolombia.jms.sample.selector.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.mq.EnableMQMessageSender;
import co.com.bancolombia.commons.jms.mq.EnableMQSelectorMessageListener;
import co.com.bancolombia.jms.sample.selector.domain.exceptions.ParseMessageException;
import co.com.bancolombia.jms.sample.selector.domain.model.Request;
import co.com.bancolombia.jms.sample.selector.domain.model.RequestGateway;
import co.com.bancolombia.jms.sample.selector.domain.model.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Date;

@Component
@Log4j2
@AllArgsConstructor
@EnableMQMessageSender
@EnableMQSelectorMessageListener
public class MyMQSender implements RequestGateway {
    private final MQMessageSender sender;
    private final MQMessageSelectorListener listener;
    private final ObjectMapper mapper;
    private final MQQueuesContainer container;

    @Override
    public Mono<String> send(Request request) {
        return sender.send(ctx -> {
            String json;
            try {
                json = mapper.writeValueAsString(request);
            } catch (JsonProcessingException e) {
                throw new ParseMessageException(e);
            }
            Message message = ctx.createTextMessage(json);
            log.info(container.get("DEV.QUEUE.2").toString());
            message.setJMSReplyTo(container.get("DEV.QUEUE.2"));
            return message;
        });
    }

    public Mono<Result> getResult(String correlationId) {
        if (log.isInfoEnabled()) {
            log.info("Received and processing");
        }
        return listener.getMessage(correlationId)
                .map(this::extractResponse);
    }

    @SneakyThrows
    private Result extractResponse(Message message) {
        TextMessage textMessage = (TextMessage) message;
        Request request = mapper.readValue(textMessage.getText(), Request.class);
        Result result = Result.builder()
                .request(request.getId())
                .takenTime((new Date().getTime()) - request.getCreatedAt())
                .build();
        String id = message.getJMSMessageID();
        if (log.isInfoEnabled()) {
            log.info("Received with id: {}", id);
        }
        return result;
    }
}
