package co.com.bancolombia.jms.sample.entrypoints;

import co.com.bancolombia.commons.jms.mq.MQListener;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import co.com.bancolombia.jms.sample.domain.model.Request;
import co.com.bancolombia.jms.sample.domain.model.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Date;

@Log4j2
@Component
@AllArgsConstructor
public class MyMQListener {
    private final ReactiveReplyRouter<Result> useCase;
    private final ObjectMapper mapper;

    @MQListener
    public Mono<Void> process(Message message) throws JMSException, JsonProcessingException {
        log.info("Received and processing");
        TextMessage textMessage = (TextMessage) message;
        Request request = mapper.readValue(textMessage.getText(), Request.class);
        Result result = Result.builder()
                .request(request.getId())
                .takenTime((new Date().getTime()) - request.getCreatedAt())
                .build();
        String id = message.getJMSCorrelationID();
        log.info("Received with id: {}", id);
        useCase.reply(id, result);
        return Mono.empty();
    }
}
