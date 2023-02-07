package co.com.bancolombia.jms.sample.drivenadapters.reqreply;

import co.com.bancolombia.commons.jms.mq.EnableReqReply;
import co.com.bancolombia.jms.sample.domain.model.Request;
import co.com.bancolombia.jms.sample.domain.model.RequestGateway;
import co.com.bancolombia.jms.sample.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Date;

@Log4j2
@Component
@AllArgsConstructor
@EnableReqReply(scanBasePackages = "co.com.bancolombia")
public class MyRequestReplyAdapter implements RequestGateway {
    private final MyRequestReply requestReply;
    private final ObjectMapper mapper;

    @Override
    public Mono<Result> doRequest(Request request) {
        return Mono.fromCallable(() -> mapper.writeValueAsString(request))
                .flatMap(requestReply::requestReply)
                .map(this::mapResponse);
    }

    @SneakyThrows
    private Result mapResponse(Message message) {
        log.info("Received and processing");
        TextMessage textMessage = (TextMessage) message;
        Request request = mapper.readValue(textMessage.getText(), Request.class);
        return Result.builder()
                .request(request.getId())
                .takenTime((new Date().getTime()) - request.getCreatedAt())
                .build();
    }
}
