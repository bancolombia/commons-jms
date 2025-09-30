package co.com.bancolombia.samplebuilder.drivenadapters;

import co.com.bancolombia.samplebuilder.domain.model.Request;
import co.com.bancolombia.samplebuilder.domain.model.RequestGateway;
import co.com.bancolombia.samplebuilder.domain.model.Result;
import co.com.bancolombia.samplebuilder.domain.model.SenderGateway;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MQClientAdapter implements RequestGateway, SenderGateway {
    @Override
    public Mono<Result> doRequest(Request request) {
        return null;
    }

    @Override
    public Mono<Result> doRequestTmp(Request request) {
        return null;
    }

    @Override
    public Mono<String> send(Request request) {
        return null;
    }
}
