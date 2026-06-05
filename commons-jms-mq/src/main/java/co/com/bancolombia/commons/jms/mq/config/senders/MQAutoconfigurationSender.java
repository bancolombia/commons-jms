package co.com.bancolombia.commons.jms.mq.config.senders;

import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQSenderConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.factory.MQSenderFactory;
import jakarta.jms.ConnectionFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import static co.com.bancolombia.commons.jms.mq.config.MQProperties.DEFAULT_DOMAIN;

@Log4j2
public class MQAutoconfigurationSender {

    @Bean
    @Lazy
    @Primary
    @ConditionalOnMissingBean(MQMessageSender.class)
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "true")
    public MQMessageSender defaultMQMessageSender(ConnectionFactory cf,
                                                  MQDestinationProvider provider,
                                                  MQProducerCustomizer customizer,
                                                  MQProperties properties,
                                                  MQHealthListener healthListener,
                                                  RetryableConfig retryableConfig,
                                                  MQSenderContainer container) {
        if (container.containsKey(DEFAULT_DOMAIN)) {
            return container.getReactive(DEFAULT_DOMAIN);
        }

        MQSenderConfig config = MQSenderConfig.builder()
                .connectionFactory(cf)
                .concurrency(properties.getOutputConcurrency())
                .destinationProvider(provider)
                .producerCustomizer(customizer)
                .healthListener(healthListener)
                .retryableConfig(retryableConfig)
                .build();

        return (MQMessageSender) MQSenderFactory.fromSenderConfig(DEFAULT_DOMAIN, container, properties, config);
    }

    @Bean
    @Lazy
    @Primary
    @ConditionalOnMissingBean(MQMessageSenderSync.class)
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "false")
    public MQMessageSenderSync defaultMQMessageSenderSync(ConnectionFactory cf,
                                                          MQDestinationProvider provider,
                                                          MQProducerCustomizer customizer,
                                                          MQProperties properties,
                                                          MQHealthListener healthListener,
                                                          RetryableConfig retryableConfig,
                                                          MQSenderContainer container) {
        if (container.containsKey(DEFAULT_DOMAIN)) {
            return container.getImperative(DEFAULT_DOMAIN);
        }

        MQSenderConfig config = MQSenderConfig.builder()
                .connectionFactory(cf)
                .concurrency(properties.getOutputConcurrency())
                .destinationProvider(provider)
                .producerCustomizer(customizer)
                .healthListener(healthListener)
                .retryableConfig(retryableConfig)
                .build();

        return (MQMessageSenderSync) MQSenderFactory.fromSenderConfig(DEFAULT_DOMAIN, container, properties, config);
    }

    @Bean
    @ConditionalOnMissingBean(MQSenderContainer.class)
    public MQSenderContainer defaultMqSenderContainer() {
        return new MQSenderContainer();
    }
}
