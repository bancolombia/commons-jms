package co.com.bancolombia.jms.sample.app.config;

import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import co.com.bancolombia.jms.sample.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT;

@Configuration
public class Config {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // Sample connection factory customization
    @Bean
    public MQConnectionFactoryCustomizer cfCustomizer() {
        return mqConnectionFactory -> {
            try {
                mqConnectionFactory.setClientReconnectOptions(WMQ_CLIENT_RECONNECT);
            } catch (JMSException e) {
                throw new JMSRuntimeException(e.getErrorCode(), e.getMessage(), e);
            }
        };
    }

    @Bean
    public ReactiveReplyRouter<Result> resultReactiveReplyRouter() {
        return new ReactiveReplyRouter<>();
    }
}
