package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidSenderException;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.jms.ConnectionFactory;

@Log4j2
public class MQAutoconfigurationSelectorListener {

    @Bean
    @ConditionalOnMissingBean(MQMessageSelectorListener.class)
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "true")
    public MQMessageSelectorListener defaultMQMessageSelectorListener(
            MQMultiContextMessageSelectorListenerSync senderSync) {
        return new MQMultiContextMessageSelectorListener(senderSync);
    }

    @Bean
    @ConditionalOnMissingBean(MQMultiContextMessageSelectorListenerSync.class)
    public MQMultiContextMessageSelectorListenerSync defaultMQMultiContextMessageSelectorListenerSync(
            ConnectionFactory cf, MQListenerConfig config) {
        if (config.getConcurrency() < 1) {
            throw new MQInvalidSenderException("Invalid property commons.jms.input-concurrency, minimum value 1, " +
                    "you have passed " + config.getConcurrency());
        }
        if (log.isInfoEnabled()) {
            log.info("Creating {} listeners", config.getConcurrency());
        }
        return new MQMultiContextMessageSelectorListenerSync(cf, config);
    }

    @Bean
    @ConditionalOnMissingBean(MQListenerConfig.class)
    public MQListenerConfig defaultMQListenerConfig(MQProperties properties, MQQueueCustomizer customizer) {
        return MQListenerConfig.builder()
                .concurrency(properties.getInputConcurrency())
                .queue(properties.getInputQueue())
                .customizer(customizer)
                .build();
    }

}
