package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.sender.MQMultiContextSender;
import co.com.bancolombia.commons.jms.internal.sender.MQMultiContextSenderSync;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidSenderException;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import jakarta.jms.ConnectionFactory;

@Log4j2
public class MQAutoconfigurationSender {

    @Bean
    @ConditionalOnMissingBean(MQMessageSender.class)
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "true")
    public MQMessageSender defaultMQMessageSender(MQMessageSenderSync senderSync) {
        return new MQMultiContextSender(senderSync);
    }

    @Bean
    @ConditionalOnMissingBean(MQMessageSenderSync.class)
    public MQMessageSenderSync defaultMQMessageSenderSync(ConnectionFactory cf,
                                                          MQDestinationProvider provider,
                                                          MQProducerCustomizer customizer,
                                                          MQProperties properties,
                                                          MQHealthListener healthListener) {
        if (properties.getOutputConcurrency() < 1) {
            throw new MQInvalidSenderException("Invalid property commons.jms.output-concurrency, minimum value 1, " +
                    "you have passed " + properties.getOutputConcurrency());
        }
        log.info("Creating {} senders", properties.getOutputConcurrency());
        return new MQMultiContextSenderSync(cf, properties.getOutputConcurrency(), provider, customizer, healthListener);
    }

    @Bean
    @ConditionalOnMissingBean(MQDestinationProvider.class)
    public MQDestinationProvider defaultDestinationProvider(MQQueueCustomizer customizer,
                                                            MQProperties properties) {
        return context -> MQQueueUtils.setupFixedQueue(context, MQListenerConfig.builder()
                .queue(properties.getOutputQueue())
                .customizer(customizer).build());
    }

    @Bean
    @ConditionalOnMissingBean(MQProducerCustomizer.class)
    public MQProducerCustomizer defaultMQProducerCustomizer(MQProperties properties) {
        return producer -> {
            if (properties.getProducerTtl() > 0) {
                producer.setTimeToLive(properties.getProducerTtl());
            }
        };
    }

}
