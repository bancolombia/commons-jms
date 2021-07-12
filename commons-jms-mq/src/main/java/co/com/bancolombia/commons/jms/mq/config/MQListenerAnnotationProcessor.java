package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.mq.MQListener;
import co.com.bancolombia.commons.jms.mq.MQListeners;
import co.com.bancolombia.commons.jms.mq.MQReactiveMessageListener;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.utils.MQMessageListenerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import javax.jms.ConnectionFactory;
import javax.jms.JMSRuntimeException;
import javax.jms.MessageListener;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Log4j2
@RequiredArgsConstructor
@Configuration
public class MQListenerAnnotationProcessor implements BeanPostProcessor, BeanFactoryAware {
    private final BeanFactory beanFactory;
    private StringValueResolver embeddedValueResolver;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (AnnotationUtils.isCandidateClass(targetClass, MQListener.class)) {
            Map<Method, Set<MQListener>> annotatedMethods = getAnnotatedMethods(targetClass);
            processAnnotated(bean, beanName, annotatedMethods);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }

    private void processAnnotated(Object bean, String beanName, Map<Method, Set<MQListener>> annotatedMethods) {
        if (!annotatedMethods.isEmpty()) {
            annotatedMethods.forEach((method, listeners) ->
                    listeners.forEach(listener -> processJmsListener(listener, method, bean)));
            if (log.isInfoEnabled()) {
                log.info("{} @MQListener methods processed on bean '{}': {}",
                        annotatedMethods.size(), beanName, annotatedMethods);
            }
        }
    }

    private void processJmsListener(MQListener mqListener, Method mostSpecificMethod, Object bean) {
        MQListenerConfig config = validateAnnotationConfig(mqListener);
        Method invocableMethod = AopUtils.selectInvocableMethod(mostSpecificMethod, bean.getClass());
        InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(bean, invocableMethod);
        MessageListener processor = new MQReactiveMessageListener(handlerMethod);
        ConnectionFactory cf = resolveBeanWithName(mqListener.connectionFactory(), ConnectionFactory.class);
        MQTemporaryQueuesContainer temporaryQueuesContainer = beanFactory.getBean(MQTemporaryQueuesContainer.class);
        try {
            MQMessageListenerUtils.createListeners(cf, processor, temporaryQueuesContainer, config);
        } catch (JMSRuntimeException ex) {
            throw new BeanInitializationException("Could not register MQ listener on [" +
                    mostSpecificMethod + "], using ConnectionFactory: " + cf, ex);
        }
    }

    private MQListenerConfig validateAnnotationConfig(MQListener config) {
        // Resolve dynamic values
        int concurrency = Integer.parseInt(Objects.
                requireNonNull(embeddedValueResolver.resolveStringValue(config.concurrency())));
        String queue = embeddedValueResolver.resolveStringValue(config.value());
        MQQueueCustomizer customizer = resolveBeanWithName(config.queueCustomizer(), MQQueueCustomizer.class);
        MQProperties properties = resolveBeanWithName("", MQProperties.class);
        // Map params
        if (StringUtils.hasText(queue) && StringUtils.hasText(config.tempQueueAlias())) {
            throw new MQInvalidListenerException("Invalid configuration, should define only one of value or " +
                    "tempQueueAlias in @MQListener annotation");
        }
        if (StringUtils.hasText(properties.getInputQueue()) && StringUtils.hasText(properties.getInputQueueAlias())) {
            throw new MQInvalidListenerException("Invalid configuration, should define only one of " +
                    "commons.jms.input-queue or commons.jms.input-queue-alias in your application.yaml file");
        }
        String temporaryQueue = resolveQueue(config.tempQueueAlias(), queue, properties.getInputQueueAlias());
        String fixedQueue = resolveQueue(queue, temporaryQueue, properties.getInputQueue());
        MQListenerConfig listenerConfig = MQListenerConfig.builder()
                .concurrency(resolveConcurrency(concurrency, properties.getInputConcurrency()))
                .tempQueueAlias(temporaryQueue)
                .queue(fixedQueue)
                .connectionFactory(config.connectionFactory())
                .customizer(customizer)
                .build();
        if (!StringUtils.hasText(listenerConfig.getQueue()) && !StringUtils.hasText(listenerConfig.getTempQueueAlias())) {
            throw new MQInvalidListenerException("Invalid configuration, should define one of value or tempQueueAlias");
        }
        return listenerConfig;
    }

    private int resolveConcurrency(int concurrencyAnnotation, int concurrencyProperties) {
        if (concurrencyAnnotation > 0) {
            return concurrencyAnnotation;
        }
        if (concurrencyProperties > 0) {
            return concurrencyProperties;
        }
        return MQProperties.DEFAULT_CONCURRENCY;
    }

    private String resolveQueue(String primaryAnnotation, String secondaryValue, String queueProperties) {
        if (StringUtils.hasText(primaryAnnotation)) {
            return primaryAnnotation;
        }
        if (StringUtils.isEmpty(secondaryValue) && StringUtils.hasText(queueProperties)) {
            return queueProperties;
        }
        return null;
    }

    private Map<Method, Set<MQListener>> getAnnotatedMethods(Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<Set<MQListener>>) method -> {
                    Set<MQListener> listenerMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                            method, MQListener.class, MQListeners.class);
                    return (!listenerMethods.isEmpty() ? listenerMethods : null);
                });
    }

    private <T> T resolveBeanWithName(String beanName, Class<T> tClass) {
        if (StringUtils.hasText(beanName)) {
            return beanFactory.getBean(beanName, tClass);
        } else {
            return beanFactory.getBean(tClass);
        }
    }

}
