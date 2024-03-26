package co.com.bancolombia.sample.redis;

import co.com.bancolombia.commons.jms.http.replier.api.LocationManager;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Log4j2
@AllArgsConstructor
public class RedisCache implements LocationManager {
    private final ReactiveRedisOperations<String, String> template;
    private final String endpoint;

    @Override
    public Mono<Void> set(String id, Duration timeout) {
        return template.opsForValue().set(id, endpoint)
                .flatMap(res -> template.expire(id, timeout))
                .then();
    }

    @Override
    public Mono<String> get(String id) {
        return template.opsForValue().getAndDelete(id)
                .doOnNext(item -> log.info("consulted value {} for {}", item, id));
    }
}
