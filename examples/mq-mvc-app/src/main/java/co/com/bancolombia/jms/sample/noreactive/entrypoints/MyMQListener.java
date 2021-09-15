package co.com.bancolombia.jms.sample.noreactive.entrypoints;

import co.com.bancolombia.commons.jms.mq.MQListener;
import co.com.bancolombia.jms.sample.noreactive.domain.model.Request;
import co.com.bancolombia.jms.sample.noreactive.domain.model.Result;
import co.com.bancolombia.jms.sample.noreactive.domain.usecase.ReplyRouterUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Date;

@Log4j2
@Component
@AllArgsConstructor
public class MyMQListener {
    private final ReplyRouterUseCase useCase;
    private final ObjectMapper mapper;

    @MQListener
    public void process(Message message) throws JMSException, JsonProcessingException {
        log.info("Received and processing");
        TextMessage textMessage = (TextMessage) message;
        Request request = mapper.readValue(textMessage.getText(), Request.class);
        Result result = Result.builder()
                .request(request.getId())
                .takenTime((new Date().getTime()) - request.getCreatedAt())
                .build();
        String id = message.getJMSMessageID();
        log.info("Received with id: {}", id);
        useCase.reply(id, result);
    }
}
