package co.com.bancolombia.remote.app.config;

import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import co.com.bancolombia.remote.domain.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
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
    public ConnectionFactory cachingJmsConnectionFactory(MQConfigurationProperties properties, ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers, JmsProperties jmsProperties) {
        JmsProperties.Cache cacheProperties = jmsProperties.getCache();
        properties.setQueueManager("QM1");
        properties.setConnName("localhost(1414)");
        MQConnectionFactory wrappedConnectionFactory = createConnectionFactory(properties, sslBundles, factoryCustomizers);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(wrappedConnectionFactory);
        connectionFactory.setCacheConsumers(cacheProperties.isConsumers());
        connectionFactory.setCacheProducers(cacheProperties.isProducers());
        connectionFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());
        return connectionFactory;
    }

    private static MQConnectionFactory createConnectionFactory(MQConfigurationProperties properties, ObjectProvider<SslBundles> sslBundles, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
        return (new MQConnectionFactoryFactory(properties, (SslBundles) sslBundles.getIfAvailable(), (List) factoryCustomizers.getIfAvailable())).createConnectionFactory(MQConnectionFactory.class);
    }

}
