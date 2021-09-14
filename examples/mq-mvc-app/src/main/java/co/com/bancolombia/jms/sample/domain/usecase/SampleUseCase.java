package co.com.bancolombia.jms.sample.domain.usecase;

import co.com.bancolombia.jms.sample.domain.model.Request;
import co.com.bancolombia.jms.sample.domain.model.RequestGateway;
import co.com.bancolombia.jms.sample.domain.model.Result;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.Date;
import java.util.UUID;

@Log4j2
@AllArgsConstructor
public class SampleUseCase {
    private final RequestGateway gateway;
    private final ReplyRouterUseCase replier;


    @SneakyThrows
    public Result sendAndListen() {
        String messageId = gateway.send(Request.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date().getTime())
                .build());
        log.info("Message sent: {}", messageId);
        return replier.wait(messageId).get();
    }
}
