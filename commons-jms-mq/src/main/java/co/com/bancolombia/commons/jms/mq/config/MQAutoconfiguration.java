package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.reply.CorrelationExtractor;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQSchedulerProvider;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.config.health.MQListenerDisabledHealthIndicator;
import co.com.bancolombia.commons.jms.mq.config.health.MQListenerHealthIndicator;
import co.com.bancolombia.commons.jms.mq.utils.MQUtils;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_MQMD_READ_ENABLED;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_MQMD_WRITE_ENABLED;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_PUT_ASYNC_ALLOWED_ENABLED;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_READ_AHEAD_ALLOWED_ENABLED;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_TARGET_CLIENT;

@Configuration
@Import(MQAnnotationAutoconfiguration.class)
public class MQAutoconfiguration {
    public static final String CLASS_LOADER_WARN = "Your class loader has been changed, please add " +
            "System.setProperty(\"spring.devtools.restart.enabled\", \"false\"); before SpringApplication.run(...)";
    public static final int MAX_THREADS = 200;
    public static final int KEEP_ALIVE_SECONDS = 5;

    @Bean
    @ConditionalOnMissingBean(MQQueueCustomizer.class)
    public MQQueueCustomizer defaultMQQueueCustomizer() {
        return queue -> {
            if (queue instanceof MQQueue) {
                MQQueue customized = (MQQueue) queue;
                customized.setProperty(WMQ_TARGET_CLIENT, "1");
                customized.setProperty(WMQ_MQMD_READ_ENABLED, "true");
                customized.setProperty(WMQ_MQMD_WRITE_ENABLED, "true");
                customized.setPutAsyncAllowed(WMQ_PUT_ASYNC_ALLOWED_ENABLED);
                customized.setReadAheadAllowed(WMQ_READ_AHEAD_ALLOWED_ENABLED);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(MQQueuesContainer.class)
    public MQQueuesContainer defaultMQQueuesContainer() {
        return new MQQueuesContainerImp();
    }

    @Bean
    @ConditionalOnMissingBean(MQBrokerUtils.class)
    public MQBrokerUtils defaultMqBrokerUtils() {
        return (context, queue) -> {
            String qmName = MQUtils.extractQMName(context);
            MQUtils.setQMName(queue, qmName);
        };
    }

    @Bean
    @ConditionalOnMissingBean(MQHealthListener.class)
    public MQHealthListener defaultMqHealthListener(ApplicationEventPublisher publisher,
                                                    @Value("${management.health.jms.enabled:true}") boolean enabled) {
        if (!enabled) {
            return new MQListenerDisabledHealthIndicator();
        }
        return new MQListenerHealthIndicator(publisher);
    }

    @Bean
    @ConditionalOnMissingBean(RetryableConfig.class)
    public RetryableConfig defaultRetryableConfig(MQProperties properties) {
        return RetryableConfig.builder()
                .maxRetries(properties.getMaxRetries())
                .initialRetryIntervalMillis(properties.getInitialRetryIntervalMillis())
                .multiplier(properties.getRetryMultiplier())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(MQDestinationProvider.class)
    public MQDestinationProvider defaultMqDestinationProvider(MQQueueCustomizer customizer,
                                                              MQProperties properties) {
        return context -> MQQueueUtils.setupFixedQueue(context, MQListenerConfig.builder()
                .listeningQueue(properties.getOutputQueue())
                .queueCustomizer(customizer).build());
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

    @Bean
    @ConditionalOnMissingBean(MQQueueManagerSetter.class)
    public MQQueueManagerSetter qmSetter(MQProperties properties, MQQueuesContainer container) {
        return (jmsContext, queue) -> {
            if (properties.isInputQueueSetQueueManager()) {
                MQUtils.setQMNameIfNotSet(jmsContext, queue);
            }
            container.registerQueue(properties.getInputQueue(), queue);
        };
    }

    @Bean
    @ConditionalOnMissingBean(SelectorBuilder.class)
    public SelectorBuilder defaultSelectorBuilder() {
        return SelectorBuilder.ofDefaults();
    }

    @Bean
    @ConditionalOnMissingBean(CorrelationExtractor.class)
    public CorrelationExtractor defaultCorrelationExtractor() {
        return Message::getJMSCorrelationID;
    }


    @Bean
    @ConditionalOnMissingBean(MQSchedulerProvider.class)
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "true")
    public MQSchedulerProvider defaultMqExecutorService() {
        Scheduler scheduler = Schedulers.newBoundedElastic(MAX_THREADS, 2, "selector-pool",
                KEEP_ALIVE_SECONDS);
        return () -> scheduler;
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveReplyRouter.class)
    @ConditionalOnProperty(prefix = "commons.jms", name = "reactive", havingValue = "true")
    public ReactiveReplyRouter<Message> selectorReactiveReplyRouter() {
        return new ReactiveReplyRouter<>();
    }
}
