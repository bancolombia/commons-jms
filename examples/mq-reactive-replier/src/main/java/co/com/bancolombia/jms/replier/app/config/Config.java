package co.com.bancolombia.jms.replier.app.config;

import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.jms.DeliveryMode;

@Configuration
public class Config {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    /**
     * When replying to a temporary queue
     *
     * @return MQProducerCustomizer
     */
    @Bean
    public MQProducerCustomizer producerCustomizer() {
        return producer -> producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }
}
