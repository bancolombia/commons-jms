package co.com.bancolombia.commons.jms.mq.config.factory;

import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.MQSenderConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.internal.sender.MQMultiContextSender;
import co.com.bancolombia.commons.jms.internal.sender.MQMultiContextSenderSync;
import co.com.bancolombia.commons.jms.mq.MQSender;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.senders.MQSenderContainer;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import co.com.bancolombia.commons.jms.mq.config.factory.model.AnnotationSenderSettings;
import co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import jakarta.jms.ConnectionFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static co.com.bancolombia.commons.jms.mq.config.MQProperties.DEFAULT_DOMAIN;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolve;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveConcurrency;
import static java.util.Objects.requireNonNull;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQSenderFactory {

    public static Object fromMQSender(MQSender annotation, MQSpringResolver resolver, String beanName) {
        AnnotationSenderSettings settings = AnnotationSenderSettings.from(annotation);
        return fromAnnotationSettings(settings, resolver, beanName);
    }

    public static Object fromReqReply(ReqReply annotation, MQSpringResolver resolver, String beanName) {
        AnnotationSenderSettings settings = AnnotationSenderSettings.from(annotation);
        return fromAnnotationSettings(settings, resolver, beanName);
    }

    private static Object fromAnnotationSettings(AnnotationSenderSettings settings, MQSpringResolver resolver,
                                                 String beanName) {
        log.info("Creating bean instance for {} class", beanName);
        String domain = AnnotationUtils.resolve(settings.getConnectionFactory(), DEFAULT_DOMAIN);
        MQSenderContainer container = resolver.getMqSenderContainer();
        MQProperties properties = resolver.getProperties();
        if (container.containsKey(domain)) {
            log.info("Using already created bean instance for {} class", beanName);
            return properties.isReactive() ? container.getReactive(domain) : container.getImperative(domain);
        }

        ConnectionFactory connectionFactory = resolver.getConnectionFactory(settings.getConnectionFactory());
        int concurrency = Integer.parseInt(requireNonNull(resolver.resolveString(settings.getConcurrency())));
        int finalConcurrency = resolveConcurrency(concurrency, properties.getOutputConcurrency());
        String realQueue = resolve(resolver.resolveString(settings.getDestinationQueue()), properties.getOutputQueue());
        MQProducerCustomizer pCust = resolver.resolveBean(settings.getProducerCustomizer(), MQProducerCustomizer.class);
        MQQueueCustomizer queueCust = resolver.resolveBean(settings.getQueueCustomizer(), MQQueueCustomizer.class);
        RetryableConfig retryableConfig = resolver.resolveBean(settings.getRetryConfig(), RetryableConfig.class);
        MQDestinationProvider destinationProvider = buildMqDestinationProvider(queueCust, realQueue);
        MQHealthListener healthListener = resolver.getHealthListener();

        MQSenderConfig config = MQSenderConfig.builder()
                .connectionFactory(connectionFactory)
                .concurrency(finalConcurrency)
                .destinationProvider(destinationProvider)
                .producerCustomizer(pCust)
                .healthListener(healthListener)
                .retryableConfig(retryableConfig)
                .build();

        return fromSenderConfig(domain, container, properties, config);
    }

    public static Object fromSenderConfig(String domain, MQSenderContainer container,
                                          MQProperties properties, MQSenderConfig config) {
        MQMultiContextSenderSync sender = new MQMultiContextSenderSync(config);
        if (properties.isReactive()) {
            MQMultiContextSender senderReactive = new MQMultiContextSender(sender);
            container.put(domain, senderReactive);
            return senderReactive;
        } else {
            container.put(domain, sender);
            return sender;
        }
    }

    private static MQDestinationProvider buildMqDestinationProvider(MQQueueCustomizer customizer, String queueName) {
        return context -> MQQueueUtils.setupFixedQueue(context, MQListenerConfig.builder()
                .listeningQueue(queueName)
                .queueCustomizer(customizer).build());
    }
}