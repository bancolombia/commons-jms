package co.com.bancolombia.sample.noreactive.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.mq.EnableMQGateway;
import co.com.bancolombia.sample.noreactive.domain.exceptions.ParseMessageException;
import co.com.bancolombia.sample.noreactive.domain.model.Request;
import co.com.bancolombia.sample.noreactive.domain.model.RequestGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@EnableMQGateway
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
