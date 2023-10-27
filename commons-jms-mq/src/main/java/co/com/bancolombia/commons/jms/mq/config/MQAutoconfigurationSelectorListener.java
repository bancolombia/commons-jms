package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.mq.utils.MQUtils;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class MQAutoconfigurationSelectorListener {
    public static int MAX_THREADS = 200;
    public static long KEEP_ALIVE_SECONDS = 5L;

    @Bean
    @ConditionalOnMissingBean(ExecutorService.class)
    @Qualifier("selectorExecutorService")
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "true")
    public ExecutorService defaultBySelectorExecutorService() {
        return new ThreadPoolExecutor(0, MAX_THREADS, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveReplyRouter.class)
    public ReactiveReplyRouter<Message> selectorReactiveReplyRouter() {
        return new ReactiveReplyRouter<>();
    }

    @Bean
    @ConditionalOnMissingBean(MQMessageSelectorListener.class)
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "true")
    public MQMessageSelectorListener defaultMQMessageSelectorListener(
            MQMultiContextMessageSelectorListenerSync senderSync,
            @Qualifier("selectorExecutorService") ExecutorService bySelectorExecutorService,
            ReactiveReplyRouter<Message> router) {
        return new MQMultiContextMessageSelectorListener(senderSync, bySelectorExecutorService, router);
    }

    @Bean
    @ConditionalOnMissingBean(MQMultiContextMessageSelectorListenerSync.class)
    public MQMultiContextMessageSelectorListenerSync defaultMQMultiContextMessageSelectorListenerSync(
            ConnectionFactory cf, @Qualifier("messageSelectorListenerConfig") MQListenerConfig config,
            MQHealthListener healthListener, MQProperties properties) {
        if (config.getConcurrency() < 1) {
            throw new MQInvalidListenerException("Invalid property commons.jms.input-concurrency, minimum value 1, " +
                    "you have passed " + config.getConcurrency());
        }
        if (log.isInfoEnabled()) {
            log.info("Creating {} listeners", config.getConcurrency());
        }
        RetryableConfig retryableConfig = RetryableConfig.builder()
                .maxRetries(properties.getMaxRetries())
                .initialRetryIntervalMillis(properties.getInitialRetryIntervalMillis())
                .multiplier(properties.getRetryMultiplier())
                .build();
        return new MQMultiContextMessageSelectorListenerSync(cf, config, healthListener, retryableConfig);
    }

    @Bean
    public MQListenerConfig messageSelectorListenerConfig(MQProperties properties, MQQueueCustomizer customizer,
                                                          MQQueueManagerSetter setter) {
        MQListenerConfig.MQListenerConfigBuilder builder = MQListenerConfig.builder()
                .concurrency(properties.getInputConcurrency())
                .queue(properties.getInputQueue())
                .customizer(customizer);

        if (properties.isInputQueueSetQueueManager()) {
            builder.qmSetter(setter);
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(MQQueueManagerSetter.class)
    public MQQueueManagerSetter qmSetter(MQProperties properties, MQQueuesContainer container) {
        return (jmsContext, queue) -> {
            log.info("Self assigning Queue Manager to listening queue: {}", queue.toString());
            if (properties.isInputQueueSetQueueManager()) {
                MQUtils.setQMNameIfNotSet(jmsContext, queue);
            }
            container.registerQueue(properties.getInputQueue(), queue);
        };
    }

}
