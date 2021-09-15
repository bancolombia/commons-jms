package co.com.bancolombia.jms.sample.noreactive.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.mq.EnableMQMessageSender;
import co.com.bancolombia.jms.sample.noreactive.domain.exceptions.ParseMessageException;
import co.com.bancolombia.jms.sample.noreactive.domain.model.Request;
import co.com.bancolombia.jms.sample.noreactive.domain.model.RequestGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@EnableMQMessageSender
public class MyMQSender implements RequestGateway {
    private final MQMessageSenderSync sender;
    private final ObjectMapper mapper;

    @Override
    public String send(Request request) {
        return sender.send(ctx -> {
            String json;
            try {
                json = mapper.writeValueAsString(request);
            } catch (JsonProcessingException e) {
                throw new ParseMessageException(e);
            }
            return ctx.createTextMessage(json);
        });
    }
}
