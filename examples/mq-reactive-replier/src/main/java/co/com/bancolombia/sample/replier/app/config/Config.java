package co.com.bancolombia.sample.replier.app.config;

import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import lombok.SneakyThrows;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;

import java.util.List;

@Configuration
@EnableConfigurationProperties({MQConfigurationProperties.class, JmsProperties.class})
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

    @Bean
    @Primary
    @SneakyThrows
    public ConnectionFactory cachingJmsConnectionFactory(MQConfigurationProperties properties, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers, JmsProperties jmsProperties) {
        JmsProperties.Cache cacheProperties = jmsProperties.getCache();
        properties.setQueueManager("QM1");
        properties.setConnName("localhost(1414)");
        MQConnectionFactory wrappedConnectionFactory = createConnectionFactory(properties, factoryCustomizers);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(wrappedConnectionFactory);
        connectionFactory.setCacheConsumers(cacheProperties.isConsumers());
        connectionFactory.setCacheProducers(cacheProperties.isProducers());
        connectionFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());
        return connectionFactory;
    }

    @Bean
    @SneakyThrows
    public ConnectionFactory domainB(MQConfigurationProperties properties, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers, JmsProperties jmsProperties) {
        JmsProperties.Cache cacheProperties = jmsProperties.getCache();
        properties.setQueueManager("QM2");
        properties.setConnName("localhost(1415)");
        MQConnectionFactory wrappedConnectionFactory = createConnectionFactory(properties, factoryCustomizers);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(wrappedConnectionFactory);
        connectionFactory.setCacheConsumers(cacheProperties.isConsumers());
        connectionFactory.setCacheProducers(cacheProperties.isProducers());
        connectionFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());
        return connectionFactory;
    }

    private static MQConnectionFactory createConnectionFactory(MQConfigurationProperties properties, ObjectProvider<List<MQConnectionFactoryCustomizer>> factoryCustomizers) {
        return (new MQConnectionFactoryFactory(properties, factoryCustomizers.getIfAvailable())).createConnectionFactory(MQConnectionFactory.class);
    }
}
