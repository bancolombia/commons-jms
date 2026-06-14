package co.com.bancolombia.sample.noreactive.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.mq.EnableMQGateway;
import co.com.bancolombia.sample.noreactive.domain.model.Request;
import co.com.bancolombia.sample.noreactive.domain.model.RequestGateway;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@AllArgsConstructor
@EnableMQGateway
public class MyMQSender implements RequestGateway {
    private final MQMessageSenderSync sender;
    private final JsonMapper mapper;

    @Override
    public String send(Request request) {
        return sender.send(ctx -> {
            String json;
            json = mapper.writeValueAsString(request);
            return ctx.createTextMessage(json);
        });
    }
}
