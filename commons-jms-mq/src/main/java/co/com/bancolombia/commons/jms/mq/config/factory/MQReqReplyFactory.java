package co.com.bancolombia.commons.jms.mq.config.factory;


import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQMultiContextMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.ContextPerMessageStrategy;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorModeProvider;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.mq.listeners.MQExecutorService;
import co.com.bancolombia.commons.jms.mq.listeners.MQRequestReplyListener;
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
            resolver.resolveBean(MQMessageSenderSync.class);
        }
        MQListenerConfig listenerConfig = validateAnnotationConfig(annotation, resolver, properties, beanName);
        MQMessageSender sender = (MQMessageSender) MQSenderFactory.fromReqReply(annotation, resolver, beanName);
        MQBrokerUtils mqBrokerUtils = resolver.getBrokerUtils();
        MQHealthListener healthListener = resolver.getHealthListener();
        MQQueuesContainer queuesContainer = resolver.getQueuesContainer();
        RetryableConfig retryableConfig = resolver.getRetryableConfig();
        Destination destination = resolveDestination(annotation, resolver, properties);
        if (listenerConfig.getQueueType() == MQListenerConfig.QueueType.FIXED) {
            SelectorModeProvider selectorModeProvider = getSelectorModeProvider(annotation.selectorMode());
            MQMultiContextMessageSelectorListenerSync selectorListener = new MQMultiContextMessageSelectorListenerSync(
                    listenerConfig,
                    healthListener,
                    retryableConfig,
                    selectorModeProvider,
                    queuesContainer);
            if (properties.isReactive()) {
                ReactiveReplyRouter<Message> router = resolver.resolveReplier();
                MQExecutorService executorService = resolver.getMqExecutorService();
                SelectorBuilder selector = resolver.getSelectorBuilder();
                MQMessageSelectorListener reactiveSelectorListener = new MQMultiContextMessageSelectorListener(
                        selectorListener,
                        executorService,
                        router);
                return new MQRequestReplySelector(
                        sender,
                        queuesContainer,
                        destination,
                        listenerConfig.getListeningQueue(),
                        selector,
                        reactiveSelectorListener);
            } else {
                throw new RuntimeException("Not available for non reactive projects"); // TODO: Make it available
            }
        } else {
            if (properties.isReactive()) {
                ReactiveReplyRouter<Message> router = resolver.resolveReplier();
                MQRequestReplyListener senderWithRouter = new MQRequestReplyListener(
                        sender,
                        router,
                        queuesContainer,
                        destination,
                        listenerConfig.getListeningQueue(),
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
            } else {
                throw new RuntimeException("Not available for non reactive projects"); // TODO: Make it available
            }
        }
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
        MQListenerConfig.SelectorMode selectorMode = annotation.selectorMode();

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
    private static SelectorModeProvider getSelectorModeProvider(MQListenerConfig.SelectorMode mode) {
        if (MQListenerConfig.SelectorMode.CONTEXT_PER_MESSAGE == mode) {
            return (factory, context) -> new ContextPerMessageStrategy(factory);
        }
        return SelectorModeProvider.defaultSelector();
    }
}

