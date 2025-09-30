package co.com.bancolombia.sample.app.config;

import co.com.bancolombia.commons.jms.api.model.MQMessageHandler;
import co.com.bancolombia.commons.jms.api.model.spec.CommonsJMSSpec;
import co.com.bancolombia.commons.jms.api.model.spec.MQDomainSpec;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import co.com.bancolombia.sample.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.msg.client.jakarta.jms.JmsConstants;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.common.CommonConstants;
import jakarta.jms.ConnectionFactory;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

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

    @Bean
    public ReactiveReplyRouter<Result> replier() {
        return new ReactiveReplyRouter<>();
    }

    @Bean
    public MQMessageHandler messageHandler() {
        return (source, message) -> {
            System.out.println("Received message from " + source + ": " + message);
            return Mono.empty();
        };
    }

    @Bean
    public CommonsJMSSpec commonsJMSSpec(MQConfigurationProperties properties, MQMessageHandler handler) {
        return CommonsJMSSpec.builder()
                .withDomain(MQDomainSpec.builder("domainA", domainA(properties))
                        .listenQueue("DEV.QUEUE.1", handler)
                        .listenQueue("DEV.QUEUE.2", handler)
                        .withSender()
                        .withTemporaryRequestReply()
                        .withFixedRequestReply()
                        .build())
                .withDomain(MQDomainSpec.builder("domainB", domainB(properties))
                        .listenQueue("DEV.QUEUE.3", handler)
                        .withFixedRequestReply()
                        .build())
                .build();
    }

    @SneakyThrows
    private ConnectionFactory domainA(MQConfigurationProperties properties) {
        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.JAKARTA_WMQ_PROVIDER);
        MQConnectionFactory mqConnection = (MQConnectionFactory) ff.createConnectionFactory();
        mqConnection.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, WMQ_CM_CLIENT);
        mqConnection.setClientReconnectOptions(WMQ_CLIENT_RECONNECT_DISABLED); // with this all exceptions are thrown

        mqConnection.setConnectionNameList(properties.getConnName());
        mqConnection.setQueueManager(properties.getQueueManager());
        mqConnection.setChannel(properties.getChannel());
        mqConnection.setStringProperty(JmsConstants.USERID, properties.getUser());
        mqConnection.setStringProperty(JmsConstants.PASSWORD, properties.getPassword());
        if (properties.getTempModel() != null) {
            mqConnection.setStringProperty(WMQ_TEMPORARY_MODEL, properties.getTempModel());
        }
        return mqConnection;
    }

    @SneakyThrows
    private ConnectionFactory domainB(MQConfigurationProperties original) {
        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        MQConfigurationProperties properties = new MQConfigurationProperties();
        properties.setUser(original.getUser());
        properties.setPassword(original.getPassword());
        properties.setChannel(original.getChannel());
        properties.setQueueManager("QM1");
        properties.setConnName("localhost(1415)");


        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.JAKARTA_WMQ_PROVIDER);
        MQConnectionFactory mqConnection = (MQConnectionFactory) ff.createConnectionFactory();
        mqConnection.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, WMQ_CM_CLIENT);
        mqConnection.setClientReconnectOptions(WMQ_CLIENT_RECONNECT_DISABLED); // with this all exceptions are thrown

        mqConnection.setConnectionNameList(properties.getConnName());
        mqConnection.setQueueManager(properties.getQueueManager());
        mqConnection.setChannel(properties.getChannel());
        mqConnection.setStringProperty(JmsConstants.USERID, properties.getUser());
        mqConnection.setStringProperty(JmsConstants.PASSWORD, properties.getPassword());
        if (properties.getTempModel() != null) {
            mqConnection.setStringProperty(WMQ_TEMPORARY_MODEL, properties.getTempModel());
        }
        return mqConnection;
    }

}
