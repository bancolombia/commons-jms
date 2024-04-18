package co.com.bancolombia.sample.app.config;

import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import co.com.bancolombia.sample.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;

import java.util.List;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT_DISABLED;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT_Q_MGR;
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

    @Bean
    @SneakyThrows
    public ConnectionFactory domainB(MQConfigurationProperties original) {
        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        MQConfigurationProperties properties = new MQConfigurationProperties();
        properties.setUser(original.getUser());
        properties.setPassword(original.getPassword());
        properties.setChannel(original.getChannel());
        properties.setQueueManager("QM2");
        properties.setConnName("localhost(1415)");


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
