package co.com.bancolombia.commons.jms.mq.config.factory;


import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import co.com.bancolombia.commons.jms.internal.listener.reply.CorrelationExtractor;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQSchedulerProvider;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.ContextPerMessageStrategy;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.MultiContextSharedStrategy;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorModeProvider;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.mq.listeners.MQRequestReplyListener;
import co.com.bancolombia.commons.jms.mq.listeners.MQRequestReplyRemoteListener;
import co.com.bancolombia.commons.jms.mq.listeners.MQRequestReplySelector;
import co.com.bancolombia.commons.jms.utils.MQMessageListenerUtils;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.util.StringUtils;

import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolve;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveConcurrency;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveRetries;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQReqReplyFactory {

    public static Object createMQReqReply(ReqReply annotation, MQSpringResolver resolver, String beanName) {
        log.info("Creating bean instance for {} class", beanName);
        MQProperties properties = resolver.getProperties();
        if (properties.isReactive()) {
            resolver.resolveBean(MQMessageSender.class);
        } else {
            throw new RuntimeException("Not available for non reactive projects"); // TODO: Make it available
        }
        MQListenerConfig listenerConfig = validateAnnotationConfig(annotation, resolver, properties, beanName);
        MQMessageSender sender = (MQMessageSender) MQSenderFactory.fromReqReply(annotation, resolver, beanName);
        MQBrokerUtils mqBrokerUtils = resolver.getBrokerUtils();
        MQHealthListener healthListener = resolver.getHealthListener();
        MQQueuesContainer queuesContainer = resolver.getQueuesContainer();
        RetryableConfig retryableConfig = resolver.getRetryableConfig();
        Destination destination = resolveDestination(annotation, resolver, properties);
        CorrelationExtractor correlationExtractor = resolveCorrelationExtractor(annotation, resolver);
        switch (listenerConfig.getQueueType()) {
            case FIXED: {
                return fixedQueueWithMessageSelector(annotation, resolver, listenerConfig, sender, healthListener,
                        queuesContainer, retryableConfig, destination);
            }
            case FIXED_LOCATION_TRANSPARENCY: {
                return fixedQueueWithAsyncListener(resolver, beanName, listenerConfig, sender, mqBrokerUtils,
                        healthListener, queuesContainer, retryableConfig, destination, correlationExtractor);
            }
            default: {
                return temporaryQueueWithAsyncListener(resolver, beanName, listenerConfig, sender, mqBrokerUtils,
                        healthListener, queuesContainer, retryableConfig, destination, correlationExtractor);
            }
        }
    }

    private static MQRequestReplyListener temporaryQueueWithAsyncListener(MQSpringResolver resolver, String beanName,
                                                                          MQListenerConfig listenerConfig,
                                                                          MQMessageSender sender,
                                                                          MQBrokerUtils mqBrokerUtils,
                                                                          MQHealthListener healthListener,
                                                                          MQQueuesContainer queuesContainer,
                                                                          RetryableConfig retryableConfig,
                                                                          Destination destination,
                                                                          CorrelationExtractor correlationExtractor) {
        log.info("Using temporary queue with async listener");
        ReactiveReplyRouter<Message> router = resolver.resolveReplier();
        MQRequestReplyListener senderWithRouter = new MQRequestReplyListener(
                sender,
                router,
                queuesContainer,
                destination,
                listenerConfig.getListeningQueue(),
                correlationExtractor,
                listenerConfig.getMaxRetries());
        try {
            MQListenerConfig finalListenerConfig = listenerConfig.toBuilder()
                    .messageListener(senderWithRouter)
                    .build();
            MQMessageListenerUtils.createListeners(
                    finalListenerConfig,
                    queuesContainer,
                    mqBrokerUtils,
                    healthListener,
                    retryableConfig);
        } catch (JMSRuntimeException ex) {
            throw new BeanInitializationException("Could not create @ReqReply bean named " + beanName
                    + " with connectionFactory: " + listenerConfig.getConnectionFactory(), ex);
        }
        return senderWithRouter;
    }

    private static MQRequestReplyRemoteListener fixedQueueWithAsyncListener(MQSpringResolver resolver, String beanName,
                                                                            MQListenerConfig listenerConfig,
                                                                            MQMessageSender sender,
                                                                            MQBrokerUtils mqBrokerUtils,
                                                                            MQHealthListener healthListener,
                                                                            MQQueuesContainer queuesContainer,
                                                                            RetryableConfig retryableConfig,
                                                                            Destination destination,
                                                                            CorrelationExtractor correlationExtractor) {
        log.info("Using fixed queue with location transparency");
        ReactiveReplyRouter<JmsMessage> router = resolver.resolveReplier(JmsMessage.class);
        MQRequestReplyRemoteListener senderWithRouter = new MQRequestReplyRemoteListener(
                sender,
                router,
                queuesContainer,
                destination,
                listenerConfig.getListeningQueue(),
                correlationExtractor,
                listenerConfig.getMaxRetries());
        try {
            MQListenerConfig finalListenerConfig = listenerConfig.toBuilder()
                    .messageListener(senderWithRouter)
                    .build();
            MQMessageListenerUtils.createListeners(
                    finalListenerConfig,
                    queuesContainer,
                    mqBrokerUtils,
                    healthListener,
                    retryableConfig);
        } catch (JMSRuntimeException ex) {
            throw new BeanInitializationException("Could not create @ReqReply bean named " + beanName
                    + " with connectionFactory: " + listenerConfig.getConnectionFactory(), ex);
        }
        return senderWithRouter;
    }

    private static MQRequestReplySelector fixedQueueWithMessageSelector(ReqReply annotation, MQSpringResolver resolver,
                                                                        MQListenerConfig listenerConfig,
                                                                        MQMessageSender sender,
                                                                        MQHealthListener healthListener,
                                                                        MQQueuesContainer queuesContainer,
                                                                        RetryableConfig retryableConfig,
                                                                        Destination destination) {
        log.info("Using fixed queue with message selector");
        String selectorMode = resolver.resolveString(annotation.selectorMode());
        SelectorModeProvider selectorModeProvider = getSelectorModeProvider(resolver, selectorMode,
                listenerConfig.getConcurrency());
        MQMultiContextMessageSelectorListenerSync selectorListener = new MQMultiContextMessageSelectorListenerSync(
                listenerConfig,
                healthListener,
                retryableConfig,
                selectorModeProvider,
                queuesContainer);
        SelectorBuilder selector = resolver.getSelectorBuilder();
        MQSchedulerProvider schedulerProvider = resolver.getMqExecutorService();
        MQMessageSelectorListener reactiveSelectorListener = new MQMultiContextMessageSelectorListener(
                selectorListener, schedulerProvider);
        return new MQRequestReplySelector(
                sender,
                queuesContainer,
                destination,
                listenerConfig.getListeningQueue(),
                selector,
                reactiveSelectorListener);
    }

    @SneakyThrows
    private static Destination resolveDestination(ReqReply annotation, MQSpringResolver resolver,
                                                  MQProperties properties) {
        String requestQueue = resolver.resolveString(annotation.requestQueue());
        String queueCustomizerName = resolver.resolveString(annotation.queueCustomizer());
        String name = StringUtils.hasText(requestQueue) ? requestQueue : properties.getOutputQueue();
        MQQueueCustomizer customizer = resolver.resolveBean(queueCustomizerName, MQQueueCustomizer.class);
        Queue queue = new MQQueue(name);
        customizer.customize(queue);
        return queue;
    }

    private static MQListenerConfig validateAnnotationConfig(ReqReply annotation, MQSpringResolver resolver,
                                                             MQProperties properties, String className) {
        // Annotation property
        String concurrencyStr = resolver.resolveString(annotation.concurrency());
        String maxRetriesStr = resolver.resolveString(annotation.maxRetries());
        String replyQueue = resolver.resolveString(annotation.replyQueue());
        String queueCustomizerName = annotation.queueCustomizer();
        MQListenerConfig.QueueType queueType = annotation.queueType();
        String selectorMode = annotation.selectorMode();

        // Resolve dynamic values
        int finalConcurrency = resolveConcurrency(Integer.parseInt(concurrencyStr), properties.getInputConcurrency());
        int maxRetries = resolveRetries(maxRetriesStr);
        MQQueueCustomizer customizer = resolver.resolveBean(queueCustomizerName, MQQueueCustomizer.class);
        if (queueType == MQListenerConfig.QueueType.TEMPORARY) {
            replyQueue = resolve(replyQueue, className);
        } else {
            replyQueue = resolve(replyQueue, properties.getInputQueue());
        }
        ConnectionFactory connectionFactory = resolver.getConnectionFactory(annotation.connectionFactory());
        MQListenerConfig.MQListenerConfigBuilder builder = MQListenerConfig.builder()
                .connectionFactory(connectionFactory)
                .concurrency(finalConcurrency)
                .queueType(queueType)
                .selectorMode(selectorMode)
                .listeningQueue(replyQueue)
                .queueCustomizer(customizer)
                .maxRetries(maxRetries);
        if (properties.isInputQueueSetQueueManager()) {
            builder.qmSetter(resolver.getMqQueueManagerSetter());
        }
        MQListenerConfig listenerConfig = builder.build();
        if (!StringUtils.hasText(listenerConfig.getListeningQueue())) {
            throw new MQInvalidListenerException("Invalid configuration, should define replyQueue and queueType");
        }
        return listenerConfig;
    }

    // Selector
    private static SelectorModeProvider getSelectorModeProvider(MQSpringResolver resolver, String mode,
                                                                int concurrency) {
        if (MQListenerConfig.SelectorMode.CONTEXT_PER_MESSAGE.name().equals(mode)) {
            return (factory, context) -> new ContextPerMessageStrategy(factory);
        }
        if (MQListenerConfig.SelectorMode.MULTI_CONTEXT_SHARED.name().equals(mode)) {
            return (factory, context) -> new MultiContextSharedStrategy(factory, concurrency);
        }
        if (MQListenerConfig.SelectorMode.CONTEXT_SHARED.name().equals(mode)) {
            return SelectorModeProvider.defaultSelector();
        }
        return resolver.resolveBean(mode, SelectorModeProvider.class);
    }


    @SneakyThrows
    private static CorrelationExtractor resolveCorrelationExtractor(ReqReply annotation, MQSpringResolver resolver) {
        return resolver.resolveBean(annotation.correlationExtractor(), CorrelationExtractor.class);
    }
}

