package co.com.bancolombia.sample.drivenadapters.reqreply;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import co.com.bancolombia.sample.domain.model.Request;
import co.com.bancolombia.sample.domain.model.RequestGateway;
import co.com.bancolombia.sample.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;

@Log4j2
@Component
@AllArgsConstructor
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
    private Result mapResponse(JmsMessage message) {
        log.info("Received and processing");
        Request request = mapper.readValue(message.getBody(), Request.class);
        return Result.builder()
                .request(request.getId())
                .takenTime((new Date().getTime()) - request.getCreatedAt())
                .build();
    }
}
