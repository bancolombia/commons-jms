package co.com.bancolombia.jms.sample.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;

import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT;

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
}
