package co.com.bancolombia.commons.jms.http.replier.api;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface LocationManager {

    Mono<Void> set(String id, Duration timeout);

    Mono<String> get(String id);
}
