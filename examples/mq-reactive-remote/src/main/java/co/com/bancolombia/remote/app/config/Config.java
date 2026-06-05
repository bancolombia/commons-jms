package co.com.bancolombia.remote.app.config;

import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import co.com.bancolombia.remote.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import lombok.SneakyThrows;
import org.springframework.boot.jms.autoconfigure.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT_DISABLED;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CM_CLIENT;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_TEMPORARY_MODEL;

@Configuration
@EnableConfigurationProperties({MQConfigurationProperties.class, JmsProperties.class})
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

    @Bean
    @Primary
    @SneakyThrows
    public ConnectionFactory domainA(MQConfigurationProperties properties) {
        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.JAKARTA_WMQ_PROVIDER);
        MQConnectionFactory mqConnection = (MQConnectionFactory) ff.createConnectionFactory();
        mqConnection.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQ_CM_CLIENT);
        mqConnection.setClientReconnectOptions(WMQ_CLIENT_RECONNECT_DISABLED); // with this all exceptions are thrown

        mqConnection.setConnectionNameList(properties.getConnName());
        mqConnection.setQueueManager(properties.getQueueManager());
        mqConnection.setChannel(properties.getChannel());
        mqConnection.setStringProperty(WMQConstants.USERID, properties.getUser());
        mqConnection.setStringProperty(WMQConstants.PASSWORD, properties.getPassword());
        if (properties.getTempModel() != null) {
            mqConnection.setStringProperty(WMQ_TEMPORARY_MODEL, properties.getTempModel());
        }
        return mqConnection;
    }

}
