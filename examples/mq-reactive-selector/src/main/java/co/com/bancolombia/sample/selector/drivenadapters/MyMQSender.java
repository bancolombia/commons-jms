package co.com.bancolombia.sample.selector.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.mq.EnableMQGateway;
import co.com.bancolombia.sample.selector.domain.model.Request;
import co.com.bancolombia.sample.selector.domain.model.RequestGateway;
import co.com.bancolombia.sample.selector.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Component
@Log4j2
@AllArgsConstructor
@EnableMQGateway
public class MyMQSender implements RequestGateway {
    private final MyRequestReply requestReply; // domainB
    private final ObjectMapper mapper;
    private final MQQueuesContainer container;

    @Override
    public Mono<Result> send(Request request) {
        return requestReply.requestReply(ctx -> {
                    String json = mapToJson(request);
                    Message message = ctx.createTextMessage(json);
                    message.setJMSReplyTo(container.get("DEV.QUEUE.2"));
                    return message;
                }, Duration.ofSeconds(5))
                .map(this::extractResponse);
    }

    @SneakyThrows
    private String mapToJson(Request request) {
        return mapper.writeValueAsString(request);
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
