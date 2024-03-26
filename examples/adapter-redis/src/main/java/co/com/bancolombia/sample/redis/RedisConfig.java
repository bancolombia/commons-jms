package co.com.bancolombia.sample.redis;

import co.com.bancolombia.commons.jms.http.replier.api.LocationManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisOperations;

@Log4j2
@Configuration
public class RedisConfig {

    @Bean
    public LocationManager locationManager(ReactiveRedisOperations<String, String> template,
                                           @Value("${commons.jms.reply.endpoint}") String endpoint) {
        log.info("Using endpoint {} for replies", endpoint);
        return new RedisCache(template, endpoint);
    }

}
