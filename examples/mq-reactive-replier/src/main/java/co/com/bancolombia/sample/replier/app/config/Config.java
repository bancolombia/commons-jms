package co.com.bancolombia.sample.replier.app.config;

import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.msg.client.jakarta.jms.JmsConstants;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jms.autoconfigure.JmsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CLIENT_RECONNECT_DISABLED;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CM_CLIENT;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CONNECTION_MODE;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_TEMPORARY_MODEL;

@Configuration
@EnableConfigurationProperties({MQConfigurationProperties.class, JmsProperties.class})
public class Config {

    @Bean
    public JsonMapper objectMapper() {
        return new JsonMapper();
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

    @Bean
    @Primary
    @SneakyThrows
    public ConnectionFactory domainA(MQConfigurationProperties properties) {
        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.JAKARTA_WMQ_PROVIDER);
        MQConnectionFactory mqConnection = (MQConnectionFactory) ff.createConnectionFactory();
        mqConnection.setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT);
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


        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.JAKARTA_WMQ_PROVIDER);
        MQConnectionFactory mqConnection = (MQConnectionFactory) ff.createConnectionFactory();
        mqConnection.setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT);
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
