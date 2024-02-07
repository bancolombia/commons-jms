package co.com.bancolombia.commons.jms.mq.config.factory;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.MQListener;
import co.com.bancolombia.commons.jms.mq.MQListeners;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.mq.listeners.MQMessageListener;
import co.com.bancolombia.commons.jms.mq.listeners.MQReactiveMessageListener;
import co.com.bancolombia.commons.jms.utils.MQMessageListenerUtils;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MessageListener;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolve;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveConcurrency;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveRetries;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQListenerFactory {

    public static void postProcessAfterInitialization(MQSpringResolver resolver,
                                                      @NonNull Object bean,
                                                      @NonNull String beanName) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (AnnotationUtils.isCandidateClass(targetClass, MQListener.class)) {
            Map<Method, Set<MQListener>> annotatedMethods = getAnnotatedMethods(targetClass);
            processAnnotated(bean, beanName, annotatedMethods, resolver);
        }
    }

    private static Map<Method, Set<MQListener>> getAnnotatedMethods(Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<Set<MQListener>>) method -> {
                    Set<MQListener> listenerMethods =
                            AnnotatedElementUtils.getMergedRepeatableAnnotations(method, MQListener.class,
                                    MQListeners.class);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
    }

    private static void processAnnotated(Object bean, String beanName, Map<Method, Set<MQListener>> annotatedMethods,
                                         MQSpringResolver resolver) {
        if (!annotatedMethods.isEmpty()) {
            annotatedMethods.forEach((method, annotations) ->
                    annotations.forEach(annotation -> processJmsListener(annotation, method, bean, resolver)));
            if (log.isInfoEnabled()) {
                log.info("{} @MQListener methods processed on bean '{}': {}",
                        annotatedMethods.size(), beanName, annotatedMethods);
            }
        }
    }

    // Build listeners

    private static void processJmsListener(MQListener annotation, Method mostSpecificMethod, Object bean,
                                           MQSpringResolver resolver) {
        MQQueuesContainer queuesContainer = resolver.getQueuesContainer();
        MQBrokerUtils brokerUtils = resolver.getBrokerUtils();
        MQHealthListener healthListener = resolver.getHealthListener();
        RetryableConfig retryableConfig = resolver.getRetryableConfig();

        MQProperties properties = resolver.getProperties();
        MQListenerConfig listenerConfig = buildConfig(annotation, mostSpecificMethod, properties, resolver, bean);

        try {
            MQMessageListenerUtils.createListeners(
                    listenerConfig,
                    queuesContainer,
                    brokerUtils,
                    healthListener,
                    retryableConfig);
        } catch (JMSRuntimeException ex) {
            throw new BeanInitializationException("Could not register MQ listener on [" + mostSpecificMethod
                    + "], using ConnectionFactory: " + listenerConfig.getConnectionFactory(), ex);
        }
    }

    private static MQListenerConfig buildConfig(MQListener annotation, Method mostSpecificMethod,
                                                MQProperties properties, MQSpringResolver resolver, Object bean) {
        // Resolve dynamic values
        String queueName = resolve(resolver.resolveString(annotation.value()), properties.getInputQueue());
        MQQueueCustomizer customizer = resolver.resolveBean(annotation.queueCustomizer(), MQQueueCustomizer.class);

        int concurrency = Integer.parseInt(Objects.requireNonNull(resolver.resolveString(annotation.concurrency())));
        int finalConcurrency = resolveConcurrency(concurrency, properties.getInputConcurrency());
        int maxRetries = resolveRetries(annotation.maxRetries());
        ConnectionFactory connectionFactory = resolver.getConnectionFactory(annotation.connectionFactory());

        Method invocableMethod = AopUtils.selectInvocableMethod(mostSpecificMethod, bean.getClass());
        MessageListener processor = buildMessageListener(bean, invocableMethod, properties.isReactive(), finalConcurrency);

        MQListenerConfig listenerConfig = MQListenerConfig.builder()
                .connectionFactory(connectionFactory)
                .queueCustomizer(customizer)
                .messageListener(processor)
                .listeningQueue(queueName)
                .maxRetries(maxRetries)
                .concurrency(finalConcurrency)
                .build();
        if (!StringUtils.hasText(listenerConfig.getListeningQueue())) {
            throw new MQInvalidListenerException(
                    "Invalid configuration, should define one of value or commons.jms.input-queue");
        }
        return listenerConfig;
    }

    private static MessageListener buildMessageListener(Object bean, Method invocableMethod, boolean isReactive,
                                                        int maxRetries) {
        return isReactive ?
                MQReactiveMessageListener.fromBeanAndMethod(bean, invocableMethod, maxRetries)
                : MQMessageListener.fromBeanAndMethod(bean, invocableMethod, maxRetries);
    }

}
